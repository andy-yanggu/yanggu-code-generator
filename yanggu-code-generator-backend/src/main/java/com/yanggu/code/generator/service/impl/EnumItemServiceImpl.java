package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.dto.EnumItemDTO;
import com.yanggu.code.generator.domain.entity.EnumItemEntity;
import com.yanggu.code.generator.domain.query.EnumItemEntityQuery;
import com.yanggu.code.generator.domain.query.EnumItemVOQuery;
import com.yanggu.code.generator.domain.vo.EnumItemVO;
import com.yanggu.code.generator.mapper.EnumItemMapper;
import com.yanggu.code.generator.mapstruct.EnumItemMapstruct;
import com.yanggu.code.generator.service.EnumItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 枚举项Service实现类
 */
@Service
public class EnumItemServiceImpl extends ServiceImpl<EnumItemMapper, EnumItemEntity> implements EnumItemService {

    @Autowired
    private EnumItemMapper enumItemMapper;

    @Autowired
    private EnumItemMapstruct enumItemMapstruct;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(EnumItemDTO dto) {
        EnumItemEntity entity = enumItemMapstruct.dtoToEntity(dto);
        //唯一性校验等
        enumItemMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(EnumItemDTO dto) {
        EnumItemEntity formEntity = enumItemMapstruct.dtoToEntity(dto);
        EnumItemEntity dbEntity = selectById(dto.getId());
        //唯一性校验等
        enumItemMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        EnumItemEntity dbEntity = selectById(id);
        //删除校验和关联删除
        enumItemMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        //删除校验和关联删除
        enumItemMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public EnumItemVO detail(Long id) {
        EnumItemEntity dbEntity = selectById(id);
        return enumItemMapstruct.entityToVO(dbEntity);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<EnumItemVO> entityPage(EnumItemEntityQuery query) {
        //简单sql使用QueryWrapper
        enumItemMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //enumItemMapper.entityPage(query);
        return enumItemMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<EnumItemVO> entityList(EnumItemEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<EnumItemEntity> entityList = enumItemMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<EnumItemEntity> entityList = enumItemMapper.entityList(query);
        return enumItemMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<EnumItemVO> voPage(EnumItemVOQuery query) {
        enumItemMapper.voPage(query);
        return enumItemMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<EnumItemVO> voList(EnumItemVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return enumItemMapper.voList(query);
    }

    @Override
    public List<EnumItemEntity> selectByEnumId(Long enumId) {
        return enumItemMapper.selectList(Wrappers.lambdaQuery(EnumItemEntity.class).eq(EnumItemEntity::getEnumId, enumId));
    }

    /**
     * 批量查询
     */
    @Override
    public List<EnumItemVO> detailList(List<Long> idList) {
        List<EnumItemEntity> entityList = enumItemMapper.selectByIds(idList);
        return enumItemMapstruct.entityToVO(entityList);
    }

    private EnumItemEntity selectById(Long id) {
        EnumItemEntity entity = enumItemMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "枚举项", id);
        }
        return entity;
    }

    private LambdaQueryWrapper<EnumItemEntity> buildQueryWrapper(EnumItemEntityQuery query) {
        LambdaQueryWrapper<EnumItemEntity> wrapper = Wrappers.lambdaQuery(EnumItemEntity.class);

        //过滤字段
        wrapper.eq(MybatisUtil.isNotEmpty(query.getEnumId()), EnumItemEntity::getEnumId, query.getEnumId());
        wrapper.like(MybatisUtil.isNotEmpty(query.getEnumItemName()), EnumItemEntity::getEnumItemName, query.getEnumItemName());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrderItemList());
        return wrapper;
    }

}