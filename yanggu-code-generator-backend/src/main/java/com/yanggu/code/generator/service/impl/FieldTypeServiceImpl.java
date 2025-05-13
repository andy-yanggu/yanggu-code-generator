package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.mapstruct.FieldTypeMapstruct;
import com.yanggu.code.generator.domain.entity.FieldTypeEntity;
import com.yanggu.code.generator.domain.query.FieldTypeVOQuery;
import com.yanggu.code.generator.domain.query.FieldTypeEntityQuery;
import com.yanggu.code.generator.domain.dto.FieldTypeDTO;
import com.yanggu.code.generator.domain.vo.FieldTypeVO;
import com.yanggu.code.generator.mapper.FieldTypeMapper;
import com.yanggu.code.generator.service.FieldTypeService;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 字段类型Service实现类
 */
@Service
public class FieldTypeServiceImpl extends ServiceImpl<FieldTypeMapper, FieldTypeEntity> implements FieldTypeService {

    @Autowired
    private FieldTypeMapper fieldTypeMapper;

    @Autowired
    private FieldTypeMapstruct fieldTypeMapstruct;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(FieldTypeDTO dto) {
        FieldTypeEntity entity = fieldTypeMapstruct.dtoToEntity(dto);
        //唯一性校验等
        checkExist(dto);
        fieldTypeMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(FieldTypeDTO dto) {
        FieldTypeEntity formEntity = fieldTypeMapstruct.dtoToEntity(dto);
        FieldTypeEntity dbEntity = selectById(dto.getId());
        //唯一性校验等
        checkExist(dto);
        fieldTypeMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        FieldTypeEntity dbEntity = selectById(id);
        //删除校验和关联删除
        fieldTypeMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        //删除校验和关联删除
        fieldTypeMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public FieldTypeVO detail(Long id) {
        FieldTypeEntity dbEntity = selectById(id);
        return fieldTypeMapstruct.entityToVO(dbEntity);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<FieldTypeVO> entityPage(FieldTypeEntityQuery query) {
        //简单sql使用QueryWrapper
        fieldTypeMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //fieldTypeMapper.entityPage(query);
        return fieldTypeMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<FieldTypeVO> entityList(FieldTypeEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<FieldTypeEntity> entityList = fieldTypeMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<FieldTypeEntity> entityList = fieldTypeMapper.entityList(query);
        return fieldTypeMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<FieldTypeVO> voPage(FieldTypeVOQuery query) {
        fieldTypeMapper.voPage(query);
        return fieldTypeMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<FieldTypeVO> voList(FieldTypeVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return fieldTypeMapper.voList(query);
    }

    @Override
    public Map<String, FieldTypeEntity> getMap() {
        List<FieldTypeEntity> list = baseMapper.selectList();
        Map<String, FieldTypeEntity> map = new LinkedHashMap<>(list.size());
        for (FieldTypeEntity entity : list) {
            map.put(entity.getColumnType().toLowerCase(), entity);
        }
        return map;
    }

    @Override
    public Set<String> getPackageByTableId(Long tableId) {
        Set<String> importList = baseMapper.getPackageByTableId(tableId);

        return importList.stream().filter(cn.hutool.core.util.StrUtil::isNotBlank).collect(Collectors.toSet());
    }

    /**
     * 批量查询
     */
    @Override
    public List<FieldTypeVO> detailList(List<Long> idList) {
        List<FieldTypeEntity> entityList = fieldTypeMapper.selectByIds(idList);
        return fieldTypeMapstruct.entityToVO(entityList);
    }

    private FieldTypeEntity selectById(Long id) {
        FieldTypeEntity entity = fieldTypeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "字段类型", id);
        }
        return entity;
    }

    private void checkExist(FieldTypeDTO dto) {
        LambdaQueryWrapper<FieldTypeEntity> queryWrapper = Wrappers.lambdaQuery(FieldTypeEntity.class);
        queryWrapper.ne(dto.getId() != null, FieldTypeEntity::getId, dto.getId());
        queryWrapper.eq(FieldTypeEntity::getColumnType, dto.getColumnType());
        boolean exists = fieldTypeMapper.exists(queryWrapper);
        if (exists) {
            throw new BusinessException("字段类型已存在");
        }
    }

    private LambdaQueryWrapper<FieldTypeEntity> buildQueryWrapper(FieldTypeEntityQuery query) {
        LambdaQueryWrapper<FieldTypeEntity> wrapper = Wrappers.lambdaQuery(FieldTypeEntity.class);

        //过滤字段
        wrapper.like(StrUtil.isNotBlank(query.getColumnType()), FieldTypeEntity::getColumnType, query.getColumnType());
        wrapper.like(StrUtil.isNotBlank(query.getAttrType()), FieldTypeEntity::getAttrType, query.getAttrType());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}