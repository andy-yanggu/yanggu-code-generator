package com.yanggu.code.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.query.EnumEntityQuery;
import com.yanggu.code.generator.domain.query.EnumVOQuery;
import com.yanggu.code.generator.domain.vo.EnumVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 枚举Mapper
 */
@Mapper
@Repository
public interface EnumMapper extends BaseMapperPlus<EnumEntity> {

    /**
     * Entity分页
     */
    IPage<EnumEntity> entityPage(@Param("query") EnumEntityQuery query);

    /**
     * Entity列表
     */
    List<EnumEntity> entityList(@Param("query") EnumEntityQuery query);

    /**
     * VO分页
     */
    IPage<EnumVO> voPage(@Param("query") EnumVOQuery query);

    /**
     * VO列表
     */
    List<EnumVO> voList(@Param("query") EnumVOQuery query);

}