package com.yanggu.code.generator.mapper;

import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 表字段Mapper
 */
@Mapper
@Repository
public interface TableFieldMapper extends BaseMapperPlus<TableFieldEntity> {

}