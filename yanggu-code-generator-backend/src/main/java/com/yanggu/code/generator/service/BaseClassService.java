package com.yanggu.code.generator.service;

import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.vo.BaseClassVO;
import com.yanggu.code.generator.domain.dto.BaseClassDTO;
import com.yanggu.code.generator.domain.query.BaseClassEntityQuery;
import com.yanggu.code.generator.domain.query.BaseClassVOQuery;
import com.yanggu.code.generator.domain.entity.BaseClassEntity;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * 简单分页
     */
    PageVO<BaseClassVO> entityPage(BaseClassEntityQuery query);

    /**
     * 简单列表
     */
    List<BaseClassVO> entityList(BaseClassEntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<BaseClassVO> voPage(BaseClassVOQuery query);

    /**
     * 复杂列表
     */
    List<BaseClassVO> voList(BaseClassVOQuery query);

}