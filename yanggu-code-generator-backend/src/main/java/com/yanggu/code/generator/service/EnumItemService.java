package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.dto.EnumItemDTO;
import com.yanggu.code.generator.domain.entity.EnumItemEntity;
import com.yanggu.code.generator.domain.query.EnumItemEntityQuery;
import com.yanggu.code.generator.domain.query.EnumItemVOQuery;
import com.yanggu.code.generator.domain.vo.EnumItemVO;

import java.util.List;

/**
 * 枚举项服务层
 */
public interface EnumItemService extends IService<EnumItemEntity> {

    /**
     * 新增
     */
    void add(EnumItemDTO dto);

    /**
     * 修改
     */
    void update(EnumItemDTO dto);

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
    EnumItemVO detail(Long id);

    /**
     * 批量查询
     */
    List<EnumItemVO> detailList(List<Long> idList);

    /**
     * 简单分页
     */
    PageVO<EnumItemVO> entityPage(EnumItemEntityQuery query);

    /**
     * 简单列表
     */
    List<EnumItemVO> entityList(EnumItemEntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<EnumItemVO> voPage(EnumItemVOQuery query);

    /**
     * 复杂列表
     */
    List<EnumItemVO> voList(EnumItemVOQuery query);

}