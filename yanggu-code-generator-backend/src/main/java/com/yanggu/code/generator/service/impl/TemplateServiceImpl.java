package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.dto.TemplateDTO;
import com.yanggu.code.generator.domain.entity.TemplateEntity;
import com.yanggu.code.generator.domain.query.TemplateEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import com.yanggu.code.generator.mapper.TemplateMapper;
import com.yanggu.code.generator.mapstruct.TemplateMapstruct;
import com.yanggu.code.generator.service.TemplateService;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 模板Service实现类
 */
@Service
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, TemplateEntity> implements TemplateService {

    @Autowired
    private TemplateMapper templateMapper;

    @Autowired
    private TemplateMapstruct templateMapstruct;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(TemplateDTO dto) {
        TemplateEntity entity = templateMapstruct.dtoToEntity(dto);
        //唯一性校验等
        templateMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(TemplateDTO dto) {
        TemplateEntity formEntity = templateMapstruct.dtoToEntity(dto);
        TemplateEntity dbEntity = selectById(dto.getId());
        //唯一性校验等
        templateMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        TemplateEntity dbEntity = selectById(id);
        //删除校验和关联删除
        templateMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        //删除校验和关联删除
        templateMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public TemplateVO detail(Long id) {
        TemplateEntity dbEntity = selectById(id);
        return templateMapstruct.entityToVO(dbEntity);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<TemplateVO> entityPage(TemplateEntityQuery query) {
        //简单sql使用QueryWrapper
        templateMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //templateMapper.entityPage(query);
        return templateMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<TemplateVO> entityList(TemplateEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<TemplateEntity> entityList = templateMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<TemplateEntity> entityList = templateMapper.entityList(query);
        return templateMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<TemplateVO> voPage(TemplateVOQuery query) {
        templateMapper.voPage(query);
        return templateMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<TemplateVO> voList(TemplateVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return templateMapper.voList(query);
    }

    @Override
    public List<TemplateEntity> selectByGroupId(Long groupId) {
        LambdaQueryWrapper<TemplateEntity> queryWrapper = Wrappers.lambdaQuery(TemplateEntity.class)
                .eq(TemplateEntity::getTemplateGroupId, groupId);
        return templateMapper.selectList(queryWrapper);
    }

    /**
     * 批量查询
     */
    @Override
    public List<TemplateVO> detailList(List<Long> idList) {
        List<TemplateEntity> entityList = templateMapper.selectByIds(idList);
        return templateMapstruct.entityToVO(entityList);
    }

    private TemplateEntity selectById(Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "模板", id);
        }
        return entity;
    }

    private LambdaQueryWrapper<TemplateEntity> buildQueryWrapper(TemplateEntityQuery query) {
        LambdaQueryWrapper<TemplateEntity> wrapper = Wrappers.lambdaQuery(TemplateEntity.class);

        //过滤字段
        wrapper.eq(Objects.nonNull(query.getTemplateGroupId()), TemplateEntity::getTemplateGroupId, query.getTemplateGroupId());
        wrapper.like(StrUtil.isNotBlank(query.getTemplateName()), TemplateEntity::getTemplateName, query.getTemplateName());
        wrapper.eq(Objects.nonNull(query.getTemplateType()), TemplateEntity::getTemplateType, query.getTemplateType());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}