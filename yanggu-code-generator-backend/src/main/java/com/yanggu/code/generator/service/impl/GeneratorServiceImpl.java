package com.yanggu.code.generator.service.impl;

import cn.hutool.v7.core.util.EnumUtil;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.model.EnumModel;
import com.yanggu.code.generator.domain.model.ProjectModel;
import com.yanggu.code.generator.domain.model.TableModel;
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
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成服务实现类
 * 仅负责入参校验、产物处理器路由与模板测试共用流程；生成编排/数据模型/下载输出委托 support 协作组件。
 */
@Service
public class GeneratorServiceImpl implements GeneratorService {

    @Autowired
    private List<ProductGenerator> productGenerators;

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

    private Map<TemplateGroupTypeEnum, ProductGenerator> generatorMap;

    @PostConstruct
    void initGeneratorMap() {
        generatorMap = new EnumMap<>(TemplateGroupTypeEnum.class);
        productGenerators.forEach(generator -> generatorMap.put(generator.type(), generator));
    }

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
        TemplateVO templateVO = templateService.detail(templateTestQuery.getTemplateId(), true);
        templateVO.setTemplateContent(templateTestQuery.getTemplateContent());
        ProjectEntity project = projectService.getById(templateTestQuery.getProjectId());
        ProductGenerator generator = generator(EnumUtil.getBy(TemplateGroupTypeEnum::getCode, templateTestQuery.getTemplateGroupType()));
        return renderForTest(templateTestQuery.getTemplateGroupType(), templateVO, generator.testDataModel(templateTestQuery, project));
    }

    @Override
    public List<VariableNodeVO> testDataModel(TemplateTestQuery templateTestQuery) {
        TemplateGroupTypeEnum groupType = EnumUtil.getBy(TemplateGroupTypeEnum::getCode, templateTestQuery.getTemplateGroupType());
        if (groupType == null) {
            // 不静默返回空列表，否则面板只会显示「无变量」而说不出原因（与 VariableCatalogServiceImpl 同款报错）
            throw new BusinessException("模板组类型错误, 模板组id: {}", templateTestQuery.getTemplateGroupId());
        }
        ProjectEntity project = projectService.getById(templateTestQuery.getProjectId());
        Object dataModel = generator(groupType).testDataModel(templateTestQuery, project);
        Class<?> rootModelClass = switch (groupType) {
            case PROJECT -> ProjectModel.class;
            case TABLE -> TableModel.class;
            case ENUM -> EnumModel.class;
        };
        return variableCatalogBuilder.build(rootModelClass, dataModel);
    }

    private ProductGenerator generator(GeneratorQuery query) {
        return generator(EnumUtil.getBy(TemplateGroupTypeEnum::getCode, query.getProductType()));
    }

    private ProductGenerator generator(TemplateGroupTypeEnum type) {
        ProductGenerator generator = generatorMap.get(type);
        if (generator == null) {
            throw new IllegalArgumentException("Invalid product type: " + type);
        }
        return generator;
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