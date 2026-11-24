package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.query.TemplateTestQuery;
import com.yanggu.code.generator.domain.vo.CascaderDataVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;

import java.util.List;

/**
 * 产物类型策略：按产物类型收口生成编排、zip 命名、模板测试数据模型与候选产物清单，新增产物类型只需新增实现
 */
public interface ProductGenerator {

    /**
     * 产物类型
     * @return
     */
    TemplateGroupTypeEnum type();

    /**
     * 生成
     */
    List<TemplateContentVO> generate(GeneratorQuery query);

    /**
     * 单品生成：单产物 + 单模板维度渲染，校验保证结果恰好一条。表/枚举 generate 已按模板 ID 过滤可直接复用；
     * 项目需覆写为仅渲染项目自身模板，避免全量预览中排在前面的表/枚举产物被误取
     */
    default TemplateContentVO generateSingle(GeneratorQuery query) {
        return generate(query).getFirst();
    }

    /**
     * 产物名称：用于 zip 命名
     */
    String artifactName(GeneratorQuery query);

    /**
     * 模板测试数据模型：按产物类型构建渲染用数据模型，project 由调用方查好传入避免重复查询
     */
    Object testDataModel(TemplateTestQuery query, ProjectEntity project);

    /**
     * 候选产物级联数据：给定模板组，列出本产物类型可关联的候选（一级为项目，二级为本类型产物），由各实现自行构建
     */
    List<CascaderDataVO> cascaderData(Long templateGroupId);

}