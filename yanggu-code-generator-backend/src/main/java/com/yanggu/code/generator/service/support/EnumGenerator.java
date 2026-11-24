package com.yanggu.code.generator.service.support;

import cn.hutool.v7.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.CascaderDataVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.service.EnumService;
import com.yanggu.code.generator.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    private ProjectService projectService;

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
        EnumEntity enumEntity = enumService.getById(query.getTestId());
        return dataModelBuilder.buildEnumDataModel(enumEntity, project);
    }

    /**
     * 候选产物级联数据：一级为绑定了本枚举模板组的项目，二级为项目下的枚举，无枚举的项目不作为候选
     */
    @Override
    public List<CascaderDataVO> cascaderData(Long templateGroupId) {
        List<ProjectEntity> projectList = projectService.listByEnumTemplateGroupId(templateGroupId);
        if (CollUtil.isEmpty(projectList)) {
            return List.of();
        }
        List<Long> projectIdList = projectList.stream()
                .map(ProjectEntity::getId)
                .toList();
        // 一次 in 查询取回全部候选枚举再按项目分组，避免逐项目查询的 N+1
        LambdaQueryWrapper<EnumEntity> enumQueryWrapper = Wrappers.lambdaQuery(EnumEntity.class)
                .in(EnumEntity::getProjectId, projectIdList);
        List<EnumEntity> enumList = enumService.list(enumQueryWrapper);
        Map<Long, List<EnumEntity>> enumGroupMap = enumList.stream()
                .collect(Collectors.groupingBy(EnumEntity::getProjectId));

        List<CascaderDataVO> nodeList = new ArrayList<>();
        for (ProjectEntity project : projectList) {
            List<EnumEntity> children = enumGroupMap.get(project.getId());
            if (CollUtil.isEmpty(children)) {
                // 该项目下一个也没有，候选无意义
                continue;
            }
            CascaderDataVO projectNode = projectNode(project);
            projectNode.setChildren(children.stream()
                    .map(enumEntity -> enumNode(enumEntity, project))
                    .toList());
            nodeList.add(projectNode);
        }
        return nodeList;
    }

    // ==================== private：候选产物装配 ====================

    /**
     * 项目节点（一级）：value 与 type 的同源规则在 CascaderDataVO.of
     */
    private CascaderDataVO projectNode(ProjectEntity project) {
        return CascaderDataVO.of(project.getId(), project.getProjectName(), project.getProjectDesc(),
                "project", project.getGeneratorType());
    }

    /**
     * 枚举节点（二级）：generatorType 恒取所属项目
     */
    private CascaderDataVO enumNode(EnumEntity enumEntity, ProjectEntity project) {
        return CascaderDataVO.of(enumEntity.getId(), enumEntity.getEnumName(), enumEntity.getEnumDesc(),
                "enum", project.getGeneratorType());
    }
}