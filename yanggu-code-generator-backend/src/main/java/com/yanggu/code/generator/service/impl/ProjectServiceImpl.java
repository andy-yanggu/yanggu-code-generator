package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.mapstruct.ProjectMapstruct;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.query.ProjectVOQuery;
import com.yanggu.code.generator.domain.query.ProjectEntityQuery;
import com.yanggu.code.generator.domain.dto.ProjectDTO;
import com.yanggu.code.generator.domain.vo.ProjectVO;
import com.yanggu.code.generator.mapper.ProjectMapper;
import com.yanggu.code.generator.service.ProjectService;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 项目Service实现类
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, ProjectEntity> implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectMapstruct projectMapstruct;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(ProjectDTO dto) {
        ProjectEntity entity = projectMapstruct.dtoToEntity(dto);
        //唯一性校验等
        projectMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(ProjectDTO dto) {
        ProjectEntity formEntity = projectMapstruct.dtoToEntity(dto);
        ProjectEntity dbEntity = selectById(dto.getId());
        //唯一性校验等
        projectMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        ProjectEntity dbEntity = selectById(id);
        //删除校验和关联删除
        projectMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        //删除校验和关联删除
        projectMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public ProjectVO detail(Long id) {
        ProjectEntity dbEntity = selectById(id);
        return projectMapstruct.entityToVO(dbEntity);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<ProjectVO> entityPage(ProjectEntityQuery query) {
        //简单sql使用QueryWrapper
        projectMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //projectMapper.entityPage(query);
        return projectMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<ProjectVO> entityList(ProjectEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<ProjectEntity> entityList = projectMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<ProjectEntity> entityList = projectMapper.entityList(query);
        return projectMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<ProjectVO> voPage(ProjectVOQuery query) {
        projectMapper.voPage(query);
        return projectMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<ProjectVO> voList(ProjectVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return projectMapper.voList(query);
    }

    /**
     * 批量查询
     */
    @Override
    public List<ProjectVO> detailList(List<Long> idList) {
        List<ProjectEntity> entityList = projectMapper.selectByIds(idList);
        return projectMapstruct.entityToVO(entityList);
    }

    private ProjectEntity selectById(Long id) {
        ProjectEntity entity = projectMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "项目", id);
        }
        return entity;
    }

    private LambdaQueryWrapper<ProjectEntity> buildQueryWrapper(ProjectEntityQuery query) {
        LambdaQueryWrapper<ProjectEntity> wrapper = Wrappers.lambdaQuery(ProjectEntity.class);

        //过滤字段
        wrapper.like(StrUtil.isNotBlank(query.getProjectName()), ProjectEntity::getProjectName, query.getProjectName());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}