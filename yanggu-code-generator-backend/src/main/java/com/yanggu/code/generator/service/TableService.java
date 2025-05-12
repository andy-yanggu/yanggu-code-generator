package com.yanggu.code.generator.service;

import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.dto.TableImportDTO;
import com.yanggu.code.generator.domain.vo.TableVO;
import com.yanggu.code.generator.domain.dto.TableDTO;
import com.yanggu.code.generator.domain.query.TableEntityQuery;
import com.yanggu.code.generator.domain.query.TableVOQuery;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 表服务层
 */
public interface TableService extends IService<TableEntity> {

    /**
     * 新增
     */
    void add(TableDTO dto);

    /**
     * 修改
     */
    void update(TableDTO dto);

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
    TableVO detail(Long id);

    /**
     * 批量查询
     */
    List<TableVO> detailList(List<Long> idList);

    /**
     * 简单分页
     */
    PageVO<TableVO> entityPage(TableEntityQuery query);

    /**
     * 简单列表
     */
    List<TableVO> entityList(TableEntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<TableVO> voPage(TableVOQuery query);

    /**
     * 复杂列表
     */
    List<TableVO> voList(TableVOQuery query);

    /**
     * 导入表
     */
    void importTable(TableImportDTO importDTO) throws Exception;

}