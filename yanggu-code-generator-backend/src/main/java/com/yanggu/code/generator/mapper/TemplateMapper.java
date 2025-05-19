package com.yanggu.code.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.TemplateEntity;
import com.yanggu.code.generator.domain.query.TemplateEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模板Mapper
 */
@Mapper
@Repository
public interface TemplateMapper extends BaseMapperPlus<TemplateEntity> {

    /**
     * Entity分页
     */
    IPage<TemplateEntity> entityPage(@Param("query") TemplateEntityQuery query);

    /**
     * Entity列表
     */
    List<TemplateEntity> entityList(@Param("query") TemplateEntityQuery query);

    /**
     * VO分页
     */
    IPage<TemplateVO> voPage(@Param("query") TemplateVOQuery query);

    /**
     * VO列表
     */
    List<TemplateVO> voList(@Param("query") TemplateVOQuery query);

}