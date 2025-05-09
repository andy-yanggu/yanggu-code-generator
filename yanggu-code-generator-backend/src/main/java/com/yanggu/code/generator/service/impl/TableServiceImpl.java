package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.mapstruct.TableMapstruct;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.query.TableVOQuery;
import com.yanggu.code.generator.domain.query.TableEntityQuery;
import com.yanggu.code.generator.domain.dto.TableDTO;
import com.yanggu.code.generator.domain.vo.TableVO;
import com.yanggu.code.generator.mapper.TableMapper;
import com.yanggu.code.generator.service.TableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 表Service实现类
 */
@Service
public class TableServiceImpl extends ServiceImpl<TableMapper, TableEntity> implements TableService {

    @Autowired
    private TableMapper tableMapper;

    @Autowired
    private TableMapstruct tableMapstruct;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(TableDTO dto) {
        TableEntity entity = tableMapstruct.dtoToEntity(dto);
        //唯一性校验等
        tableMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(TableDTO dto) {
        TableEntity formEntity = tableMapstruct.dtoToEntity(dto);
        TableEntity dbEntity = selectById(dto.getId());
        //唯一性校验等
        tableMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        TableEntity dbEntity = selectById(id);
        //删除校验和关联删除
        tableMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        //删除校验和关联删除
        tableMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public TableVO detail(Long id) {
        TableEntity dbEntity = selectById(id);
        return tableMapstruct.entityToVO(dbEntity);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<TableVO> entityPage(TableEntityQuery query) {
        //简单sql使用QueryWrapper
        tableMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //tableMapper.entityPage(query);
        return tableMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<TableVO> entityList(TableEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<TableEntity> entityList = tableMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<TableEntity> entityList = tableMapper.entityList(query);
        return tableMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<TableVO> voPage(TableVOQuery query) {
        tableMapper.voPage(query);
        return tableMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<TableVO> voList(TableVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return tableMapper.voList(query);
    }

    /**
     * 批量查询
     */
    @Override
    public List<TableVO> detailList(List<Long> idList) {
        List<TableEntity> entityList = tableMapper.selectByIds(idList);
        return tableMapstruct.entityToVO(entityList);
    }

    private TableEntity selectById(Long id) {
        TableEntity entity = tableMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "表", id);
        }
        return entity;
    }

    private LambdaQueryWrapper<TableEntity> buildQueryWrapper(TableEntityQuery query) {
        LambdaQueryWrapper<TableEntity> wrapper = Wrappers.lambdaQuery(TableEntity.class);

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}