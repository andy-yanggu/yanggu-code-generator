package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.model.EnumModel;
import com.yanggu.code.generator.domain.model.ProjectModel;
import com.yanggu.code.generator.domain.model.TableModel;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.CascaderDataVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.EnumService;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TableService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductGeneratorTest {

    @Mock
    private PreviewBuilder previewBuilder;
    @Mock
    private DataModelBuilder dataModelBuilder;
    @Mock
    private TableService tableService;
    @Mock
    private EnumService enumService;
    @Mock
    private ProjectService projectService;
    @InjectMocks
    private TableGenerator tableGenerator;
    @InjectMocks
    private EnumGenerator enumGenerator;
    @InjectMocks
    private ProjectGenerator projectGenerator;

    private GeneratorQuery query(int type, List<Long> ids) {
        GeneratorQuery q = new GeneratorQuery();
        q.setProductType(type);
        q.setProductIds(ids);
        q.setTemplateIds(List.of(11L));
        return q;
    }

    private TemplateContentVO vo(String fileName) {
        TemplateContentVO v = new TemplateContentVO();
        v.setFileName(fileName);
        return v;
    }

    @Test
    void tableGenerateLoopsAllProducts() {
        GeneratorQuery q = query(1, List.of(201L, 202L));
        when(previewBuilder.tablePreview(201L, q.getTemplateIds())).thenReturn(List.of(vo("A.java")));
        when(previewBuilder.tablePreview(202L, q.getTemplateIds())).thenReturn(List.of(vo("B.java")));
        List<TemplateContentVO> result = tableGenerator.generate(q);
        assertEquals(List.of("A.java", "B.java"), result.stream().map(TemplateContentVO::getFileName).toList());
        assertEquals(TemplateGroupTypeEnum.TABLE, tableGenerator.type());
    }

    @Test
    void tableArtifactNameJoinsTableNames() {
        TableEntity t1 = new TableEntity();
        t1.setTableName("sys_user");
        TableEntity t2 = new TableEntity();
        t2.setTableName("sys_role");
        when(tableService.listByIds(List.of(201L, 202L))).thenReturn(List.of(t1, t2));
        assertEquals("sys_user_sys_role", tableGenerator.artifactName(query(1, List.of(201L, 202L))));
    }

    @Test
    void enumGenerateLoopsAllProducts() {
        GeneratorQuery q = query(2, List.of(301L, 302L));
        when(previewBuilder.enumPreview(301L, q.getTemplateIds())).thenReturn(List.of(vo("E1.java")));
        when(previewBuilder.enumPreview(302L, q.getTemplateIds())).thenReturn(List.of(vo("E2.java")));
        assertEquals(List.of("E1.java", "E2.java"),
                enumGenerator.generate(q).stream().map(TemplateContentVO::getFileName).toList());
        assertEquals(TemplateGroupTypeEnum.ENUM, enumGenerator.type());
    }

    @Test
    void enumArtifactNameJoinsEnumNames() {
        EnumEntity e1 = new EnumEntity();
        e1.setEnumName("status");
        EnumEntity e2 = new EnumEntity();
        e2.setEnumName("gender");
        when(enumService.listByIds(List.of(301L, 302L))).thenReturn(List.of(e1, e2));
        assertEquals("status_gender", enumGenerator.artifactName(query(2, List.of(301L, 302L))));
    }

    @Test
    void projectGenerateSingleRendersOnlyProjectTemplates() {
        GeneratorQuery q = query(0, List.of(100L));
        // projectPreview 入参统一为 projectId，ProjectGenerator 不再需要预加载 ProjectEntity
        when(previewBuilder.projectPreview(100L, q.getTemplateIds())).thenReturn(List.of(vo("CorsConfig.java")));
        assertEquals("CorsConfig.java", projectGenerator.generateSingle(q).getFileName());
    }

    @Test
    void tableGenerateSingleDelegatesToGenerate() {
        GeneratorQuery q = query(1, List.of(201L));
        when(previewBuilder.tablePreview(201L, q.getTemplateIds())).thenReturn(List.of(vo("A.java")));
        assertEquals("A.java", tableGenerator.generateSingle(q).getFileName());
    }

    @Test
    void projectDelegatesAndNamesByProject() {
        GeneratorQuery q = query(0, List.of(100L));
        when(previewBuilder.fullProjectPreview(q)).thenReturn(List.of(vo("P.java")));
        assertEquals(1, projectGenerator.generate(q).size());
        ProjectEntity project = new ProjectEntity();
        project.setProjectName("demo");
        when(projectService.getById(100L)).thenReturn(project);
        assertEquals("demo", projectGenerator.artifactName(q));
        assertEquals(TemplateGroupTypeEnum.PROJECT, projectGenerator.type());
    }

    @Test
    void projectTestDataModelUsesResolvedDatasource() {
        ProjectEntity project = new ProjectEntity();
        ProjectModel model = new ProjectModel();
        when(dataModelBuilder.buildProjectDataModel(project)).thenReturn(model);
        assertSame(model, projectGenerator.testDataModel(new TemplateTestQuery(), project));
    }

    @Test
    void tableTestDataModelLoadsTestTable() {
        TemplateTestQuery q = new TemplateTestQuery();
        q.setTestId(201L);
        ProjectEntity project = new ProjectEntity();
        TableEntity table = new TableEntity();
        TableModel model = new TableModel();
        when(tableService.getById(201L)).thenReturn(table);
        when(dataModelBuilder.buildTableDataModel(table, project)).thenReturn(model);
        assertSame(model, tableGenerator.testDataModel(q, project));
    }

    @Test
    void enumTestDataModelLoadsTestEnum() {
        TemplateTestQuery q = new TemplateTestQuery();
        q.setTestId(301L);
        ProjectEntity project = new ProjectEntity();
        EnumEntity enumEntity = new EnumEntity();
        EnumModel model = new EnumModel();
        when(enumService.getById(301L)).thenReturn(enumEntity);
        when(dataModelBuilder.buildEnumDataModel(enumEntity, project)).thenReturn(model);
        assertSame(model, enumGenerator.testDataModel(q, project));
    }

    // ==================== 候选产物级联数据 ====================

    private ProjectEntity project(Long id, Integer generatorType) {
        ProjectEntity project = new ProjectEntity();
        project.setId(id);
        project.setGeneratorType(generatorType);
        return project;
    }

    private TableEntity table(Long id, Long projectId, String tableName) {
        TableEntity table = new TableEntity();
        table.setId(id);
        table.setProjectId(projectId);
        table.setTableName(tableName);
        return table;
    }

    @SuppressWarnings("unchecked")
    @Test
    void tableCascaderGroupsChildrenByProjectInSingleQuery() {
        // 100 有两张表、101 一张也没有：101 不作为候选
        when(projectService.listByTableTemplateGroupId(7L)).thenReturn(List.of(project(100L, 1), project(101L, 0)));
        when(tableService.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class)))
                .thenReturn(List.of(table(201L, 100L, "sys_user"), table(202L, 100L, "sys_role")));

        List<CascaderDataVO> nodeList = tableGenerator.cascaderData(7L);

        // 两个项目只触发一次表查询：逐项目查询的 N+1 不得回退
        verify(tableService, times(1)).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        assertEquals(1, nodeList.size());
        assertEquals("project_100", nodeList.getFirst().getValue());
        // value 前缀与 type 同值，前端按 '_' 反解取 ID，改名即破坏契约
        CascaderDataVO tableNode = nodeList.getFirst().getChildren().getFirst();
        assertEquals("table", tableNode.getType());
        assertEquals("table_201", tableNode.getValue());
        assertEquals(Integer.valueOf(1), tableNode.getGeneratorType());
    }

    @Test
    void projectCascaderKeepsProjectsWithoutChildren() {
        // 项目模板组的候选就是项目本身，无子级可过滤，children 保持不下发
        when(projectService.listByProjectTemplateGroupId(7L)).thenReturn(List.of(project(100L, 1), project(101L, 0)));

        List<CascaderDataVO> nodeList = projectGenerator.cascaderData(7L);

        assertEquals(2, nodeList.size());
        assertNull(nodeList.getFirst().getChildren());
    }

    @Test
    void invalidTemplateGroupTypeCodeFailsFast() {
        // 路由组件走字段注入 + @PostConstruct（避开与 TemplateGroupService 之间的构造注入环），测试里手动装配
        ProductGenerators productGenerators = new ProductGenerators();
        ReflectionTestUtils.setField(productGenerators, "productGenerators", List.of(tableGenerator, enumGenerator, projectGenerator));
        productGenerators.initGeneratorMap();
        assertSame(tableGenerator, productGenerators.getByCode(1));
        // EnumUtil.getBy 未命中返回 null，交给 switch 只会 NPE，路由必须给明确业务异常
        assertThrows(BusinessException.class, () -> productGenerators.getByCode(9));
    }
}