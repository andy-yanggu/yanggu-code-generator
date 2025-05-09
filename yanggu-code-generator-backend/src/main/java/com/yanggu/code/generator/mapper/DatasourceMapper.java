package com.yanggu.code.generator.mapper;

import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import com.yanggu.code.generator.domain.vo.DatasourceVO;
import com.yanggu.code.generator.domain.query.DatasourceEntityQuery;
import com.yanggu.code.generator.domain.query.DatasourceVOQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据源Mapper
 */
@Mapper
@Repository
public interface DatasourceMapper extends BaseMapperPlus<DatasourceEntity> {

    /**
     * Entity分页
     */
    IPage<DatasourceEntity> entityPage(@Param("query") DatasourceEntityQuery query);

    /**
     * Entity列表
     */
    List<DatasourceEntity> entityList(@Param("query") DatasourceEntityQuery query);

    /**
     * VO分页
     */
    IPage<DatasourceVO> voPage(@Param("query") DatasourceVOQuery query);

    /**
     * VO列表
     */
    List<DatasourceVO> voList(@Param("query") DatasourceVOQuery query);

}