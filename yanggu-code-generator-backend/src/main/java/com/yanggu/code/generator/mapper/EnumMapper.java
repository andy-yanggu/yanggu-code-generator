package com.yanggu.code.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.query.EnumQuery;
import com.yanggu.code.generator.domain.vo.EnumVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 枚举Mapper
 */
@Mapper
@Repository
public interface EnumMapper extends BaseMapperPlus<EnumEntity> {

    /**
     * VO分页
     */
    IPage<EnumVO> voPage(IPage<EnumVO> page, @Param("query") EnumQuery query);

    /**
     * VO列表
     */
    List<EnumVO> voList(@Param("query") EnumQuery query);

    List<EnumEntity> selectEnumList(@Param("projectId") Long projectId, @Param("enumIdList") List<Long> enumIdList);

    List<Long> distinctProjectIdList(@Param("idList") List<Long> idList);
}