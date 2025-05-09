package com.yanggu.code.generator.mapper;

import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.vo.ProjectVO;
import com.yanggu.code.generator.domain.query.ProjectEntityQuery;
import com.yanggu.code.generator.domain.query.ProjectVOQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目Mapper
 */
@Mapper
@Repository
public interface ProjectMapper extends BaseMapperPlus<ProjectEntity> {

    /**
     * Entity分页
     */
    IPage<ProjectEntity> entityPage(@Param("query") ProjectEntityQuery query);

    /**
     * Entity列表
     */
    List<ProjectEntity> entityList(@Param("query") ProjectEntityQuery query);

    /**
     * VO分页
     */
    IPage<ProjectVO> voPage(@Param("query") ProjectVOQuery query);

    /**
     * VO列表
     */
    List<ProjectVO> voList(@Param("query") ProjectVOQuery query);

}