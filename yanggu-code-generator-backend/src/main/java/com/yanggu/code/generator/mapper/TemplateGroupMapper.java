package com.yanggu.code.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.query.TemplateGroupQuery;
import com.yanggu.code.generator.domain.vo.TemplateGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模板组Mapper
 */
@Mapper
@Repository
public interface TemplateGroupMapper extends BaseMapperPlus<TemplateGroupEntity> {

    /**
     * Entity分页
     */
    IPage<TemplateGroupEntity> entityPage(IPage<TemplateGroupEntity> page, @Param("query") TemplateGroupQuery query);

    /**
     * Entity列表
     */
    List<TemplateGroupEntity> entityList(@Param("query") TemplateGroupQuery query);

    /**
     * VO分页
     */
    IPage<TemplateGroupVO> voPage(IPage<TemplateGroupVO> page, @Param("query") TemplateGroupQuery query);

    /**
     * VO列表
     */
    List<TemplateGroupVO> voList(@Param("query") TemplateGroupQuery query);

}