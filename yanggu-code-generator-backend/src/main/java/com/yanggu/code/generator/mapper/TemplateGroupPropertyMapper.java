package com.yanggu.code.generator.mapper;

import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import com.yanggu.code.generator.domain.vo.TemplateGroupPropertyVO;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模板组属性Mapper
 */
@Mapper
@Repository
public interface TemplateGroupPropertyMapper extends BaseMapperPlus<TemplateGroupPropertyEntity> {

    /**
     * Entity分页
     */
    IPage<TemplateGroupPropertyEntity> entityPage(IPage<TemplateGroupPropertyEntity> page, @Param("query") TemplateGroupPropertyQuery query);

    /**
     * Entity列表
     */
    List<TemplateGroupPropertyEntity> entityList(@Param("query") TemplateGroupPropertyQuery query);

    /**
     * VO分页
     */
    IPage<TemplateGroupPropertyVO> voPage(IPage<TemplateGroupPropertyVO> page, @Param("query") TemplateGroupPropertyQuery query);

    /**
     * VO列表
     */
    List<TemplateGroupPropertyVO> voList(@Param("query") TemplateGroupPropertyQuery query);

}