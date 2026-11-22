package com.yanggu.code.generator.service;

import com.yanggu.code.generator.domain.vo.VariableNodeVO;

import java.util.List;

/**
 * 模板变量目录服务层
 * 独立于 TemplateService：TemplateGroupPropertyServiceImpl 已反向注入 TemplateService，
 * 目录编排若塞进 TemplateServiceImpl 会新增一条字段注入环（本项目开启了
 * allow-circular-references，成环不会启动失败，但不应继续累积）
 */
public interface VariableCatalogService {

    /**
     * 按模板组类型返回可插入的变量目录
     *
     * @param templateGroupId 模板组ID
     */
    List<VariableNodeVO> variableCatalog(Long templateGroupId);

}
