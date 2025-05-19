package com.yanggu.code.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.query.TableFieldEntityQuery;
import com.yanggu.code.generator.domain.query.TableFieldVOQuery;
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
    IPage<TableFieldEntity> entityPage(@Param("query") TableFieldEntityQuery query);

    /**
     * Entity列表
     */
    List<TableFieldEntity> entityList(@Param("query") TableFieldEntityQuery query);

    /**
     * VO分页
     */
    IPage<TableFieldVO> voPage(@Param("query") TableFieldVOQuery query);

    /**
     * VO列表
     */
    List<TableFieldVO> voList(@Param("query") TableFieldVOQuery query);

}