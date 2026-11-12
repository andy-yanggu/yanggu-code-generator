package com.yanggu.code.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.BaseClassEntity;
import com.yanggu.code.generator.domain.query.BaseClassQuery;
import com.yanggu.code.generator.domain.vo.BaseClassVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
    IPage<BaseClassEntity> entityPage(IPage<BaseClassEntity> page, @Param("query") BaseClassQuery query);

    /**
     * Entity列表
     */
    List<BaseClassEntity> entityList(@Param("query") BaseClassQuery query);

    /**
     * VO分页
     */
    IPage<BaseClassVO> voPage(IPage<BaseClassVO> page, @Param("query") BaseClassQuery query);

    /**
     * VO列表
     */
    List<BaseClassVO> voList(@Param("query") BaseClassQuery query);

}