package com.yanggu.code.generator.mapper;

import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.domain.entity.BaseClassEntity;
import com.yanggu.code.generator.domain.vo.BaseClassVO;
import com.yanggu.code.generator.domain.query.BaseClassEntityQuery;
import com.yanggu.code.generator.domain.query.BaseClassVOQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 基类Mapper
 */
@Mapper
@Repository
public interface BaseClassMapper extends BaseMapperPlus<BaseClassEntity> {

    /**
     * Entity分页
     */
    IPage<BaseClassEntity> entityPage(@Param("query") BaseClassEntityQuery query);

    /**
     * Entity列表
     */
    List<BaseClassEntity> entityList(@Param("query") BaseClassEntityQuery query);

    /**
     * VO分页
     */
    IPage<BaseClassVO> voPage(@Param("query") BaseClassVOQuery query);

    /**
     * VO列表
     */
    List<BaseClassVO> voList(@Param("query") BaseClassVOQuery query);

}