package com.yanggu.code.generator.mapper;

import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.ProjectTemplateGroupPropertyValueEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 项目模板组属性值Mapper
 */
@Mapper
@Repository
public interface ProjectTemplateGroupPropertyValueMapper extends BaseMapperPlus<ProjectTemplateGroupPropertyValueEntity> {
}