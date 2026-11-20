package com.yanggu.code.generator.service.support;

import cn.hutool.v7.core.collection.CollUtil;
import cn.hutool.v7.core.text.StrUtil;
import cn.hutool.v7.core.util.EnumUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yanggu.code.generator.domain.entity.DatasourceEntity;
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
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * 生成器预览编排器
 *
 * 编排项目/表/枚举三种产品维度的模板预览构建，并统一收口渲染链：
 * 模板树路径分配 → 模板 ID 过滤 → 条件表达式过滤 → 单模板渲染，
 * 消除原 tablePreview/enumPreview/projectPreview/enumListPreview/templateTest 五处重复链路。
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

    /**
     * 获取模板组内配置了生成路径的叶子模板列表（可按模板 ID 过滤）
     */
    public List<TemplateVO> leafTemplates(TemplateGroupEntity templateGroup, List<Long> templateIdFilter) {
        List<TemplateVO> templateList = TemplatePathResolver.assignGeneratorPaths(templateMapstruct.entityToVO(templateGroup.getTemplateList()));
        if (CollUtil.isNotEmpty(templateIdFilter)) {
            templateList = templateList.stream()
                    .filter(template -> templateIdFilter.contains(template.getId()))
                    .toList();
        }
        return templateList;
    }

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
    public TemplateContentVO render(TemplateGroupEntity templateGroup, TemplateVO template, Object dataModel) {
        String templateName = template.getTemplateName();
        Integer templateType = template.getTemplateType();
        String templateContent = template.getTemplateContent();
        String fileContent;
        switch (EnumUtil.getBy(TemplateTypeEnum::getCode, templateType)) {
            case DIRECTORY -> fileContent = "";
            // 渲染成文件内容
            case TEMPLATE_FILE -> fileContent = TemplateUtil.renderTemplate(templateName, templateContent, dataModel);
            // 二进制文件
            case BINARY_FILE -> fileContent = templateContent;
            default -> throw new RuntimeException("未知模板类型");
        }
        //路径
        String filePath = TemplateUtil.renderTemplate(templateName, template.getGeneratorPath(), dataModel);
        //文件名
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        TemplateContentVO templateContentVO = new TemplateContentVO();
        templateContentVO.setContent(fileContent);
        templateContentVO.setFileName(fileName);
        templateContentVO.setFilePath(filePath);
        templateContentVO.setTemplateId(template.getId());
        templateContentVO.setTemplateGroupType(templateGroup.getType());
        templateContentVO.setTemplateType(templateType);
        return templateContentVO;
    }

    /**
     * 统一渲染链：叶子模板 → ID 过滤 → 条件表达式过滤 → 渲染 → 附加字段填充
     *
     * @param templateGroup  模板组
     * @param templateIdFilter 模板 ID 过滤（空表示不过滤）
     * @param dataModel      渲染数据模型
     * @param contentFiller  渲染结果附加填充（可 null）
     * @return 渲染内容列表
     */
    public List<TemplateContentVO> renderGroupTemplates(TemplateGroupEntity templateGroup, List<Long> templateIdFilter,
                                                        Object dataModel, Consumer<TemplateContentVO> contentFiller) {
        return leafTemplates(templateGroup, templateIdFilter).stream()
                .filter(template -> checkCondition(template.getConditionExpression(), dataModel))
                .map(template -> {
                    TemplateContentVO vo = render(templateGroup, template, dataModel);
                    if (contentFiller != null) {
                        contentFiller.accept(vo);
                    }
                    return vo;
                })
                .toList();
    }

    /**
     * 构建项目维度的完整预览（表模板 + 枚举模板 + 项目模板）
     */
    public List<TemplateContentVO> buildProjectPreview(GeneratorQuery query) {
        Long projectId = query.getProductIds().getFirst();
        //查询项目
        ProjectEntity project = projectService.getById(projectId);
        ProjectFilter filter = query.getProjectFilter();
        List<Long> tableIdList = filter == null ? null : filter.getTableIds();
        List<Long> enumIdList = filter == null ? null : filter.getEnumIds();
        List<Long> tableTemplateIdList = filter == null ? null : filter.getTableTemplateIds();
        List<Long> enumTemplateIdList = filter == null ? null : filter.getEnumTemplateIds();

        List<TemplateContentVO> allPreviewList = new ArrayList<>();

        //获取表预览数据
        //查询该项目下的表
        List<TableEntity> tableList = tableService.list(Wrappers.<TableEntity>lambdaQuery().eq(TableEntity::getProjectId, projectId));
        if (CollUtil.isNotEmpty(tableIdList)) {
            tableList = tableList.stream()
                    .filter(table -> tableIdList.contains(table.getId()))
                    .toList();
        }
        List<TemplateContentVO> tablePreviewList = tableListPreview(tableList, tableTemplateIdList);
        allPreviewList.addAll(tablePreviewList);

        //获取枚举预览数据
        if (project.getEnumTemplateGroupId() != null) {
            List<TemplateContentVO> enumPreviewList = enumListPreview(project, enumIdList, enumTemplateIdList);
            allPreviewList.addAll(enumPreviewList);
        }

        //获取项目预览数据
        List<TemplateContentVO> projectPreviewList = projectPreview(project, query.getTemplateIds());
        allPreviewList.addAll(projectPreviewList);

        return allPreviewList;
    }

    /**
     * 构建单表预览
     */
    public List<TemplateContentVO> tablePreview(Long tableId, List<Long> templateIds) {
        // 表信息
        TableEntity table = tableService.getById(tableId);

        ProjectEntity project = projectService.getById(table.getProjectId());

        //获取数据模型
        TableModel tableModel = dataModelBuilder.buildTableDataModel(table, project);

        Long tableTemplateGroupId = tableService.getTableTemplateGroupId(tableId);

        TemplateGroupEntity templateGroup = templateGroupService.getById(tableTemplateGroupId, true);
        return renderGroupTemplates(templateGroup, templateIds, tableModel,
                templateContentVO -> templateContentVO.setTableId(tableId));
    }

    /**
     * 构建单枚举预览
     */
    public List<TemplateContentVO> enumPreview(Long enumId, List<Long> templateIds) {
        EnumEntity enumEntity = enumService.getById(enumId);
        ProjectEntity project = projectService.getById(enumEntity.getProjectId());
        EnumModel enumModel = dataModelBuilder.buildEnumDataModel(enumEntity, project);

        //查询项目对应的枚举模板
        TemplateGroupEntity templateGroup = templateGroupService.getById(project.getEnumTemplateGroupId(), true);
        return renderGroupTemplates(templateGroup, templateIds, enumModel,
                templateContentVO -> templateContentVO.setEnumId(enumId));
    }

    /**
     * 构建项目自身模板预览
     */
    public List<TemplateContentVO> projectPreview(ProjectEntity project, List<Long> templateIdList) {
        //获取数据源信息
        DatasourceEntity datasource = dataModelBuilder.resolveDatasource(project);
        //获取项目模板组信息
        Long projectTemplateGroupId = project.getProjectTemplateGroupId();
        TemplateGroupEntity templateGroup = templateGroupService.getById(projectTemplateGroupId, true);

        ProjectModel projectModel = dataModelBuilder.buildProjectDataModel(project, datasource);
        return renderGroupTemplates(templateGroup, templateIdList, projectModel, null);
    }

    /**
     * 构建多表预览
     */
    private List<TemplateContentVO> tableListPreview(List<TableEntity> tableList, List<Long> tableTemplateIdList) {
        return tableList.stream()
                .map(table -> tablePreview(table.getId(), tableTemplateIdList))
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * 构建项目下多枚举预览
     */
    private List<TemplateContentVO> enumListPreview(ProjectEntity project, List<Long> enumIdList, List<Long> enumTemplateIdList) {
        List<EnumModel> enumModelList = new ArrayList<>();
        List<EnumEntity> enumList = enumService.enumList(project.getId());
        enumList = enumList.stream()
                .filter(enumEntity -> CollUtil.isEmpty(enumIdList) || enumIdList.contains(enumEntity.getId()))
                .toList();
        for (EnumEntity enumEntity : enumList) {
            EnumModel dataModel = dataModelBuilder.buildEnumDataModel(enumEntity, project);
            enumModelList.add(dataModel);
        }

        //查询项目对应的枚举模板
        TemplateGroupEntity templateGroup = templateGroupService.getById(project.getEnumTemplateGroupId(), true);
        List<TemplateVO> templateList = leafTemplates(templateGroup, enumTemplateIdList);
        return enumModelList.stream()
                .flatMap(enumModel -> templateList.stream()
                        .filter(template -> checkCondition(template.getConditionExpression(), enumModel))
                        .map(template -> {
                            TemplateContentVO templateContentVO = render(templateGroup, template, enumModel);
                            templateContentVO.setEnumId(enumModel.getEnumId());
                            return templateContentVO;
                        })
                )
                .toList();
    }
}
