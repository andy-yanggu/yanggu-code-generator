package com.yanggu.code.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import com.yanggu.code.generator.domain.query.DatasourceQuery;
import com.yanggu.code.generator.domain.vo.DatasourceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
    IPage<DatasourceEntity> entityPage(IPage<DatasourceEntity> page, @Param("query") DatasourceQuery query);

    /**
     * Entity列表
     */
    List<DatasourceEntity> entityList(@Param("query") DatasourceQuery query);

    /**
     * VO分页
     */
    IPage<DatasourceVO> voPage(IPage<DatasourceVO> page, @Param("query") DatasourceQuery query);

    /**
     * VO列表
     */
    List<DatasourceVO> voList(@Param("query") DatasourceQuery query);

}