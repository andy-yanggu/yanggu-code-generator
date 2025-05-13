package com.yanggu.code.generator.service;

import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.vo.FieldTypeVO;
import com.yanggu.code.generator.domain.dto.FieldTypeDTO;
import com.yanggu.code.generator.domain.query.FieldTypeEntityQuery;
import com.yanggu.code.generator.domain.query.FieldTypeVOQuery;
import com.yanggu.code.generator.domain.entity.FieldTypeEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字段类型服务层
 */
public interface FieldTypeService extends IService<FieldTypeEntity> {

    /**
     * 新增
     */
    void add(FieldTypeDTO dto);

    /**
     * 修改
     */
    void update(FieldTypeDTO dto);

    /**
     * 删除
     */
    void delete(Long id);

    /**
     * 批量删除
     */
    void deleteList(List<Long> idList);

    /**
     * 详情
     */
    FieldTypeVO detail(Long id);

    /**
     * 批量查询
     */
    List<FieldTypeVO> detailList(List<Long> idList);

    /**
     * 简单分页
     */
    PageVO<FieldTypeVO> entityPage(FieldTypeEntityQuery query);

    /**
     * 简单列表
     */
    List<FieldTypeVO> entityList(FieldTypeEntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<FieldTypeVO> voPage(FieldTypeVOQuery query);

    /**
     * 复杂列表
     */
    List<FieldTypeVO> voList(FieldTypeVOQuery query);

    Map<String, FieldTypeEntity> getMap();

    /**
     * 根据tableId，获取包列表
     *
     * @param tableId 表ID
     * @return 返回包列表
     */
    Set<String> getPackageByTableId(Long tableId);

}