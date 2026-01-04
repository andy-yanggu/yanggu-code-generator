package com.yanggu.code.generator.service;

import java.util.Map;

/**
 * 项目模板组属性值服务层
 */
public interface TemplateGroupPropertyDataService {

    Map<String, Object> getData(Long templateGroupId, Map<String, Object> data);

}