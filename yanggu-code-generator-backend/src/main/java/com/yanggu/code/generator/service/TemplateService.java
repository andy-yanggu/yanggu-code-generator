package com.yanggu.code.generator.service;

import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import com.yanggu.code.generator.domain.dto.TemplateDTO;
import com.yanggu.code.generator.domain.query.TemplateEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateVOQuery;
import com.yanggu.code.generator.domain.entity.TemplateEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 模板服务层
 */
public interface TemplateService extends IService<TemplateEntity> {

    /**
     * 新增
     */
    void add(TemplateDTO dto);

    /**
     * 修改
     */
    void update(TemplateDTO dto);

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
    TemplateVO detail(Long id);

    /**
     * 批量查询
     */
    List<TemplateVO> detailList(List<Long> idList);

    /**
     * 简单分页
     */
    PageVO<TemplateVO> entityPage(TemplateEntityQuery query);

    /**
     * 简单列表
     */
    List<TemplateVO> entityList(TemplateEntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<TemplateVO> voPage(TemplateVOQuery query);

    /**
     * 复杂列表
     */
    List<TemplateVO> voList(TemplateVOQuery query);

}