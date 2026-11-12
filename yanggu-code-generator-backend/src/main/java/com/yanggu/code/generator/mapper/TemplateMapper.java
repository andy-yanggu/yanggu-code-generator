package com.yanggu.code.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.TemplateEntity;
import com.yanggu.code.generator.domain.query.TemplateQuery;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 模板Mapper
 */
@Mapper
@Repository
public interface TemplateMapper extends BaseMapperPlus<TemplateEntity> {

    /**
     * VO分页
     */
    IPage<TemplateVO> voPage(IPage<TemplateVO> page, @Param("query") TemplateQuery query);

}