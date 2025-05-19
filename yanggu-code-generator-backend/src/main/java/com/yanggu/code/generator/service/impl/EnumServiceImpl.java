package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.dto.EnumDTO;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.EnumItemEntity;
import com.yanggu.code.generator.domain.model.EnumDataModel;
import com.yanggu.code.generator.domain.query.EnumEntityQuery;
import com.yanggu.code.generator.domain.query.EnumVOQuery;
import com.yanggu.code.generator.domain.vo.EnumVO;
import com.yanggu.code.generator.mapper.EnumMapper;
import com.yanggu.code.generator.mapstruct.EnumMapstruct;
import com.yanggu.code.generator.service.EnumItemService;
import com.yanggu.code.generator.service.EnumService;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 枚举Service实现类
 */
@Service
public class EnumServiceImpl extends ServiceImpl<EnumMapper, EnumEntity> implements EnumService {

    @Autowired
    private EnumMapper enumMapper;

    @Autowired
    private EnumMapstruct enumMapstruct;

    @Autowired
    private EnumItemService enumItemService;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(EnumDTO dto) {
        EnumEntity entity = enumMapstruct.dtoToEntity(dto);
        //唯一性校验等
        enumMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(EnumDTO dto) {
        EnumEntity formEntity = enumMapstruct.dtoToEntity(dto);
        EnumEntity dbEntity = selectById(dto.getId());
        //唯一性校验等
        enumMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        EnumEntity dbEntity = selectById(id);
        //删除校验和关联删除
        enumMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        //删除校验和关联删除
        enumMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public EnumVO detail(Long id) {
        EnumEntity dbEntity = selectById(id);
        return enumMapstruct.entityToVO(dbEntity);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<EnumVO> entityPage(EnumEntityQuery query) {
        //简单sql使用QueryWrapper
        enumMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //enumMapper.entityPage(query);
        return enumMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<EnumVO> entityList(EnumEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<EnumEntity> entityList = enumMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<EnumEntity> entityList = enumMapper.entityList(query);
        return enumMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<EnumVO> voPage(EnumVOQuery query) {
        enumMapper.voPage(query);
        return enumMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<EnumVO> voList(EnumVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return enumMapper.voList(query);
    }

    /**
     * 批量查询
     */
    @Override
    public List<EnumVO> detailList(List<Long> idList) {
        List<EnumEntity> entityList = enumMapper.selectByIds(idList);
        return enumMapstruct.entityToVO(entityList);
    }

    @Override
    public List<EnumDataModel> enumList(Long id) {
        return enumMapper.selectEnumList(id);
    }

    @Override
    public EnumEntity getById(Long id) {
        EnumEntity enumEntity = selectById(id);
        List<EnumItemEntity> enumItemList = enumItemService.selectByEnumId(id);
        enumEntity.setEnumItemList(enumItemList);
        return enumEntity;
    }

    private EnumEntity selectById(Long id) {
        EnumEntity entity = enumMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "枚举", id);
        }
        return entity;
    }

    private LambdaQueryWrapper<EnumEntity> buildQueryWrapper(EnumEntityQuery query) {
        LambdaQueryWrapper<EnumEntity> wrapper = Wrappers.lambdaQuery(EnumEntity.class);

        //过滤字段
        wrapper.eq(Objects.nonNull(query.getProjectId()), EnumEntity::getProjectId, query.getProjectId());
        wrapper.eq(StrUtil.isNotBlank(query.getEnumName()), EnumEntity::getEnumName, query.getEnumName());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}