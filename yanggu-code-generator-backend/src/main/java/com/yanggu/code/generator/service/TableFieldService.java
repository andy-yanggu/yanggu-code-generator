package com.yanggu.code.generator.service;

import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.vo.TableFieldVO;
import com.yanggu.code.generator.domain.dto.TableFieldDTO;
import com.yanggu.code.generator.domain.query.TableFieldEntityQuery;
import com.yanggu.code.generator.domain.query.TableFieldVOQuery;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 表字段服务层
 */
public interface TableFieldService extends IService<TableFieldEntity> {

    /**
     * 新增
     */
    void add(TableFieldDTO dto);

    /**
     * 修改
     */
    void update(TableFieldDTO dto);

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
    TableFieldVO detail(Long id);

    /**
     * 批量查询
     */
    List<TableFieldVO> detailList(List<Long> idList);

    /**
     * 简单分页
     */
    PageVO<TableFieldVO> entityPage(TableFieldEntityQuery query);

    /**
     * 简单列表
     */
    List<TableFieldVO> entityList(TableFieldEntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<TableFieldVO> voPage(TableFieldVOQuery query);

    /**
     * 复杂列表
     */
    List<TableFieldVO> voList(TableFieldVOQuery query);

}