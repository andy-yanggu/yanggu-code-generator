package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.dto.EnumDTO;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.query.EnumEntityQuery;
import com.yanggu.code.generator.domain.query.EnumVOQuery;
import com.yanggu.code.generator.domain.vo.EnumVO;

import java.util.List;

/**
 * 枚举服务层
 */
public interface EnumService extends IService<EnumEntity> {

    /**
     * 新增
     */
    void add(EnumDTO dto);

    /**
     * 修改
     */
    void update(EnumDTO dto);

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
    EnumVO detail(Long id);

    /**
     * 批量查询
     */
    List<EnumVO> detailList(List<Long> idList);

    /**
     * 简单分页
     */
    PageVO<EnumVO> entityPage(EnumEntityQuery query);

    /**
     * 简单列表
     */
    List<EnumVO> entityList(EnumEntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<EnumVO> voPage(EnumVOQuery query);

    /**
     * 复杂列表
     */
    List<EnumVO> voList(EnumVOQuery query);

}