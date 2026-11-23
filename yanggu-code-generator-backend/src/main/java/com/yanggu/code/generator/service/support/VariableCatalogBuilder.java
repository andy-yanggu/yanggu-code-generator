package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.model.VarDesc;
import com.yanggu.code.generator.domain.vo.VariableNodeVO;
import com.yanggu.code.generator.enums.VariableKindEnum;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 模板变量目录构建器：反射 Model 类生成变量树
 * 传入实例时额外产出「带值目录」：结构仍源自类，值逐字段读实例（敏感字段已掩码）
 * 刻意不注册为 bean、不注入任何依赖，便于纯单测直接 new
 */
public class VariableCatalogBuilder {

    private static final String MODEL_PACKAGE = "com.yanggu.code.generator.domain.model.";

    /**
     * 敏感值非空时的统一占位
     */
    private static final String SENSITIVE_MASK = "******";

    /**
     * 实例字段为 null 时的占位，区分「有值但空串」与「没值」
     */
    private static final String NULL_TEXT = "(null)";

    /**
     * 结构目录：以 rootModelClass 的声明字段为根生成目录，不带值（既有行为零变更）
     */
    public List<VariableNodeVO> build(Class<?> rootModelClass) {
        return buildModel(rootModelClass, null, "", new ArrayDeque<>());
    }

    /**
     * 带值目录：结构仍反射类拿 kind/loopVar/nullable/desc，值并行读 rootInstance 对应字段
     */
    public List<VariableNodeVO> build(Class<?> rootModelClass, Object rootInstance) {
        return buildModel(rootModelClass, rootInstance, "", new ArrayDeque<>());
    }

    private List<VariableNodeVO> buildModel(Class<?> modelClass, Object instance, String pathPrefix, Deque<Class<?>> visiting) {
        if (visiting.contains(modelClass)) {
            // 循环引用防御：当前模型图无回边，保留兑底
            return new ArrayList<>();
        }
        visiting.addLast(modelClass);
        List<VariableNodeVO> nodeList = new ArrayList<>();
        for (Field field : modelClass.getDeclaredFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            nodeList.add(buildNode(field, instance, pathPrefix, visiting));
        }
        visiting.removeLast();
        return nodeList;
    }

    private VariableNodeVO buildNode(Field field, Object owner, String pathPrefix, Deque<Class<?>> visiting) {
        VariableNodeVO node = new VariableNodeVO();
        Class<?> rawType = field.getType();
        node.setName(field.getName());
        node.setPath(pathPrefix.isEmpty() ? field.getName() : pathPrefix + "." + field.getName());
        node.setChildren(new ArrayList<>());

        VarDesc varDesc = field.getAnnotation(VarDesc.class);
        // 缺注解照常列出，desc 空串：面板挂掉不影响代码生成
        node.setDesc(varDesc == null ? "" : varDesc.value());
        node.setLoopVar(varDesc == null ? "" : varDesc.loopVar());
        node.setNullable(varDesc != null && varDesc.nullable());
        boolean sensitive = varDesc != null && varDesc.sensitive();
        Object rawValue = readValue(field, owner);

        if (Map.class.isAssignableFrom(rawType)) {
            // 结构目录留空壳由 service 填 key；带值目录直接展开实例 entries
            node.setJavaType("Map<String, Object>");
            node.setKind(VariableKindEnum.MAP);
            if (owner != null) {
                fillMapValues(node, rawValue, sensitive);
            }
            return node;
        }

        if (Collection.class.isAssignableFrom(rawType)) {
            Class<?> elementType = resolveElementType(field.getGenericType());
            node.setJavaType(describeCollection(rawType, elementType));
            if (isModel(elementType)) {
                node.setKind(VariableKindEnum.MODEL_LIST);
                if (owner == null) {
                    node.setChildren(buildModel(elementType, null, node.getPath(), visiting));
                } else {
                    fillModelListValues(node, elementType, rawValue, visiting);
                }
            } else {
                node.setKind(VariableKindEnum.SCALAR_LIST);
                if (owner == null) {
                    node.setChildren(new ArrayList<>(List.of(virtualElementNode(node, elementType))));
                } else {
                    fillScalarListValues(node, rawValue, sensitive);
                }
            }
            return node;
        }

        node.setJavaType(rawType.getSimpleName());
        if (isModel(rawType)) {
            node.setKind(VariableKindEnum.OBJECT);
            if (owner == null) {
                node.setChildren(buildModel(rawType, null, node.getPath(), visiting));
            } else if (rawValue == null) {
                // 嵌套对象未赋值：只留节点与 (null) 占位，子级不展开
                node.setValue(NULL_TEXT);
            } else {
                node.setChildren(buildModel(rawValue.getClass(), rawValue, node.getPath(), visiting));
            }
        } else {
            node.setKind(VariableKindEnum.SCALAR);
            if (owner != null) {
                node.setValue(describeValue(rawValue, sensitive));
            }
        }
        return node;
    }

    /**
     * 带值 MAP：children 直接来自实例 entries；实例 map 为 null 时置 (null) 且无子级
     */
    private void fillMapValues(VariableNodeVO mapNode, Object rawValue, boolean sensitive) {
        if (!(rawValue instanceof Map<?, ?> map)) {
            mapNode.setValue(NULL_TEXT);
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            VariableNodeVO node = new VariableNodeVO();
            String key = String.valueOf(entry.getKey());
            node.setName(key);
            node.setPath(mapNode.getPath() + "." + key);
            node.setJavaType("Object");
            node.setKind(VariableKindEnum.SCALAR);
            node.setDesc("");
            node.setLoopVar("");
            node.setNullable(entry.getValue() == null);
            node.setValue(describeValue(entry.getValue(), sensitive));
            node.setChildren(new ArrayList<>());
            mapNode.getChildren().add(node);
        }
    }

    /**
     * 带值 MODEL_LIST：逐实际元素展开，元素名 = 循环变量名 + 下标（策略 B：父节点携「共 N 条」，收起由前端控制）
     */
    private void fillModelListValues(VariableNodeVO listNode, Class<?> elementType, Object rawValue, Deque<Class<?>> visiting) {
        if (!(rawValue instanceof Collection<?> elements)) {
            listNode.setValue(NULL_TEXT);
            return;
        }
        listNode.setValue("共 " + elements.size() + " 条");
        String loopVar = listNode.getLoopVar().isEmpty() ? listNode.getName() : listNode.getLoopVar();
        int index = 0;
        for (Object element : elements) {
            VariableNodeVO node = new VariableNodeVO();
            node.setName(loopVar + "[" + index + "]");
            node.setPath(listNode.getPath() + "[" + index + "]");
            node.setJavaType(elementType.getSimpleName());
            node.setKind(VariableKindEnum.OBJECT);
            node.setDesc("");
            node.setLoopVar("");
            node.setNullable(Boolean.FALSE);
            // null 元素保留占位行不展开，下标不错位
            node.setChildren(element == null ? new ArrayList<>() : buildModel(elementType, element, node.getPath(), visiting));
            listNode.getChildren().add(node);
            index++;
        }
    }

    /**
     * 带值 SCALAR_LIST：逐实际元素展开为标量行；结构目录的虚拟元素节点仅服务于插入骨架，不带值
     */
    private void fillScalarListValues(VariableNodeVO listNode, Object rawValue, boolean sensitive) {
        if (!(rawValue instanceof Collection<?> elements)) {
            listNode.setValue(NULL_TEXT);
            return;
        }
        listNode.setValue("共 " + elements.size() + " 条");
        int index = 0;
        for (Object element : elements) {
            VariableNodeVO node = new VariableNodeVO();
            node.setName("[" + index + "]");
            node.setPath(listNode.getPath() + "[" + index + "]");
            node.setJavaType(element == null ? "Object" : element.getClass().getSimpleName());
            node.setKind(VariableKindEnum.SCALAR);
            node.setDesc("");
            node.setLoopVar("");
            node.setNullable(element == null);
            node.setValue(describeValue(element, sensitive));
            node.setChildren(new ArrayList<>());
            listNode.getChildren().add(node);
            index++;
        }
    }

    /**
     * 值文本：null 置占位；敏感非空一律掩码
     */
    private static String describeValue(Object value, boolean sensitive) {
        if (value == null) {
            return NULL_TEXT;
        }
        return sensitive ? SENSITIVE_MASK : String.valueOf(value);
    }

    private static Object readValue(Field field, Object owner) {
        if (owner == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(owner);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("读取变量值失败: " + field, e);
        }
    }

    /**
     * 标量集合的虚拟元素节点：只用于承载「插入 ${loopVar}」，不展开层级
     */
    private VariableNodeVO virtualElementNode(VariableNodeVO parent, Class<?> elementType) {
        VariableNodeVO node = new VariableNodeVO();
        node.setName(elementType == null ? "" : elementType.getSimpleName());
        node.setPath(parent.getPath());
        node.setJavaType(elementType == null ? "Object" : elementType.getSimpleName());
        node.setKind(VariableKindEnum.SCALAR);
        node.setDesc("");
        node.setLoopVar("");
        node.setNullable(Boolean.FALSE);
        node.setChildren(new ArrayList<>());
        return node;
    }

    private boolean isModel(Class<?> type) {
        return type != null && type.getName().startsWith(MODEL_PACKAGE);
    }

    /**
     * 取集合元素类型；raw type 或通配符返回 null，按标量集合降级
     */
    private Class<?> resolveElementType(Type genericType) {
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return null;
        }
        Type[] args = parameterizedType.getActualTypeArguments();
        return args.length == 1 && args[0] instanceof Class<?> elementClass ? elementClass : null;
    }

    private String describeCollection(Class<?> rawType, Class<?> elementType) {
        String simpleName = rawType == Set.class ? "Set" : "List";
        return simpleName + "<" + (elementType == null ? "Object" : elementType.getSimpleName()) + ">";
    }

}
