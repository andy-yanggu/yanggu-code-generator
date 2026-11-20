package com.yanggu.code.generator.service.impl;

import cn.hutool.v7.core.util.EnumUtil;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.query.*;
import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import com.yanggu.code.generator.enums.BusinessResultError;
import com.yanggu.code.generator.enums.GeneratorProductTypeEnum;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.*;
import com.yanggu.code.generator.service.support.DataModelBuilder;
import com.yanggu.code.generator.service.support.DownloadHandler;
import com.yanggu.code.generator.service.support.PreviewBuilder;
import com.yanggu.code.generator.service.support.PreviewTreeBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 代码生成服务实现类
 * 仅负责接口层分发、单品生成统一入口与模板测试；
 * 预览编排、数据模型组装、下载输出分别委托
 * PreviewBuilder / DataModelBuilder / DownloadHandler 协作组件。
 */
@Service
public class GeneratorServiceImpl implements GeneratorService {

    @Autowired
    private TableService tableService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EnumService enumService;

    @Autowired
    private TemplateGroupService templateGroupService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PreviewBuilder previewBuilder;

    @Autowired
    private DataModelBuilder dataModelBuilder;

    @Autowired
    private DownloadHandler downloadHandler;

    @Override
    public List<PreviewTemplateVO> preview(CodePreviewQuery codePreviewQuery) throws Exception {
        Integer generatorProductType = codePreviewQuery.getGeneratorProductType();
        Long previewProductId = codePreviewQuery.getPreviewProductId();

        List<TemplateContentVO> templateContentList;
        switch (EnumUtil.getBy(GeneratorProductTypeEnum::getProductType, generatorProductType)) {
            case PROJECT -> {
                GeneratorProjectQuery projectQuery = new GeneratorProjectQuery();
                projectQuery.setProjectId(previewProductId);
                templateContentList = previewBuilder.buildProjectPreview(projectQuery);
            }
            case TABLE -> {
                GeneratorTableQuery tableQuery = new GeneratorTableQuery();
                tableQuery.setTableIdList(List.of(previewProductId));
                templateContentList = previewBuilder.tablePreview(tableQuery);
            }
            case ENUM -> {
                GeneratorEnumQuery enumQuery = new GeneratorEnumQuery();
                enumQuery.setEnumIdList(List.of(previewProductId));
                templateContentList = previewBuilder.enumPreview(enumQuery);
            }
            default -> throw new BusinessException("Invalid generator product type: " + generatorProductType);
        }
        return PreviewTreeBuilder.buildTree(templateContentList);
    }

    @Override
    public ResponseEntity<byte[]> downloadSingle(CodeSingleGeneratorQuery singleGeneratorQuery) throws Exception {
        TemplateContentVO preview = generateContents(singleGeneratorQuery).getFirst();
        return downloadHandler.buildResponseEntity(preview.getFileName(), preview.getContent().getBytes());
    }

    @Override
    public void singleLocal(CodeSingleGeneratorQuery singleGeneratorQuery) throws Exception {
        downloadHandler.downloadLocal(generateContents(singleGeneratorQuery));
    }

    @Override
    public void projectDownloadLocal(GeneratorProjectQuery projectQuery) throws Exception {
        downloadHandler.downloadLocal(previewBuilder.buildProjectPreview(projectQuery));
    }

    @Override
    public ResponseEntity<byte[]> projectDownloadZip(GeneratorProjectQuery projectQuery) throws Exception {
        List<TemplateContentVO> list = previewBuilder.buildProjectPreview(projectQuery);

        //构建zip文件名
        ProjectEntity project = projectService.getById(projectQuery.getProjectId());

        //生成zip文件
        return downloadHandler.downloadZip(project.getProjectName(), list);
    }

    @Override
    public void tableDownloadLocal(GeneratorTableQuery tableQuery) {
        downloadHandler.downloadLocal(previewBuilder.tablePreview(tableQuery));
    }

    @Override
    public ResponseEntity<byte[]> tableDownloadZip(GeneratorTableQuery tableQuery) {
        List<TemplateContentVO> list = previewBuilder.tablePreview(tableQuery);

        // 拼接表名
        String tableNameConcat = tableService.listByIds(tableQuery.getTableIdList()).stream()
                .map(TableEntity::getTableName)
                .collect(Collectors.joining("_"));

        return downloadHandler.downloadZip(tableNameConcat, list);
    }

    @Override
    public void enumDownloadLocal(GeneratorEnumQuery enumQuery) {
        downloadHandler.downloadLocal(previewBuilder.enumPreview(enumQuery));
    }

    @Override
    public ResponseEntity<byte[]> enumDownloadZip(GeneratorEnumQuery enumQuery) {
        List<TemplateContentVO> enumPreviewData = previewBuilder.enumPreview(enumQuery);

        //拼接枚举名称
        List<Long> enumIdList = enumQuery.getEnumIdList();
        String enumNameConcat = enumService.listByIds(enumIdList).stream()
                .map(EnumEntity::getEnumName)
                .collect(Collectors.joining("_"));

        return downloadHandler.downloadZip(enumNameConcat, enumPreviewData);
    }

    @Override
    public TemplateContentVO templateTest(TemplateTestQuery templateTestQuery) throws Exception {
        TemplateGroupEntity templateGroup = templateGroupService.getById(templateTestQuery.getTemplateGroupId(), false);
        TemplateVO templateVO = templateService.detail(templateTestQuery.getTemplateId(), true);
        templateVO.setTemplateContent(templateTestQuery.getTemplateContent());
        ProjectEntity project = projectService.getById(templateTestQuery.getProjectId());
        return switch (EnumUtil.getBy(TemplateGroupTypeEnum::getCode, templateTestQuery.getTemplateGroupType())) {
            case PROJECT -> renderForTest(templateGroup, templateVO,
                    dataModelBuilder.buildProjectDataModel(project, previewBuilder.resolveDatasource(project)));
            case TABLE -> renderForTest(templateGroup, templateVO,
                    dataModelBuilder.buildTableDataModel(tableService.getById(templateTestQuery.getTestId()), project));
            case ENUM -> renderForTest(templateGroup, templateVO,
                    dataModelBuilder.buildEnumDataModel(enumService.getById(templateTestQuery.getTestId()), project));
            default -> throw new IllegalArgumentException("Invalid template group type: " + templateTestQuery.getTemplateGroupType());
        };
    }

    /**
     * 统一单品生成：按模板组类型渲染指定产品的单个模板，
     * 消除原 downloadSingle/singleLocal 中 PROJECT/TABLE/ENUM 的重复查询构造与分发
     */
    private List<TemplateContentVO> generateContents(CodeSingleGeneratorQuery singleGeneratorQuery) throws Exception {
        Long id = singleGeneratorQuery.getId();
        List<Long> templateIdList = List.of(singleGeneratorQuery.getTemplateId());
        return switch (EnumUtil.getBy(TemplateGroupTypeEnum::getCode, singleGeneratorQuery.getTemplateGroupType())) {
            case PROJECT -> previewBuilder.projectPreview(projectService.getById(id), templateIdList);
            case TABLE -> {
                GeneratorTableQuery tableQuery = new GeneratorTableQuery();
                tableQuery.setTableIdList(List.of(id));
                tableQuery.setTemplateIdList(templateIdList);
                yield previewBuilder.tablePreview(tableQuery);
            }
            case ENUM -> {
                GeneratorEnumQuery enumQuery = new GeneratorEnumQuery();
                enumQuery.setEnumIdList(List.of(id));
                enumQuery.setTemplateIdList(templateIdList);
                yield previewBuilder.enumPreview(enumQuery);
            }
        };
    }

    /**
     * 模板测试渲染：条件表达式不通过则抛业务异常，通过则渲染，
     * 消除原三个分支重复的"校验失败抛异常→渲染"骨架
     */
    private TemplateContentVO renderForTest(TemplateGroupEntity templateGroup, TemplateVO templateVO, Object dataModel) {
        if (!previewBuilder.checkCondition(templateVO.getConditionExpression(), dataModel)) {
            throw new BusinessException(BusinessResultError.EXECUTION_EXPRESS_ERROR);
        }
        return previewBuilder.render(templateGroup, templateVO, dataModel);
    }
}
