package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyDTO;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateGroupPropertyVO;
import com.yanggu.code.generator.mapper.TemplateGroupPropertyMapper;
import com.yanggu.code.generator.mapstruct.TemplateGroupPropertyMapstruct;
import com.yanggu.code.generator.service.TemplateGroupPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 模板组属性Service实现类
 */
@Service
public class TemplateGroupPropertyServiceImpl extends ServiceImpl<TemplateGroupPropertyMapper, TemplateGroupPropertyEntity> implements TemplateGroupPropertyService {

    @Autowired
    private TemplateGroupPropertyMapper templateGroupPropertyMapper;

    @Autowired
    private TemplateGroupPropertyMapstruct templateGroupPropertyMapstruct;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public TemplateGroupPropertyEntity add(TemplateGroupPropertyDTO dto) {
        TemplateGroupPropertyEntity entity = templateGroupPropertyMapstruct.dtoToEntity(dto);
        templateGroupPropertyMapper.insert(entity);
        return entity;
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(TemplateGroupPropertyDTO dto) {
        TemplateGroupPropertyEntity formEntity = templateGroupPropertyMapstruct.dtoToEntity(dto);
        TemplateGroupPropertyEntity dbEntity = selectById(dto.getId());
        templateGroupPropertyMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        deleteList(List.of(id));
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        List<TemplateGroupPropertyEntity> dbEntityList = templateGroupPropertyMapper.selectByIds(idList);
        //删除校验
        //checkReference(dbEntityList);
        //关联删除
        templateGroupPropertyMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public TemplateGroupPropertyVO detail(Long id) {
        TemplateGroupPropertyEntity dbEntity = selectById(id);
        return templateGroupPropertyMapstruct.entityToVO(dbEntity);
    }

    /**
     * 详情列表
     */
    @Override
    public List<TemplateGroupPropertyVO> detailList(List<Long> idList) {
        List<TemplateGroupPropertyEntity> entityList = templateGroupPropertyMapper.selectByIds(idList);
        return templateGroupPropertyMapstruct.entityToVO(entityList);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<TemplateGroupPropertyVO> entityPage(TemplateGroupPropertyEntityQuery query) {
        //简单sql使用QueryWrapper
        templateGroupPropertyMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //templateGroupPropertyMapper.entityPage(query);
        return templateGroupPropertyMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<TemplateGroupPropertyVO> entityList(TemplateGroupPropertyEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<TemplateGroupPropertyEntity> entityList = templateGroupPropertyMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<TemplateGroupPropertyEntity> entityList = templateGroupPropertyMapper.entityList(query);
        return templateGroupPropertyMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<TemplateGroupPropertyVO> voPage(TemplateGroupPropertyVOQuery query) {
        templateGroupPropertyMapper.voPage(query);
        return templateGroupPropertyMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<TemplateGroupPropertyVO> voList(TemplateGroupPropertyVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return templateGroupPropertyMapper.voList(query);
    }

    /**
     * ID查询
     */
    private TemplateGroupPropertyEntity selectById(Long id) {
        TemplateGroupPropertyEntity entity = templateGroupPropertyMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "模板组属性", id);
        }
        return entity;
    }

    /**
     * 校验能否被删除
     */
    private void checkReference(List<TemplateGroupPropertyEntity> dbEntityList) {
        //TODO 校验是否被引用
        for (TemplateGroupPropertyEntity dbEntity : dbEntityList) {

        }
    }

    private LambdaQueryWrapper<TemplateGroupPropertyEntity> buildQueryWrapper(TemplateGroupPropertyEntityQuery query) {
        LambdaQueryWrapper<TemplateGroupPropertyEntity> wrapper = Wrappers.lambdaQuery(TemplateGroupPropertyEntity.class);

        //过滤字段
        wrapper.like(MybatisUtil.isNotEmpty(query.getPropTitle()), TemplateGroupPropertyEntity::getPropTitle, query.getPropTitle());
        wrapper.like(MybatisUtil.isNotEmpty(query.getPropKey()), TemplateGroupPropertyEntity::getPropKey, query.getPropKey());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrderItemList());
        return wrapper;
    }

}
