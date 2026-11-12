package com.yanggu.code.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.yanggu.code.generator.domain.entity.EnumItemEntity;
import com.yanggu.code.generator.domain.query.EnumItemQuery;
import com.yanggu.code.generator.domain.vo.EnumItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 枚举项Mapper
 */
@Mapper
@Repository
public interface EnumItemMapper extends BaseMapperPlus<EnumItemEntity> {

    /**
     * Entity分页
     */
    IPage<EnumItemEntity> entityPage(IPage<EnumItemEntity> page, @Param("query") EnumItemQuery query);

    /**
     * Entity列表
     */
    List<EnumItemEntity> entityList(@Param("query") EnumItemQuery query);

    /**
     * VO分页
     */
    IPage<EnumItemVO> voPage(IPage<EnumItemVO> page, @Param("query") EnumItemQuery query);

    /**
     * VO列表
     */
    List<EnumItemVO> voList(@Param("query") EnumItemQuery query);

}