package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.model.BaseClassModel;
import com.yanggu.code.generator.domain.model.EnumItemModel;
import com.yanggu.code.generator.domain.model.EnumModel;
import com.yanggu.code.generator.domain.model.ModuleModel;
import com.yanggu.code.generator.domain.model.ProjectModel;
import com.yanggu.code.generator.domain.model.TableFieldModel;
import com.yanggu.code.generator.domain.model.TableModel;
import com.yanggu.code.generator.domain.model.VarDesc;
import com.yanggu.code.generator.domain.vo.VariableNodeVO;
import com.yanggu.code.generator.enums.VariableKindEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableCatalogBuilderTest {

    private static final List<Class<?>> ALL_MODEL_CLASSES = List.of(ProjectModel.class, TableModel.class,
            TableFieldModel.class, EnumModel.class, EnumItemModel.class, ModuleModel.class, BaseClassModel.class);

    private final VariableCatalogBuilder builder = new VariableCatalogBuilder();

    @Test
    void scalarFieldsBecomeLeafNodes() {
        List<VariableNodeVO> nodeList = builder.build(ModuleModel.class);

        assertEquals(3, nodeList.size());
        VariableNodeVO moduleName = nodeList.getFirst();
        assertEquals("moduleName", moduleName.getName());
        // 根级 path 即字段名：FreeMarker 数据模型无对象名前缀
        assertEquals("moduleName", moduleName.getPath());
        assertEquals("String", moduleName.getJavaType());
        assertEquals(VariableKindEnum.SCALAR, moduleName.getKind());
        assertEquals("模块名称", moduleName.getDesc());
        assertEquals(Boolean.FALSE, moduleName.getNullable());
        assertEquals(List.of(), moduleName.getChildren());
    }

    private VariableNodeVO find(List<VariableNodeVO> nodeList, String name) {
        return nodeList.stream().filter(item -> name.equals(item.getName())).findFirst().orElse(null);
    }

    @Test
    void modelListDrillsDownIntoElementTypeFields() {
        List<VariableNodeVO> nodeList = builder.build(EnumModel.class);
        VariableNodeVO itemList = find(nodeList, "enumItemList");

        assertNotNull(itemList);
        assertEquals(VariableKindEnum.MODEL_LIST, itemList.getKind());
        assertEquals("List<EnumItemModel>", itemList.getJavaType());
        assertEquals("item", itemList.getLoopVar());
        // EnumItemModel 声明 4 个字段
        assertEquals(4, itemList.getChildren().size());
        // path 不含循环变量名前缀，前端按 kind 合成 field.xxx
        assertEquals("enumItemList.enumItemName", find(itemList.getChildren(), "enumItemName").getPath());
    }

    @Test
    void scalarListKeepsOneVirtualElementChild() {
        List<VariableNodeVO> nodeList = builder.build(TableModel.class);
        VariableNodeVO importList = find(nodeList, "importList");

        assertEquals(VariableKindEnum.SCALAR_LIST, importList.getKind());
        assertEquals("i", importList.getLoopVar());
        // Set 类型的字段要如实显示 Set<...>，不能被当成 List
        assertEquals("Set<String>", importList.getJavaType());
        assertEquals(1, importList.getChildren().size());
        assertEquals("String", importList.getChildren().getFirst().getJavaType());
    }

    @Test
    void nestedModelObjectDrillsDownWithoutLoopVar() {
        List<VariableNodeVO> nodeList = builder.build(TableModel.class);
        VariableNodeVO baseClass = find(nodeList, "entityBaseClass");

        assertEquals(VariableKindEnum.OBJECT, baseClass.getKind());
        assertEquals("", baseClass.getLoopVar());
        // BaseClassModel 声明 5 个字段
        assertEquals(5, baseClass.getChildren().size());
        assertEquals("entityBaseClass.fieldList", find(baseClass.getChildren(), "fieldList").getPath());
    }

    @Test
    void fiveTableModelFieldListsExpandIndependently() {
        List<VariableNodeVO> nodeList = builder.build(TableModel.class);
        Map<String, VariableNodeVO> nodeMap = nodeList.stream()
                .collect(Collectors.toMap(VariableNodeVO::getName, Function.identity()));

        // TableModel 声明 44 个字段（规格 7.1 第 1 条）
        assertEquals(44, nodeList.size());
        for (String listName : List.of("fieldList", "primaryList", "formList", "gridList", "queryList")) {
            assertEquals(34, nodeMap.get(listName).getChildren().size(), listName);
        }
        // 内容相同但 path 不同，不做子树共享（TableFieldModel 声明顺序首个字段是 id，故用 find 取 attrName）
        assertEquals("fieldList.attrName", find(nodeMap.get("fieldList").getChildren(), "attrName").getPath());
        assertEquals("formList.attrName", find(nodeMap.get("formList").getChildren(), "attrName").getPath());
    }

    @Test
    void fourLayerPathIsComposedAcrossNestedModelLists() {
        List<VariableNodeVO> nodeList = builder.build(ProjectModel.class);
        VariableNodeVO fieldList = find(find(nodeList, "tableModelList").getChildren(), "fieldList");
        VariableNodeVO attrName = find(fieldList.getChildren(), "attrName");

        // 插入语义的地基：跳两层集合的 path 逐层拼接（规格 7.1 第 2 条）
        assertEquals("tableModelList.fieldList.attrName", attrName.getPath());
    }

    @Test
    void enumModelDoesNotDeclareProjectNameScalars() {
        List<String> nameList = builder.build(EnumModel.class).stream().map(VariableNodeVO::getName).toList();

        // fillProjectCommon(EnumModel) 只填 Dot/Slash，类里确实没有这三个字段（规格 2.3）
        assertTrue(nameList.stream().noneMatch(name -> name.equals("projectName")
                || name.equals("projectNamePascal") || name.equals("projectNameUnderline")));
    }

    @Test
    void mapFieldExpandsToEmptyPlaceholder() {
        List<VariableNodeVO> nodeList = builder.build(TableModel.class);
        VariableNodeVO propertyData = find(nodeList, "tableTemplatePropertyData");

        // 属性 key 由 service 层按模板组填充，反射层只留空壳
        assertEquals(VariableKindEnum.MAP, propertyData.getKind());
        assertEquals("Map<String, Object>", propertyData.getJavaType());
        assertEquals(0, propertyData.getChildren().size());
    }

    @Test
    void mapNodesInsideNestedModelsAreAlsoDetected() {
        List<VariableNodeVO> nodeList = builder.build(ProjectModel.class);
        VariableNodeVO tableList = find(nodeList, "tableModelList");
        VariableNodeVO nestedTable = find(tableList.getChildren(), "tableTemplatePropertyData");

        assertEquals(VariableKindEnum.MAP, nestedTable.getKind());
        assertEquals("tableModelList.tableTemplatePropertyData", nestedTable.getPath());
    }

    @Test
    void buildingAllModelRootsNeverThrows() {
        // 循环引用与 raw type 防御靠真实模型图验证：7 个类均能构造出非空目录且不抛
        for (Class<?> modelClass : List.of(ProjectModel.class, TableModel.class, EnumModel.class,
                TableFieldModel.class, EnumItemModel.class, BaseClassModel.class, ModuleModel.class)) {
            assertFalse(new VariableCatalogBuilder().build(modelClass).isEmpty(), modelClass.getSimpleName());
        }
    }

    @Test
    void tableFieldModelAsRootHasNoChildren() {
        List<VariableNodeVO> nodeList = builder.build(TableFieldModel.class);

        // 34 个全标量字段，是最浅的一棵目录
        assertEquals(34, nodeList.size());
        assertTrue(nodeList.stream().allMatch(node -> node.getChildren().isEmpty()));
    }

    @Test
    void everyModelFieldCarriesVarDesc() {
        for (Class<?> modelClass : ALL_MODEL_CLASSES) {
            for (Field field : modelClass.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                VarDesc varDesc = field.getAnnotation(VarDesc.class);
                assertNotNull(varDesc, modelClass.getSimpleName() + "." + field.getName());
                assertFalse(varDesc.value().isBlank(), modelClass.getSimpleName() + "." + field.getName());
            }
        }
    }

    @Test
    void exactlyTwelveCollectionsCarryLoopVar() {
        List<String> actualList = new ArrayList<>();
        for (Class<?> modelClass : ALL_MODEL_CLASSES) {
            for (Field field : modelClass.getDeclaredFields()) {
                // 与构建器保持同一个字段总体：否则将来新增静态字段会让闸门假失败
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                VarDesc varDesc = field.getAnnotation(VarDesc.class);
                boolean isCollection = Collection.class.isAssignableFrom(field.getType());
                // 全部集合字段必须有 loopVar，非集合字段必须没有
                if (isCollection) {
                    assertFalse(varDesc.loopVar().isBlank(), modelClass.getSimpleName() + "." + field.getName());
                    actualList.add(modelClass.getSimpleName() + "." + field.getName() + "=" + varDesc.loopVar());
                } else {
                    assertEquals("", varDesc.loopVar(), modelClass.getSimpleName() + "." + field.getName());
                }
            }
        }
        assertEquals(List.of(
                "BaseClassModel.fieldList=fieldName",
                "EnumModel.enumItemList=item",
                "ProjectModel.enumModelList=enumModel",
                "ProjectModel.moduleList=module",
                "ProjectModel.tableModelList=table",
                "TableModel.fieldList=field",
                "TableModel.formList=formField",
                "TableModel.generatorFunction=funcCode",
                "TableModel.gridList=field",
                "TableModel.importList=i",
                "TableModel.primaryList=field",
                "TableModel.queryList=field"), actualList.stream().sorted().toList());
    }

    @Test
    void onlyConditionallyAssignedFieldsAreNullable() {
        List<String> actualList = new ArrayList<>();
        for (Class<?> modelClass : ALL_MODEL_CLASSES) {
            for (Field field : modelClass.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (field.getAnnotation(VarDesc.class).nullable()) {
                    actualList.add(modelClass.getSimpleName() + "." + field.getName());
                }
            }
        }
        // 与规格 2.4 表一致：4 个数据源字段 + 2 个基类 + 2 个模块 + 4 个枚举关联字段
        assertEquals(List.of(
                "ProjectModel.databaseDriverClassName",
                "ProjectModel.databasePassword",
                "ProjectModel.databaseUrl",
                "ProjectModel.databaseUsername",
                "TableFieldModel.enumId",
                "TableFieldModel.enumName",
                "TableFieldModel.enumNameAllUpper",
                "TableFieldModel.enumNamePascal",
                "TableModel.entityBaseClass",
                "TableModel.moduleDesc",
                "TableModel.modulePath",
                "TableModel.voBaseClass"), actualList.stream().sorted().toList());
    }
}
