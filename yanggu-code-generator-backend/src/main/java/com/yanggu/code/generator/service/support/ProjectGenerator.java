package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
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

    @Override
    public TemplateGroupTypeEnum type() {
        return TemplateGroupTypeEnum.PROJECT;
    }

    @Override
    public List<TemplateContentVO> generate(GeneratorQuery query) {
        return previewBuilder.buildProjectPreview(query);
    }

    @Override
    public List<TemplateContentVO> generateSingle(GeneratorQuery query) {
        ProjectEntity project = projectService.getById(query.getProductIds().getFirst());
        return previewBuilder.projectPreview(project, query.getTemplateIds());
    }

    @Override
    public String artifactName(GeneratorQuery query) {
        return projectService.getById(query.getProductIds().getFirst()).getProjectName();
    }
}