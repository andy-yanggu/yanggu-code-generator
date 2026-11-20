package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.model.EnumModel;
import com.yanggu.code.generator.domain.model.ProjectModel;
import com.yanggu.code.generator.domain.model.TableModel;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        ProjectEntity project = new ProjectEntity();
        project.setProjectName("demo");
        when(projectService.getById(100L)).thenReturn(project);
        when(previewBuilder.projectPreview(project, q.getTemplateIds())).thenReturn(List.of(vo("CorsConfig.java")));
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
        when(previewBuilder.buildProjectPreview(q)).thenReturn(List.of(vo("P.java")));
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
        DatasourceEntity datasource = new DatasourceEntity();
        ProjectModel model = new ProjectModel();
        when(dataModelBuilder.resolveDatasource(project)).thenReturn(datasource);
        when(dataModelBuilder.buildProjectDataModel(project, datasource)).thenReturn(model);
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
}