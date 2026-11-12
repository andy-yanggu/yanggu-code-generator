package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.dto.ProjectDTO;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.query.ProjectQuery;
import com.yanggu.code.generator.domain.query.ProjectTableQuery;
import com.yanggu.code.generator.domain.vo.ProjectVO;
import com.yanggu.code.generator.domain.vo.TableImportVO;

import java.util.List;

/**
 * 项目服务层
 */
public interface ProjectService extends IService<ProjectEntity> {

    /**
     * 新增
     */
    void add(ProjectDTO dto) throws Exception;

    /**
     * 修改
     */
    void update(ProjectDTO dto);

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
    ProjectVO detail(Long id);

    /**
     * 批量查询
     */
    List<ProjectVO> detailList(List<Long> idList);

    /**
     * 简单分页
     */
    PageVO<ProjectVO> entityPage(ProjectQuery query);

    /**
     * 简单列表
     */
    List<ProjectVO> entityList(ProjectQuery query);

    /**
     * 复杂分页
     */
    PageVO<ProjectVO> voPage(ProjectQuery query);

    /**
     * 复杂列表
     */
    List<ProjectVO> voList(ProjectQuery query);

    /**
     * 项目下的表
     */
    List<TableImportVO> tableList(ProjectTableQuery query) throws Exception;

}