package com.yanggu.code.generator.mapper;

import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.BaseClassEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 基类Mapper
 */
@Mapper
@Repository
public interface BaseClassMapper extends BaseMapperPlus<BaseClassEntity> {

}