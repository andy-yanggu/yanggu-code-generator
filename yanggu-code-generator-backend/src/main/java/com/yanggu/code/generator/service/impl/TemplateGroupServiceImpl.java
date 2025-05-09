package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.mapstruct.TemplateGroupMapstruct;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.query.TemplateGroupVOQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupEntityQuery;
import com.yanggu.code.generator.domain.dto.TemplateGroupDTO;
import com.yanggu.code.generator.domain.vo.TemplateGroupVO;
import com.yanggu.code.generator.mapper.TemplateGroupMapper;
import com.yanggu.code.generator.service.TemplateGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 模板组Service实现类
 */
@Service
public class TemplateGroupServiceImpl extends ServiceImpl<TemplateGroupMapper, TemplateGroupEntity> implements TemplateGroupService {

    @Autowired
    private TemplateGroupMapper templateGroupMapper;

    @Autowired
    private TemplateGroupMapstruct templateGroupMapstruct;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(TemplateGroupDTO dto) {
        TemplateGroupEntity entity = templateGroupMapstruct.dtoToEntity(dto);
        //唯一性校验等
        templateGroupMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(TemplateGroupDTO dto) {
        TemplateGroupEntity formEntity = templateGroupMapstruct.dtoToEntity(dto);
        TemplateGroupEntity dbEntity = selectById(dto.getId());
        //唯一性校验等
        templateGroupMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        TemplateGroupEntity dbEntity = selectById(id);
        //删除校验和关联删除
        templateGroupMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        //删除校验和关联删除
        templateGroupMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public TemplateGroupVO detail(Long id) {
        TemplateGroupEntity dbEntity = selectById(id);
        return templateGroupMapstruct.entityToVO(dbEntity);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<TemplateGroupVO> entityPage(TemplateGroupEntityQuery query) {
        //简单sql使用QueryWrapper
        templateGroupMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //templateGroupMapper.entityPage(query);
        return templateGroupMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<TemplateGroupVO> entityList(TemplateGroupEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<TemplateGroupEntity> entityList = templateGroupMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<TemplateGroupEntity> entityList = templateGroupMapper.entityList(query);
        return templateGroupMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<TemplateGroupVO> voPage(TemplateGroupVOQuery query) {
        templateGroupMapper.voPage(query);
        return templateGroupMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<TemplateGroupVO> voList(TemplateGroupVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return templateGroupMapper.voList(query);
    }

    /**
     * 批量查询
     */
    @Override
    public List<TemplateGroupVO> detailList(List<Long> idList) {
        List<TemplateGroupEntity> entityList = templateGroupMapper.selectByIds(idList);
        return templateGroupMapstruct.entityToVO(entityList);
    }

    private TemplateGroupEntity selectById(Long id) {
        TemplateGroupEntity entity = templateGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "模板组", id);
        }
        return entity;
    }

    private LambdaQueryWrapper<TemplateGroupEntity> buildQueryWrapper(TemplateGroupEntityQuery query) {
        LambdaQueryWrapper<TemplateGroupEntity> wrapper = Wrappers.lambdaQuery(TemplateGroupEntity.class);

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}