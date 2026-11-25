package com.yanggu.code.generator.service.impl;

import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import com.yanggu.code.generator.domain.vo.VariableNodeVO;
import com.yanggu.code.generator.enums.BusinessResultError;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.*;
import com.yanggu.code.generator.service.support.*;
import cn.hutool.v7.core.text.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 代码生成服务实现类
 * 仅负责入参校验、产物处理器路由与模板测试共用流程；生成编排/数据模型/下载输出委托 support 协作组件。
 */
@Service
public class GeneratorServiceImpl implements GeneratorService {

    @Autowired
    private ProductGenerators productGenerators;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PreviewBuilder previewBuilder;

    @Autowired
    private DownloadHandler downloadHandler;

    // 构建器无状态、不带 Spring 依赖，与 VariableCatalogServiceImpl 同款直接实例化
    private final VariableCatalogBuilder variableCatalogBuilder = new VariableCatalogBuilder();

    @Override
    public List<PreviewTemplateVO> preview(GeneratorQuery query) {
        query.validate(false);
        return PreviewTreeBuilder.buildTree(renderTemplateList(query));
    }

    @Override
    public ResponseEntity<byte[]> downloadZip(GeneratorQuery query) {
        query.validate(false);
        ProductGenerator generator = generator(query);
        String originFileName = generator.artifactName(query);
        List<TemplateContentVO> templateContentList = generator.generate(query);
        return downloadHandler.downloadZip(originFileName, templateContentList);
    }

    @Override
    public void downloadLocal(GeneratorQuery query) {
        query.validate(false);
        downloadHandler.downloadLocal(renderTemplateList(query));
    }

    @Override
    public ResponseEntity<byte[]> downloadSingle(GeneratorQuery query) {
        query.validate(true);
        TemplateContentVO content = generator(query).generateSingle(query);
        return downloadHandler.buildResponseEntity(content.getFileName(), content.getContent().getBytes());
    }

    @Override
    public void singleLocal(GeneratorQuery query) {
        query.validate(true);
        downloadHandler.downloadLocal(List.of(generator(query).generateSingle(query)));
    }

    @Override
    public TemplateContentVO templateTest(TemplateTestQuery templateTestQuery) {
        // 渲染测试特有必填：模板ID与模板内容，在代码中校验（testDataModel 不需要这两项）
        if (templateTestQuery.getTemplateId() == null) {
            throw new BusinessException("模板ID不能为空");
        }
        if (StrUtil.isBlank(templateTestQuery.getTemplateContent())) {
            throw new BusinessException("模板内容不能为空");
        }
        TemplateVO templateVO = templateService.detail(templateTestQuery.getTemplateId(), true);
        templateVO.setTemplateContent(templateTestQuery.getTemplateContent());
        ProductGenerator generator = productGenerators.getByCode(templateTestQuery.getTemplateGroupType());
        validateTestArtifact(generator, templateTestQuery);
        ProjectEntity project = projectService.getById(templateTestQuery.getProjectId());
        Object dataModel = generator.testDataModel(templateTestQuery, project);
        return renderForTest(templateTestQuery.getTemplateGroupType(), templateVO, dataModel);
    }

    @Override
    public List<VariableNodeVO> testDataModel(TemplateTestQuery templateTestQuery) {
        // 类型非法不再静默返回空列表：由路由组件直接报错，此处不重复判空
        ProductGenerator generator = productGenerators.getByCode(templateTestQuery.getTemplateGroupType());
        validateTestArtifact(generator, templateTestQuery);
        ProjectEntity project = projectService.getById(templateTestQuery.getProjectId());
        Object dataModel = generator.testDataModel(templateTestQuery, project);
        // 根模型类型与变量目录链路共用 TemplateModelMapping，新增类型时只需改那一处
        return variableCatalogBuilder.build(TemplateModelMapping.of(generator.type()).rootModelClass(), dataModel);
    }

    /**
     * 模板测试公共条件校验：testId 仅表/枚举类型必填（注解无法表达按类型条件），
     * projectId 等非空拦截由 Controller 的 @Validated 完成，此处判空为直调场景的防御性兜底
     */
    private void validateTestArtifact(ProductGenerator generator, TemplateTestQuery query) {
        if (generator.type() != TemplateGroupTypeEnum.PROJECT && query.getTestId() == null) {
            throw new BusinessException("表/枚举模板测试必须指定测试ID");
        }
        if (query.getProjectId() == null) {
            throw new BusinessException("项目ID不能为空");
        }
    }

    private ProductGenerator generator(GeneratorQuery query) {
        return productGenerators.getByCode(query.getProductType());
    }
    
    private List<TemplateContentVO> renderTemplateList(GeneratorQuery query) {
        ProductGenerator generator = generator(query);
        return generator.generate(query);
    }

    /**
     * 模板测试渲染：条件表达式不通过则抛业务异常，通过则渲染
     */
    private TemplateContentVO renderForTest(Integer templateGroupType, TemplateVO templateVO, Object dataModel) {
        if (!previewBuilder.checkCondition(templateVO.getConditionExpression(), dataModel)) {
            throw new BusinessException(BusinessResultError.EXECUTION_EXPRESS_ERROR);
        }
        return previewBuilder.render(templateGroupType, templateVO, dataModel);
    }

}