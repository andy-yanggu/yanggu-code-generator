package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.EnumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 枚举产物生成器（真批量：逐枚举渲染汇总）
 */
@Component
public class EnumGenerator implements ProductGenerator {

    @Autowired
    private PreviewBuilder previewBuilder;

    @Autowired
    private EnumService enumService;

    @Autowired
    private DataModelBuilder dataModelBuilder;

    @Override
    public TemplateGroupTypeEnum type() {
        return TemplateGroupTypeEnum.ENUM;
    }

    @Override
    public List<TemplateContentVO> generate(GeneratorQuery query) {
        return query.getProductIds().stream()
                .map(enumId -> previewBuilder.enumPreview(enumId, query.getTemplateIds()))
                .flatMap(Collection::stream)
                .toList();
    }

    @Override
    public String artifactName(GeneratorQuery query) {
        return enumService.listByIds(query.getProductIds()).stream()
                .map(EnumEntity::getEnumName)
                .collect(Collectors.joining("_"));
    }

    @Override
    public Object testDataModel(TemplateTestQuery query, ProjectEntity project) {
        return dataModelBuilder.buildEnumDataModel(enumService.getById(query.getTestId()), project);
    }
}