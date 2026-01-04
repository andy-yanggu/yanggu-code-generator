package com.yanggu.code.generator.service.impl;

import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import com.yanggu.code.generator.service.TemplateGroupPropertyDataService;
import com.yanggu.code.generator.service.TemplateGroupPropertyService;
import com.yanggu.code.generator.util.GenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板组属性属性Service实现类
 */
@Service
public class TemplateGroupPropertyDataServiceImpl implements TemplateGroupPropertyDataService {

    @Autowired
    private TemplateGroupPropertyService templateGroupPropertyService;

    @Override
    public Map<String, Object> getData(Long templateGroupId, Map<String, Object> data) {
        List<Long> templateGroupIdList = List.of(templateGroupId);
        Map<String, Object> collectMap = templateGroupPropertyService.listByGroupId(templateGroupIdList).stream()
                .collect(Collectors.toMap(TemplateGroupPropertyEntity::getPropKey, TemplateGroupPropertyEntity::getPropDefaultValue));

        collectMap.putAll(data);

        return collectMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> GenUtil.handleData(entry.getValue().toString())));
    }

}
