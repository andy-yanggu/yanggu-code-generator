package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.mapstruct.TableFieldMapstruct;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.query.TableFieldVOQuery;
import com.yanggu.code.generator.domain.query.TableFieldEntityQuery;
import com.yanggu.code.generator.domain.dto.TableFieldDTO;
import com.yanggu.code.generator.domain.vo.TableFieldVO;
import com.yanggu.code.generator.mapper.TableFieldMapper;
import com.yanggu.code.generator.service.TableFieldService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 表字段Service实现类
 */
@Service
public class TableFieldServiceImpl extends ServiceImpl<TableFieldMapper, TableFieldEntity> implements TableFieldService {

    @Autowired
    private TableFieldMapper tableFieldMapper;

    @Autowired
    private TableFieldMapstruct tableFieldMapstruct;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(TableFieldDTO dto) {
        TableFieldEntity entity = tableFieldMapstruct.dtoToEntity(dto);
        //唯一性校验等
        tableFieldMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(TableFieldDTO dto) {
        TableFieldEntity formEntity = tableFieldMapstruct.dtoToEntity(dto);
        TableFieldEntity dbEntity = selectById(dto.getId());
        //唯一性校验等
        tableFieldMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        TableFieldEntity dbEntity = selectById(id);
        //删除校验和关联删除
        tableFieldMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        //删除校验和关联删除
        tableFieldMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public TableFieldVO detail(Long id) {
        TableFieldEntity dbEntity = selectById(id);
        return tableFieldMapstruct.entityToVO(dbEntity);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<TableFieldVO> entityPage(TableFieldEntityQuery query) {
        //简单sql使用QueryWrapper
        tableFieldMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //tableFieldMapper.entityPage(query);
        return tableFieldMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<TableFieldVO> entityList(TableFieldEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<TableFieldEntity> entityList = tableFieldMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<TableFieldEntity> entityList = tableFieldMapper.entityList(query);
        return tableFieldMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<TableFieldVO> voPage(TableFieldVOQuery query) {
        tableFieldMapper.voPage(query);
        return tableFieldMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<TableFieldVO> voList(TableFieldVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return tableFieldMapper.voList(query);
    }

    /**
     * 批量查询
     */
    @Override
    public List<TableFieldVO> detailList(List<Long> idList) {
        List<TableFieldEntity> entityList = tableFieldMapper.selectByIds(idList);
        return tableFieldMapstruct.entityToVO(entityList);
    }

    private TableFieldEntity selectById(Long id) {
        TableFieldEntity entity = tableFieldMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "表字段", id);
        }
        return entity;
    }

    private LambdaQueryWrapper<TableFieldEntity> buildQueryWrapper(TableFieldEntityQuery query) {
        LambdaQueryWrapper<TableFieldEntity> wrapper = Wrappers.lambdaQuery(TableFieldEntity.class);

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}