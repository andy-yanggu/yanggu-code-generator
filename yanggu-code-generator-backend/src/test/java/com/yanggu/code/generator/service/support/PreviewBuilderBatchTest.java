package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TemplateEntity;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.model.EnumModel;
import com.yanggu.code.generator.domain.model.ProjectModel;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.ProjectFilter;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import com.yanggu.code.generator.mapstruct.TemplateMapstruct;
import com.yanggu.code.generator.service.EnumService;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TableService;
import com.yanggu.code.generator.service.TemplateGroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 项目全量预览批量段测试：
 * 批量枚举必须走带枚举项装配的查询（enumList），不能用只查主表的通用 list；
 * 未配置枚举模板组时枚举段静默跳过，不阻断其余产物。
 */
@ExtendWith(MockitoExtension.class)
class PreviewBuilderBatchTest {

    @Mock
    private TableService tableService;
    @Mock
    private ProjectService projectService;
    @Mock
    private TemplateGroupService templateGroupService;
    @Mock
    private EnumService enumService;
    @Mock
    private TemplateMapstruct templateMapstruct;
    @Mock
    private DataModelBuilder dataModelBuilder;
    @InjectMocks
    private PreviewBuilder previewBuilder;

    private ProjectEntity project(Long enumTemplateGroupId) {
        ProjectEntity project = new ProjectEntity();
        project.setId(100L);
        project.setProjectName("demo");
        project.setProjectTemplateGroupId(29L);
        project.setTableTemplateGroupId(1L);
        project.setEnumTemplateGroupId(enumTemplateGroupId);
        return project;
    }

    private TemplateGroupEntity group(int type, List<TemplateEntity> templateList) {
        TemplateGroupEntity group = new TemplateGroupEntity();
        group.setType(type);
        group.setTemplateList(templateList);
        return group;
    }

    private GeneratorQuery query(ProjectFilter filter) {
        GeneratorQuery query = new GeneratorQuery();
        query.setProductType(0);
        query.setProductIds(List.of(100L));
        query.setProjectFilter(filter);
        return query;
    }

    /** 表段置空、项目模板组置空叶子，让枚举段成为唯一产物来源 */
    private void stubEmptyTableAndProjectSegments(ProjectEntity project) {
        when(tableService.list(any())).thenReturn(List.of());
        when(templateGroupService.getById(29L, true)).thenReturn(group(0, List.of()));
        when(templateMapstruct.entityToVO(List.of())).thenReturn(List.of());
        when(dataModelBuilder.buildProjectDataModel(project)).thenReturn(new ProjectModel());
    }

    @Test
    void enumBatchUsesAssemblingQueryWithIdFilter() {
        ProjectEntity project = project(31L);
        when(projectService.getById(100L)).thenReturn(project);
        stubEmptyTableAndProjectSegments(project);

        EnumEntity enumEntity = new EnumEntity();
        enumEntity.setId(4L);
        enumEntity.setEnumName("status");
        enumEntity.setEnumItemList(List.of());
        when(enumService.enumList(100L, List.of(4L))).thenReturn(List.of(enumEntity));

        TemplateGroupEntity enumGroup = group(2, List.of(new TemplateEntity()));
        when(templateGroupService.getById(31L, true)).thenReturn(enumGroup);
        TemplateVO leaf = new TemplateVO();
        leaf.setId(221L);
        leaf.setParentId(0L);
        leaf.setFileName("Demo.java");
        leaf.setTemplateName("Demo.ftl");
        leaf.setTemplateType(1);
        leaf.setTemplateContent("hello");
        when(templateMapstruct.entityToVO(enumGroup.getTemplateList())).thenReturn(List.of(leaf));
        EnumModel enumModel = new EnumModel();
        enumModel.setEnumId(4L);
        when(dataModelBuilder.buildEnumDataModel(enumEntity, project)).thenReturn(enumModel);

        ProjectFilter filter = new ProjectFilter();
        filter.setEnumIds(List.of(4L));
        List<TemplateContentVO> result = previewBuilder.fullProjectPreview(query(filter));

        assertEquals(1, result.size());
        assertEquals("Demo.java", result.getFirst().getFileName());
        assertEquals(4L, result.getFirst().getEnumId());
        verify(enumService).enumList(100L, List.of(4L));
        verify(enumService, never()).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }

    @Test
    void missingEnumTemplateGroupSkipsEnumSegmentSilently() {
        ProjectEntity project = project(null);
        when(projectService.getById(100L)).thenReturn(project);
        stubEmptyTableAndProjectSegments(project);

        List<TemplateContentVO> result = previewBuilder.fullProjectPreview(query(null));

        assertTrue(result.isEmpty());
        verify(enumService, never()).enumList(anyLong(), anyList());
    }
}