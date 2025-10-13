package com.yanggu.code.generator.service;

import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.vo.TemplateGroupPropertyVO;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyDTO;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyVOQuery;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 模板组属性服务层
 */
public interface TemplateGroupPropertyService extends IService<TemplateGroupPropertyEntity> {

    /**
     * 新增
     */
    TemplateGroupPropertyEntity add(TemplateGroupPropertyDTO dto);

    /**
     * 修改
     */
    void update(TemplateGroupPropertyDTO dto);

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
    TemplateGroupPropertyVO detail(Long id);

    /**
     * 详情列表
     */
    List<TemplateGroupPropertyVO> detailList(List<Long> idList);

    /**
     * 简单分页
     */
    PageVO<TemplateGroupPropertyVO> entityPage(TemplateGroupPropertyEntityQuery query);

    /**
     * 简单列表
     */
    List<TemplateGroupPropertyVO> entityList(TemplateGroupPropertyEntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<TemplateGroupPropertyVO> voPage(TemplateGroupPropertyVOQuery query);

    /**
     * 复杂列表
     */
    List<TemplateGroupPropertyVO> voList(TemplateGroupPropertyVOQuery query);

}