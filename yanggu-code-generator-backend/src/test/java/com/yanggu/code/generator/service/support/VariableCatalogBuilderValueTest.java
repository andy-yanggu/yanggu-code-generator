package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.model.BaseClassModel;
import com.yanggu.code.generator.domain.model.EnumItemModel;
import com.yanggu.code.generator.domain.model.EnumModel;
import com.yanggu.code.generator.domain.model.ProjectModel;
import com.yanggu.code.generator.domain.model.TableFieldModel;
import com.yanggu.code.generator.domain.model.TableModel;
import com.yanggu.code.generator.domain.vo.VariableNodeVO;
import com.yanggu.code.generator.enums.VariableKindEnum;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 带值目录（build(Class, Object) 重载）单测：纯反射直接 new，不起 Spring（规格 7.1）
 */
class VariableCatalogBuilderValueTest {

    private final VariableCatalogBuilder builder = new VariableCatalogBuilder();

    private VariableNodeVO find(List<VariableNodeVO> nodeList, String name) {
        return nodeList.stream().filter(item -> name.equals(item.getName())).findFirst().orElse(null);
    }

    @Test
    void scalarValueIsFilledFromInstance() {
        TableModel tableModel = new TableModel();
        tableModel.setProjectName("炎牛");
        tableModel.setVersion("1.0.0");
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, tableModel);

        assertEquals("炎牛", find(nodeList, "projectName").getValue());
        assertEquals("1.0.0", find(nodeList, "version").getValue());
    }

    @Test
    void nullScalarShowsPlaceholder() {
        // tableName 未赋值：值文本必须是 (null) 而不是空串，和"有值但空串"区分开
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, new TableModel());
        assertEquals("(null)", find(nodeList, "tableName").getValue());
    }

    @Test
    void emptyStringScalarIsNotConfusedWithNull() {
        TableModel tableModel = new TableModel();
        tableModel.setTableName("");
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, tableModel);
        assertEquals("", find(nodeList, "tableName").getValue());
    }

    @Test
    void sensitiveScalarIsMasked() {
        ProjectModel projectModel = new ProjectModel();
        projectModel.setDatabasePassword("123456");
        projectModel.setDatabaseUrl("jdbc:mysql://10.0.0.1:3306/x");
        List<VariableNodeVO> nodeList = builder.build(ProjectModel.class, projectModel);

        assertEquals("******", find(nodeList, "databasePassword").getValue());
        assertEquals("******", find(nodeList, "databaseUrl").getValue());
    }

    @Test
    void nullSensitiveScalarStaysNullPlaceholderNotMask() {
        // 未绑定数据源时 databaseUsername 为 null：必须显示 (null)，不能伪装成有值被掩码
        List<VariableNodeVO> nodeList = builder.build(ProjectModel.class, new ProjectModel());
        assertEquals("(null)", find(nodeList, "databaseUsername").getValue());
    }

    @Test
    void modelListExpandsActualElementsWithCount() {
        TableModel tableModel = new TableModel();
        TableFieldModel idField = new TableFieldModel();
        idField.setAttrName("id");
        TableFieldModel nameField = new TableFieldModel();
        nameField.setAttrName("userName");
        TableFieldModel sortField = new TableFieldModel();
        sortField.setAttrName("sort");
        tableModel.setFieldList(List.of(idField, nameField, sortField));
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, tableModel);
        VariableNodeVO fieldList = find(nodeList, "fieldList");

        // 策略 B：父节点携条数文本，默认收起由前端控制
        assertEquals("共 3 条", fieldList.getValue());
        assertEquals(3, fieldList.getChildren().size());
        // 元素名 = 循环变量名 + 下标（规格 2.8 既有约定 field）
        assertEquals("field[0]", fieldList.getChildren().get(0).getName());
        assertEquals("field[2]", fieldList.getChildren().get(2).getName());
        assertEquals(VariableKindEnum.OBJECT, fieldList.getChildren().getFirst().getKind());
        // 元素子级是"该元素类型结构 + 该元素实际值"：34 个字段全展开
        VariableNodeVO element = fieldList.getChildren().getFirst();
        assertEquals(34, element.getChildren().size());
        assertEquals("id", find(element.getChildren(), "attrName").getValue());
        assertEquals("userName", find(fieldList.getChildren().get(1).getChildren(), "attrName").getValue());
    }

    @Test
    void nullModelListShowsPlaceholder() {
        // DataModelBuilder 对未装配的集合可能留 null：显示 (null) 而不是"共 0 条"
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, new TableModel());
        VariableNodeVO fieldList = find(nodeList, "fieldList");
        assertEquals("(null)", fieldList.getValue());
        assertTrue(fieldList.getChildren().isEmpty());
    }

    @Test
    void emptyModelListShowsZeroCount() {
        TableModel tableModel = new TableModel();
        tableModel.setFieldList(List.of());
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, tableModel);
        assertEquals("共 0 条", find(nodeList, "fieldList").getValue());
    }

    @Test
    void scalarListExpandsActualElements() {
        TableModel tableModel = new TableModel();
        tableModel.setImportList(Set.of("a", "b", "c"));
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, tableModel);
        VariableNodeVO importList = find(nodeList, "importList");

        assertEquals("共 3 条", importList.getValue());
        assertEquals(3, importList.getChildren().size());
        // 标量集合的元素行不带循环变量名前缀，只有下标
        assertEquals("[0]", importList.getChildren().getFirst().getName());
        assertEquals(VariableKindEnum.SCALAR, importList.getChildren().getFirst().getKind());
        assertNotNull(importList.getChildren().getFirst().getValue());
    }

    @Test
    void objectValueDrillsIntoInstanceWithoutNullValue() {
        TableModel tableModel = new TableModel();
        BaseClassModel entityBaseClass = new BaseClassModel();
        entityBaseClass.setPackageName("com.baomidou.mybatisplus.extension.activerecord");
        entityBaseClass.setClassName("Model");
        tableModel.setEntityBaseClass(entityBaseClass);
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, tableModel);
        VariableNodeVO baseClass = find(nodeList, "entityBaseClass");

        // OBJECT 容器自身留空值，子级带实际值
        assertNull(baseClass.getValue());
        assertEquals("com.baomidou.mybatisplus.extension.activerecord", find(baseClass.getChildren(), "packageName").getValue());
        assertEquals("Model", find(baseClass.getChildren(), "className").getValue());
    }

    @Test
    void nullObjectShowsPlaceholderAndNoChildren() {
        // entityBaseClass 未配置基类时为 null：只留占位行，不展开 5 个字段的空值树
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, new TableModel());
        VariableNodeVO baseClass = find(nodeList, "entityBaseClass");
        assertEquals("(null)", baseClass.getValue());
        assertTrue(baseClass.getChildren().isEmpty());
    }

    @Test
    void mapExpandsInstanceEntries() {
        TableModel tableModel = new TableModel();
        Map<String, Object> propertyData = new LinkedHashMap<>();
        propertyData.put("author", "yanggu");
        propertyData.put("createTime", null);
        tableModel.setTableTemplatePropertyData(propertyData);
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, tableModel);
        VariableNodeVO propertyNode = find(nodeList, "tableTemplatePropertyData");

        assertEquals(VariableKindEnum.MAP, propertyNode.getKind());
        assertEquals(2, propertyNode.getChildren().size());
        VariableNodeVO author = find(propertyNode.getChildren(), "author");
        assertEquals("tableTemplatePropertyData.author", author.getPath());
        assertEquals("yanggu", author.getValue());
        assertEquals(Boolean.FALSE, author.getNullable());
        // map 里显式存 null：值显示占位且打可空标记
        VariableNodeVO createTime = find(propertyNode.getChildren(), "createTime");
        assertEquals("(null)", createTime.getValue());
        assertEquals(Boolean.TRUE, createTime.getNullable());
    }

    @Test
    void nullMapShowsPlaceholder() {
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, new TableModel());
        VariableNodeVO propertyNode = find(nodeList, "tableTemplatePropertyData");
        assertEquals("(null)", propertyNode.getValue());
        assertTrue(propertyNode.getChildren().isEmpty());
    }

    @Test
    void crossGroupMapIsAlsoExpandedFromInstance() {
        // 表模板里的 projectTemplatePropertyData：结构目录按决策 4 不展开，带值目录按实例实际有什么就展示什么
        TableModel tableModel = new TableModel();
        Map<String, Object> projectProperty = new LinkedHashMap<>();
        projectProperty.put("backendPathPrefix", "yanggu");
        tableModel.setProjectTemplatePropertyData(projectProperty);
        List<VariableNodeVO> nodeList = builder.build(TableModel.class, tableModel);

        assertEquals("yanggu", find(find(nodeList, "projectTemplatePropertyData").getChildren(), "backendPathPrefix").getValue());
    }

    @Test
    void valueCatalogNeverCarriesVarDescMetaLoss() {
        // 带值目录与结构目录的元信息（kind/desc/loopVar/nullable）逐字段一致，只是多了 value
        EnumModel enumModel = new EnumModel();
        EnumItemModel item = new EnumItemModel();
        item.setEnumItemName("YES");
        enumModel.setEnumItemList(List.of(item));
        List<VariableNodeVO> valueList = builder.build(EnumModel.class, enumModel);
        List<VariableNodeVO> structList = builder.build(EnumModel.class);

        for (int i = 0; i < structList.size(); i++) {
            VariableNodeVO struct = structList.get(i);
            VariableNodeVO value = valueList.get(i);
            assertEquals(struct.getName(), value.getName());
            assertEquals(struct.getKind(), value.getKind());
            assertEquals(struct.getDesc(), value.getDesc());
            assertEquals(struct.getLoopVar(), value.getLoopVar());
            assertEquals(struct.getNullable(), value.getNullable());
        }
    }

    @Test
    void structureCatalogKeepsValueNull() {
        // 旧 build(Class) 路径不得被带值逻辑污染：所有节点 value 必须为 null（回归闸门）
        List<VariableNodeVO> nodeList = builder.build(ProjectModel.class);
        assertTrue(nodeList.stream().allMatch(node -> node.getValue() == null));
        VariableNodeVO tableList = find(nodeList, "tableModelList");
        // 结构目录的 MODEL_LIST 仍是"元素类型子树"而非实际元素
        assertEquals(44, tableList.getChildren().size());
        assertEquals("tableModelList.projectId", find(tableList.getChildren(), "projectId").getPath());
    }
}
