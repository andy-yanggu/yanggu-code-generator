package com.yanggu.code.generator.mapper;

import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.EnumItemEntity;
import com.yanggu.code.generator.domain.query.EnumItemQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 枚举项Mapper
 */
@Mapper
@Repository
public interface EnumItemMapper extends BaseMapperPlus<EnumItemEntity> {

    /**
     * Entity列表
     */
    List<EnumItemEntity> entityList(@Param("query") EnumItemQuery query);

}