package com.yanggu.code.generator.mapper;

import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 数据源Mapper
 */
@Mapper
@Repository
public interface DatasourceMapper extends BaseMapperPlus<DatasourceEntity> {

}