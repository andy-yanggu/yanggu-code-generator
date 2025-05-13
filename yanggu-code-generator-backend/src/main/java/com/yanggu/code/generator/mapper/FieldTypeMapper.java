package com.yanggu.code.generator.mapper;

import com.yanggu.code.generator.common.mybatis.mapper.BaseMapperPlus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yanggu.code.generator.domain.entity.FieldTypeEntity;
import com.yanggu.code.generator.domain.vo.FieldTypeVO;
import com.yanggu.code.generator.domain.query.FieldTypeEntityQuery;
import com.yanggu.code.generator.domain.query.FieldTypeVOQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 字段类型Mapper
 */
@Mapper
@Repository
public interface FieldTypeMapper extends BaseMapperPlus<FieldTypeEntity> {

    /**
     * Entity分页
     */
    IPage<FieldTypeEntity> entityPage(@Param("query") FieldTypeEntityQuery query);

    /**
     * Entity列表
     */
    List<FieldTypeEntity> entityList(@Param("query") FieldTypeEntityQuery query);

    /**
     * VO分页
     */
    IPage<FieldTypeVO> voPage(@Param("query") FieldTypeVOQuery query);

    /**
     * VO列表
     */
    List<FieldTypeVO> voList(@Param("query") FieldTypeVOQuery query);

    /**
     * 根据tableId，获取包列表
     */
    Set<String> getPackageByTableId(Long tableId);

    List<String> distinctAttrTypeList();
}