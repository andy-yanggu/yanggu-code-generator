package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.dto.EnumItemDTO;
import com.yanggu.code.generator.domain.entity.EnumItemEntity;
import com.yanggu.code.generator.domain.query.EnumItemQuery;
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
     * 分页（完整VO）
     */
    PageVO<EnumItemVO> page(EnumItemQuery query);

    /**
     * 列表（完整VO）
     */
    List<EnumItemVO> list(EnumItemQuery query);

    List<EnumItemEntity> selectByEnumId(Long enumId);

}