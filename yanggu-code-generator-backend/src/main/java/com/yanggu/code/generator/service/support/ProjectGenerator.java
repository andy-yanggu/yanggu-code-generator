package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目产物生成器
 */
@Component
public class ProjectGenerator implements ProductGenerator {

    @Autowired
    private PreviewBuilder previewBuilder;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DataModelBuilder dataModelBuilder;

    @Override
    public TemplateGroupTypeEnum type() {
        return TemplateGroupTypeEnum.PROJECT;
    }

    @Override
    public List<TemplateContentVO> generate(GeneratorQuery query) {
        return previewBuilder.fullProjectPreview(query);
    }

    @Override
    public TemplateContentVO generateSingle(GeneratorQuery query) {
        // 与 tablePreview/enumPreview 保持入参一致：仅传 ID，项目实体由 PreviewBuilder 内部加载
        return previewBuilder.projectPreview(query.getProductIds().getFirst(), query.getTemplateIds()).getFirst();
    }

    @Override
    public String artifactName(GeneratorQuery query) {
        return projectService.getById(query.getProductIds().getFirst()).getProjectName();
    }

    @Override
    public Object testDataModel(TemplateTestQuery query, ProjectEntity project) {
        return dataModelBuilder.buildProjectDataModel(project);
    }
}