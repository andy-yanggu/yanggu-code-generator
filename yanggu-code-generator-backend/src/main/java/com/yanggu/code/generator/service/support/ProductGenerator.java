package com.yanggu.code.generator.service.support;

import com.yanggu.code.generator.domain.query.GeneratorQuery;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;

import java.util.List;

/**
 * 产物生成处理器：按产物类型收口生成编排与 zip 命名，新增产物类型只需新增实现
 */
public interface ProductGenerator {

    TemplateGroupTypeEnum type();

    List<TemplateContentVO> generate(GeneratorQuery query);

    String artifactName(GeneratorQuery query);
}