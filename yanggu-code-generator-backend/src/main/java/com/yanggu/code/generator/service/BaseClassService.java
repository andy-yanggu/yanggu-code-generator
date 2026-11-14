package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.dto.BaseClassDTO;
import com.yanggu.code.generator.domain.entity.BaseClassEntity;
import com.yanggu.code.generator.domain.query.BaseClassQuery;
import com.yanggu.code.generator.domain.vo.BaseClassVO;

import java.util.List;

/**
 * 基类服务层
 */
public interface BaseClassService extends IService<BaseClassEntity> {

    /**
     * 新增
     */
    void add(BaseClassDTO dto);

    /**
     * 修改
     */
    void update(BaseClassDTO dto);

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
    BaseClassVO detail(Long id);

    /**
     * 批量查询
     */
    List<BaseClassVO> detailList(List<Long> idList);

    /**
     * 分页（完整VO）
     */
    PageVO<BaseClassVO> page(BaseClassQuery query);

    /**
     * 列表（完整VO）
     */
    List<BaseClassVO> list(BaseClassQuery query);

}