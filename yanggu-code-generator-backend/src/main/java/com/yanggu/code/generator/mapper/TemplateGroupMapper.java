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
     * VO分页
     */
    IPage<TemplateGroupVO> voPage(IPage<TemplateGroupVO> page, @Param("query") TemplateGroupQuery query);

    /**
     * VO列表
     */
    List<TemplateGroupVO> voList(@Param("query") TemplateGroupQuery query);

}