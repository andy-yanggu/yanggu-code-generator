package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.model.VarDesc;
import com.yanggu.code.generator.domain.vo.VariableNodeVO;
import com.yanggu.code.generator.enums.VariableKindEnum;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 模板变量目录构建器：反射 Model 类生成变量树
 * 刻意不注册为 bean、不注入任何依赖，便于纯单测直接 new
 */
public class VariableCatalogBuilder {

    private static final String MODEL_PACKAGE = "com.yanggu.code.generator.domain.model.";

    /**
     * 以 rootModelClass 的声明字段为根生成目录
     */
    public List<VariableNodeVO> build(Class<?> rootModelClass) {
        return buildModel(rootModelClass, "", new ArrayDeque<>());
    }

    private List<VariableNodeVO> buildModel(Class<?> modelClass, String pathPrefix, Deque<Class<?>> visiting) {
        if (visiting.contains(modelClass)) {
            // 循环引用防御：当前模型图无回边，保留兜底
            return new ArrayList<>();
        }
        visiting.addLast(modelClass);
        List<VariableNodeVO> nodeList = new ArrayList<>();
        for (Field field : modelClass.getDeclaredFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            nodeList.add(buildNode(field, pathPrefix, visiting));
        }
        visiting.removeLast();
        return nodeList;
    }

    private VariableNodeVO buildNode(Field field, String pathPrefix, Deque<Class<?>> visiting) {
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

        if (Map.class.isAssignableFrom(rawType)) {
            // 属性 key 运行时才知道，这里只留空壳，由 service 按模板组填充
            node.setJavaType("Map<String, Object>");
            node.setKind(VariableKindEnum.MAP);
            return node;
        }

        if (Collection.class.isAssignableFrom(rawType)) {
            Class<?> elementType = resolveElementType(field.getGenericType());
            node.setJavaType(describeCollection(rawType, elementType));
            if (elementType != null && isModel(elementType)) {
                node.setKind(VariableKindEnum.MODEL_LIST);
                node.setChildren(buildModel(elementType, node.getPath(), visiting));
            } else {
                node.setKind(VariableKindEnum.SCALAR_LIST);
                node.setChildren(new ArrayList<>(List.of(virtualElementNode(node, elementType))));
            }
            return node;
        }

        node.setJavaType(rawType.getSimpleName());
        if (isModel(rawType)) {
            node.setKind(VariableKindEnum.OBJECT);
            node.setChildren(buildModel(rawType, node.getPath(), visiting));
        } else {
            node.setKind(VariableKindEnum.SCALAR);
        }
        return node;
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
