package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.dto.EnumDTO;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.query.EnumQuery;
import com.yanggu.code.generator.domain.vo.EnumGenerateCheckVO;
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
     * 分页（完整VO，含关联字段）
     */
    PageVO<EnumVO> page(EnumQuery query);

    /**
     * 列表（完整VO，含关联字段）
     */
    List<EnumVO> list(EnumQuery query);

    List<EnumEntity> enumList(Long projectId);

    EnumEntity getById(Long id);

    EnumGenerateCheckVO generateCheck(List<Long> idList);

    void deleteByProjectId(List<Long> idList);

}