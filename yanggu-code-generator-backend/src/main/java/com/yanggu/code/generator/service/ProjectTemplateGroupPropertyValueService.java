package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yanggu.code.generator.domain.entity.ProjectTemplateGroupPropertyValueEntity;

import java.util.Map;

/**
 * 项目模板组属性值服务层
 */
public interface ProjectTemplateGroupPropertyValueService extends IService<ProjectTemplateGroupPropertyValueEntity> {

    default void deleteByProjectIdAndTemplateGroupId(Long projectId, Long projectTemplateGroupId) {
        LambdaQueryWrapper<ProjectTemplateGroupPropertyValueEntity> queryWrapper = Wrappers.lambdaQuery(ProjectTemplateGroupPropertyValueEntity.class);
        queryWrapper.eq(ProjectTemplateGroupPropertyValueEntity::getProjectId, projectId);
        queryWrapper.eq(ProjectTemplateGroupPropertyValueEntity::getTemplateGroupId, projectTemplateGroupId);
        remove(queryWrapper);
    }

    void saveData(Long projectId, Long templateGroupId, Map<String, Object> templateGroupPropValue);

    Map<String, Object> getData(Long projectId, Long templateGroupId);

}