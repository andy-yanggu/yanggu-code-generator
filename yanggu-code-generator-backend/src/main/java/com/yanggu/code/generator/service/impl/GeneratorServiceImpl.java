package com.yanggu.code.generator.service.impl;

import cn.hutool.v7.core.util.EnumUtil;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.domain.vo.TemplateVO;
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
 * 仅负责入参校验、产物处理器路由与模板测试；生成编排/数据模型/下载输出委托 support 协作组件。
 */
@Service
public class GeneratorServiceImpl implements GeneratorService {

    @Autowired
    private List<ProductGenerator> productGenerators;

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
        TemplateGroupEntity templateGroup = templateGroupService.getById(templateTestQuery.getTemplateGroupId(), false);
        TemplateVO templateVO = templateService.detail(templateTestQuery.getTemplateId(), true);
        templateVO.setTemplateContent(templateTestQuery.getTemplateContent());
        ProjectEntity project = projectService.getById(templateTestQuery.getProjectId());
        return switch (EnumUtil.getBy(TemplateGroupTypeEnum::getCode, templateTestQuery.getTemplateGroupType())) {
            case PROJECT -> renderForTest(templateGroup, templateVO,
                    dataModelBuilder.buildProjectDataModel(project, dataModelBuilder.resolveDatasource(project)));
            case TABLE -> renderForTest(templateGroup, templateVO,
                    dataModelBuilder.buildTableDataModel(tableService.getById(templateTestQuery.getTestId()), project));
            case ENUM -> renderForTest(templateGroup, templateVO,
                    dataModelBuilder.buildEnumDataModel(enumService.getById(templateTestQuery.getTestId()), project));
            default -> throw new IllegalArgumentException("Invalid template group type: " + templateTestQuery.getTemplateGroupType());
        };
    }

    private ProductGenerator generator(GeneratorQuery query) {
        return generatorMap.get(EnumUtil.getBy(TemplateGroupTypeEnum::getCode, query.getProductType()));
    }
    
    private List<TemplateContentVO> renderTemplateList(GeneratorQuery query) {
        ProductGenerator generator = generator(query);
        return generator.generate(query);
    }

    /**
     * 模板测试渲染：条件表达式不通过则抛业务异常，通过则渲染
     */
    private TemplateContentVO renderForTest(TemplateGroupEntity templateGroup, TemplateVO templateVO, Object dataModel) {
        if (!previewBuilder.checkCondition(templateVO.getConditionExpression(), dataModel)) {
            throw new BusinessException(BusinessResultError.EXECUTION_EXPRESS_ERROR);
        }
        return previewBuilder.render(templateGroup, templateVO, dataModel);
    }

}