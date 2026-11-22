package com.yanggu.code.generator.mapper;

import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 模板组属性Mapper
 */
@Mapper
@Repository
public interface TemplateGroupPropertyMapper extends BaseMapperPlus<TemplateGroupPropertyEntity> {

}