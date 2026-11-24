package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.IService;
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
     * 分页（完整VO，含关联字段）
     */
    PageVO<ProjectVO> page(ProjectQuery query);

    /**
     * 列表（完整VO，含关联字段）
     */
    List<ProjectVO> list(ProjectQuery query);

    /**
     * 项目下的表
     */
    List<TableImportVO> tableList(ProjectTableQuery query) throws Exception;

    /**
     * 绑定了指定项目模板组的项目
     */
    default List<ProjectEntity> listByProjectTemplateGroupId(Long templateGroupId) {
        LambdaQueryWrapper<ProjectEntity> queryWrapper = Wrappers.lambdaQuery(ProjectEntity.class)
                .eq(ProjectEntity::getProjectTemplateGroupId, templateGroupId);
        return getBaseMapper().selectList(queryWrapper);
    }

    /**
     * 绑定了指定表模板组的项目
     */
    default List<ProjectEntity> listByTableTemplateGroupId(Long templateGroupId) {
        LambdaQueryWrapper<ProjectEntity> queryWrapper = Wrappers.lambdaQuery(ProjectEntity.class)
                .eq(ProjectEntity::getTableTemplateGroupId, templateGroupId);
        return getBaseMapper().selectList(queryWrapper);
    }

    /**
     * 绑定了指定枚举模板组的项目
     */
    default List<ProjectEntity> listByEnumTemplateGroupId(Long templateGroupId) {
        LambdaQueryWrapper<ProjectEntity> queryWrapper = Wrappers.lambdaQuery(ProjectEntity.class)
                .eq(ProjectEntity::getEnumTemplateGroupId, templateGroupId);
        return getBaseMapper().selectList(queryWrapper);
    }

}