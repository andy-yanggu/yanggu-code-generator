package com.yanggu.code.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.query.TableEntityQuery;
import com.yanggu.code.generator.domain.query.TableVOQuery;
import com.yanggu.code.generator.domain.vo.TableVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 表Mapper
 */
@Mapper
@Repository
public interface TableMapper extends BaseMapperPlus<TableEntity> {

    /**
     * Entity分页
     */
    IPage<TableEntity> entityPage(@Param("query") TableEntityQuery query);

    /**
     * Entity列表
     */
    List<TableEntity> entityList(@Param("query") TableEntityQuery query);

    /**
     * VO分页
     */
    IPage<TableVO> voPage(@Param("query") TableVOQuery query);

    /**
     * VO列表
     */
    List<TableVO> voList(@Param("query") TableVOQuery query);

    List<Long> distinctProjectIdList(@Param("idList") List<Long> idList);

}