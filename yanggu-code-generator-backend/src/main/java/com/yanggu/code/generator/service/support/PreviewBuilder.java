package com.yanggu.code.generator.service.support;

import cn.hutool.v7.core.collection.CollUtil;
import cn.hutool.v7.core.text.StrUtil;
import cn.hutool.v7.core.util.EnumUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.model.EnumModel;
import com.yanggu.code.generator.domain.model.ProjectModel;
import com.yanggu.code.generator.domain.model.TableModel;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.ProjectFilter;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import com.yanggu.code.generator.enums.TemplateTypeEnum;
import com.yanggu.code.generator.mapstruct.TemplateMapstruct;
import com.yanggu.code.generator.service.EnumService;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TableService;
import com.yanggu.code.generator.service.TemplateGroupService;
import com.yanggu.code.generator.util.AviatorUtil;
import com.yanggu.code.generator.util.TemplateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 生成器预览编排器
 * 编排项目/表/枚举三种产品维度的模板预览构建，并统一收口渲染链：
 * 模板树路径分配 → 模板 ID 过滤 → 条件表达式过滤 → 单模板渲染，
 * 消除原 tablePreview/enumPreview/projectPreview/enumListPreview/templateTest 五处重复链路。
 *
 * <p>可见性约定：
 * <ul>
 *   <li>{@link #checkCondition}、{@link #render} 保留 public — 供 {@code GeneratorServiceImpl#templateTest} 及单测直调；</li>
 *   <li>产品入口（fullProjectPreview / tablePreview / enumPreview / projectPreview）public — 供 ProductGenerator 分派；</li>
 *   <li>其他均为内部辅助，private。</li>
 * </ul>
 */
@Component
public class PreviewBuilder {

    @Autowired
    private TableService tableService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TemplateGroupService templateGroupService;

    @Autowired
    private EnumService enumService;

    @Autowired
    private TemplateMapstruct templateMapstruct;

    @Autowired
    private DataModelBuilder dataModelBuilder;

    // ==================== public：渲染基础设施 ====================

    /**
     * 校验模板的条件表达式（空表达式视为通过）
     */
    public boolean checkCondition(String expression, Object dataModel) {
        if (StrUtil.isBlank(expression)) {
            return true;
        }
        return (Boolean) AviatorUtil.execute(AviatorUtil.compile(expression), dataModel);
    }

    /**
     * 渲染单个模板为内容 VO
     */
    public TemplateContentVO render(Integer templateGroupType, TemplateVO template, Object dataModel) {
        String templateName = template.getTemplateName();
        Integer templateType = template.getTemplateType();
        String templateContent = template.getTemplateContent();
        String fileContent = switch (EnumUtil.getBy(TemplateTypeEnum::getCode, templateType)) {
            // 目录节点无内容
            case DIRECTORY -> "";
            // 模板文件：用 freemarker 渲染成文本
            case TEMPLATE_FILE -> TemplateUtil.renderTemplate(templateName, templateContent, dataModel);
            // 二进制文件：直传原文
            case BINARY_FILE -> templateContent;
        };
        // 路径也需要按数据模型渲染一次（占位符替换）
        String filePath = TemplateUtil.renderTemplate(templateName, template.getGeneratorPath(), dataModel);
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        TemplateContentVO vo = new TemplateContentVO();
        vo.setContent(fileContent);
        vo.setFileName(fileName);
        vo.setFilePath(filePath);
        vo.setTemplateId(template.getId());
        vo.setTemplateGroupType(templateGroupType);
        vo.setTemplateType(templateType);
        return vo;
    }

    // ==================== public：产品维度入口 ====================

    /**
     * 构建项目维度的完整预览（表模板 + 枚举模板 + 项目模板）
     */
    public List<TemplateContentVO> fullProjectPreview(GeneratorQuery query) {
        Long projectId = query.getProductIds().getFirst();
        ProjectEntity project = projectService.getById(projectId);
        ProjectFilter filter = query.getProjectFilter();
        List<Long> tableIdList = filterValue(filter, ProjectFilter::getTableIds);
        List<Long> enumIdList = filterValue(filter, ProjectFilter::getEnumIds);
        List<Long> tableTemplateIdList = filterValue(filter, ProjectFilter::getTableTemplateIds);
        List<Long> enumTemplateIdList = filterValue(filter, ProjectFilter::getEnumTemplateIds);

        List<TemplateContentVO> allPreviewList = new ArrayList<>();
        allPreviewList.addAll(tableListPreview(project, tableIdList, tableTemplateIdList));
        allPreviewList.addAll(enumListPreview(project, enumIdList, enumTemplateIdList));
        allPreviewList.addAll(doProjectPreview(project, query.getTemplateIds()));
        return allPreviewList;
    }

    /**
     * 构建单表预览
     */
    public List<TemplateContentVO> tablePreview(Long tableId, List<Long> templateIds) {
        TableEntity table = tableService.getById(tableId);
        ProjectEntity project = projectService.getById(table.getProjectId());
        // 单品生成：表已显式指定，模板组必配，未配直接报错
        TemplateGroupEntity templateGroup = requireTemplateGroup(project, project.getTableTemplateGroupId(), "表模板组");

        TableModel tableModel = dataModelBuilder.buildTableDataModel(table, project);
        return renderGroupTemplates(templateGroup, templateIds, tableModel, vo -> vo.setTableId(tableId));
    }

    /**
     * 构建单枚举预览
     */
    public List<TemplateContentVO> enumPreview(Long enumId, List<Long> templateIds) {
        EnumEntity enumEntity = enumService.getById(enumId);
        ProjectEntity project = projectService.getById(enumEntity.getProjectId());
        // 单品生成：枚举已显式指定，模板组必配，未配直接报错
        TemplateGroupEntity templateGroup = requireTemplateGroup(project, project.getEnumTemplateGroupId(), "枚举模板组");

        EnumModel enumModel = dataModelBuilder.buildEnumDataModel(enumEntity, project);
        return renderGroupTemplates(templateGroup, templateIds, enumModel, vo -> vo.setEnumId(enumId));
    }

    /**
     * 构建项目自身模板预览（对外入口，与 table/enum 保持入参一致：传 ID）
     */
    public List<TemplateContentVO> projectPreview(Long projectId, List<Long> templateIdList) {
        return doProjectPreview(projectService.getById(projectId), templateIdList);
    }

    // ==================== private：渲染链基础设施 ====================

    /**
     * 获取模板组内配置了生成路径的叶子模板列表（可按模板 ID 过滤）
     */
    private List<TemplateVO> leafTemplates(TemplateGroupEntity templateGroup, List<Long> templateIdFilter) {
        List<TemplateVO> templateList = TemplatePathResolver.assignGeneratorPaths(
                templateMapstruct.entityToVO(templateGroup.getTemplateList()));
        if (CollUtil.isNotEmpty(templateIdFilter)) {
            templateList = templateList.stream()
                    .filter(template -> templateIdFilter.contains(template.getId()))
                    .toList();
        }
        return templateList;
    }

    /**
     * 叶子模板列表在给定数据模型上的渲染链：条件过滤 → 渲染 → 附加字段填充。
     * 批量场景（多枚举共用一份叶子）直接复用，避免每条目都重复查模板组与重算路径。
     */
    private List<TemplateContentVO> renderLeafTemplates(Integer templateGroupType, List<TemplateVO> leaves,
                                                        Object dataModel, Consumer<TemplateContentVO> contentFiller) {
        return leaves.stream()
                .filter(template -> checkCondition(template.getConditionExpression(), dataModel))
                .map(template -> {
                    TemplateContentVO vo = render(templateGroupType, template, dataModel);
                    if (contentFiller != null) {
                        contentFiller.accept(vo);
                    }
                    return vo;
                })
                .toList();
    }

    /**
     * 单数据模型 + 模板组的完整渲染链：leafTemplates → renderLeafTemplates
     */
    private List<TemplateContentVO> renderGroupTemplates(
            TemplateGroupEntity templateGroup,
            List<Long> templateIdFilter,
            Object dataModel,
            Consumer<TemplateContentVO> contentFiller) {
        return renderLeafTemplates(templateGroup.getType(),
                leafTemplates(templateGroup, templateIdFilter), dataModel, contentFiller);
    }

    // ==================== private：产品预览实现 ====================

    /**
     * 项目预览的内部实现，datasource 由 DataModelBuilder 内部按需解析
     */
    private List<TemplateContentVO> doProjectPreview(ProjectEntity project, List<Long> templateIdList) {
        TemplateGroupEntity templateGroup = requireTemplateGroup(project, project.getProjectTemplateGroupId(), "项目模板组");
        ProjectModel projectModel = dataModelBuilder.buildProjectDataModel(project);
        return renderGroupTemplates(templateGroup, templateIdList, projectModel, null);
    }

    /**
     * 多表预览：模板组与叶子路径只计算一次，逐表复用 {@link #renderLeafTemplates}
     */
    private List<TemplateContentVO> tableListPreview(ProjectEntity project, List<Long> tableIdList, List<Long> tableTemplateIdList) {
        LambdaQueryWrapper<TableEntity> wrapper = Wrappers.<TableEntity>lambdaQuery()
                .eq(TableEntity::getProjectId, project.getId())
                .in(CollUtil.isNotEmpty(tableIdList), TableEntity::getId, tableIdList);
        List<TableEntity> tableList = tableService.list(wrapper);
        if (tableList.isEmpty()) {
            return List.of();
        }

        TemplateGroupEntity templateGroup = requireTemplateGroup(project, project.getTableTemplateGroupId(), "表模板组");
        Integer groupType = templateGroup.getType();
        List<TemplateVO> leaves = leafTemplates(templateGroup, tableTemplateIdList);

        return tableList.stream()
                .flatMap(table -> {
                    TableModel tableModel = dataModelBuilder.buildTableDataModel(table, project);
                    Long tableId = table.getId();
                    return renderLeafTemplates(groupType, leaves, tableModel,
                            vo -> vo.setTableId(tableId)).stream();
                })
                .toList();
    }

    /**
     * 多枚举预览：模板组与叶子路径只计算一次，逐枚举复用 {@link #renderLeafTemplates}。
     * 枚举查询必须走 enumList（JOIN 装配枚举项），通用 list 只查主表会导致枚举项缺失；
     * 项目下无枚举时跳过枚举段，有枚举但未配置枚举模板组则报错，与多表预览保持一致。
     */
    private List<TemplateContentVO> enumListPreview(ProjectEntity project, List<Long> enumIdList, List<Long> enumTemplateIdList) {
        List<EnumEntity> enumList = enumService.enumList(project.getId(), enumIdList);
        if (enumList.isEmpty()) {
            return List.of();
        }

        TemplateGroupEntity templateGroup = requireTemplateGroup(project, project.getEnumTemplateGroupId(), "枚举模板组");
        Integer groupType = templateGroup.getType();
        List<TemplateVO> leaves = leafTemplates(templateGroup, enumTemplateIdList);

        return enumList.stream()
                .map(enumEntity -> dataModelBuilder.buildEnumDataModel(enumEntity, project))
                .flatMap(enumModel -> renderLeafTemplates(groupType, leaves, enumModel,
                        vo -> vo.setEnumId(enumModel.getEnumId())).stream())
                .toList();
    }

    // ==================== private：通用辅助 ====================

    /**
     * 加载模板组；未配置时按项目上下文抛出明确业务异常，
     * 避免深入 selectById(null) 报出含糊错误或 NPE
     */
    private TemplateGroupEntity requireTemplateGroup(ProjectEntity project, Long templateGroupId, String label) {
        if (templateGroupId == null) {
            throw new BusinessException("项目[" + project.getProjectName() + "]未配置" + label);
        }
        return templateGroupService.getById(templateGroupId, true);
    }

    /**
     * ProjectFilter 为 null 或字段为 null 时统一兜底为空列表，避免展开 4 段 Optional 样板
     */
    private static List<Long> filterValue(ProjectFilter filter, Function<ProjectFilter, List<Long>> getter) {
        if (filter == null) {
            return List.of();
        }
        List<Long> value = getter.apply(filter);
        return value == null ? List.of() : value;
    }
}
