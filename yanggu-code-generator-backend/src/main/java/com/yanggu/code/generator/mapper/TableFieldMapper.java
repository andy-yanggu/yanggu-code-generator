package com.yanggu.code.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.query.TableFieldQuery;
import com.yanggu.code.generator.domain.vo.TableFieldVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 表字段Mapper
 */
@Mapper
@Repository
public interface TableFieldMapper extends BaseMapperPlus<TableFieldEntity> {

    /**
     * Entity分页
     */
    IPage<TableFieldEntity> entityPage(IPage<TableFieldEntity> page, @Param("query") TableFieldQuery query);

    /**
     * Entity列表
     */
    List<TableFieldEntity> entityList(@Param("query") TableFieldQuery query);

    /**
     * VO分页
     */
    IPage<TableFieldVO> voPage(IPage<TableFieldVO> page, @Param("query") TableFieldQuery query);

    /**
     * VO列表
     */
    List<TableFieldVO> voList(@Param("query") TableFieldQuery query);

}