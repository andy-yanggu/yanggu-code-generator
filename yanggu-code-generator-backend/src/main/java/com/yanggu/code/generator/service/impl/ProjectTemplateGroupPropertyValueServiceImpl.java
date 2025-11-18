package com.yanggu.code.generator.service.impl;

import cn.hutool.v7.json.JSONArray;
import cn.hutool.v7.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.domain.entity.ProjectTemplateGroupPropertyValueEntity;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import com.yanggu.code.generator.mapper.ProjectTemplateGroupPropertyValueMapper;
import com.yanggu.code.generator.service.ProjectTemplateGroupPropertyValueService;
import com.yanggu.code.generator.service.TemplateGroupPropertyService;
import com.yanggu.code.generator.util.GenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目模板组属性值Service实现类
 */
@Service
public class ProjectTemplateGroupPropertyValueServiceImpl extends ServiceImpl<ProjectTemplateGroupPropertyValueMapper, ProjectTemplateGroupPropertyValueEntity> implements ProjectTemplateGroupPropertyValueService {

    @Autowired
    private ProjectTemplateGroupPropertyValueMapper projectTemplateGroupPropertyValueMapper;

    @Autowired
    private TemplateGroupPropertyService templateGroupPropertyService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void saveData(Long projectId, Long templateGroupId, Map<String, Object> templateGroupPropValue) {

        Map<String, Long> collectMap = templateGroupPropertyService.listByGroupId(List.of(templateGroupId)).stream()
                .collect(Collectors.toMap(TemplateGroupPropertyEntity::getPropKey, TemplateGroupPropertyEntity::getId));

        templateGroupPropValue.forEach((key, value) -> {
            ProjectTemplateGroupPropertyValueEntity entity = new ProjectTemplateGroupPropertyValueEntity();
            entity.setProjectId(projectId);
            entity.setTemplateGroupId(templateGroupId);
            entity.setTemplateGroupPropertyId(collectMap.get(key));
            String data = GenUtil.getStringValue(value);
            entity.setTemplateGroupPropertyValue(data);
            projectTemplateGroupPropertyValueMapper.insert(entity);
        });
    }

    @Override
    public Map<String, Object> getData(Long projectId, Long templateGroupId) {
        Map<Long, String> collectMap = templateGroupPropertyService.listByGroupId(List.of(templateGroupId)).stream()
                .collect(Collectors.toMap(TemplateGroupPropertyEntity::getId, TemplateGroupPropertyEntity::getPropKey));

        LambdaQueryWrapper<ProjectTemplateGroupPropertyValueEntity> queryWrapper = Wrappers.lambdaQuery(ProjectTemplateGroupPropertyValueEntity.class)
                .eq(ProjectTemplateGroupPropertyValueEntity::getProjectId, projectId)
                .eq(ProjectTemplateGroupPropertyValueEntity::getTemplateGroupId, templateGroupId);

        return this.list(queryWrapper).stream()
                .collect(Collectors.toMap(
                        temp -> collectMap.get(temp.getTemplateGroupPropertyId()),
                        temp -> handlerData(temp.getTemplateGroupPropertyValue()))
                );
    }

    private Object handlerData(String value) {
        Object object = GenUtil.convertStringToAppropriateType(value);
        if (JSONUtil.isTypeJSONArray(object.toString())) {
            JSONArray array = JSONUtil.parseArray(object);
            return array.stream().map(item -> item.asJSONPrimitive().getValue()).toList();
        }
        return object;
    }

}
