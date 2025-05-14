package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.entity.FieldTypeEntity;
import com.yanggu.code.generator.enums.AutoFillEnum;
import com.yanggu.code.generator.mapstruct.TableFieldMapstruct;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.query.TableFieldVOQuery;
import com.yanggu.code.generator.domain.query.TableFieldEntityQuery;
import com.yanggu.code.generator.domain.dto.TableFieldDTO;
import com.yanggu.code.generator.domain.vo.TableFieldVO;
import com.yanggu.code.generator.mapper.TableFieldMapper;
import com.yanggu.code.generator.service.FieldTypeService;
import com.yanggu.code.generator.service.TableFieldService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Autowired
    private FieldTypeService fieldTypeService;

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

    @Override
    public void initFieldList(List<TableFieldEntity> tableFieldList) {
        // 字段类型、属性类型映射
        Map<String, FieldTypeEntity> fieldTypeMap = fieldTypeService.getMap();
        int index = 0;
        for (TableFieldEntity field : tableFieldList) {
            field.setAttrName(StringUtils.underlineToCamel(field.getFieldName()));
            // 获取字段对应的类型
            FieldTypeEntity fieldTypeMapping = fieldTypeMap.get(field.getFieldType().toLowerCase());
            if (fieldTypeMapping == null) {
                // 没找到对应的类型，则为Object类型
                field.setAttrType("Object");
            } else {
                field.setAttrType(fieldTypeMapping.getAttrType());
                field.setPackageName(fieldTypeMapping.getPackageName());
            }

            field.setAutoFill(AutoFillEnum.DEFAULT.name());
            field.setFormItem(true);
            field.setGridItem(true);
            field.setQueryType("=");
            field.setQueryFormType("text");
            field.setFormType("text");
            field.setFieldSort(index);
            field.setQueryFieldSort(index);
            field.setFormFieldSort(index);
            field.setGridFieldSort(index);
            index++;
        }
    }

    @Override
    public void deleteByTableIdList(List<Long> tableIdList) {
        LambdaQueryWrapper<TableFieldEntity> queryWrapper = Wrappers.lambdaQuery(TableFieldEntity.class)
                .in(TableFieldEntity::getTableId, tableIdList);
        tableFieldMapper.delete(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void submitList(List<TableFieldDTO> submitList) {
        List<TableFieldEntity> entityList = tableFieldMapstruct.dtoToEntity(submitList);
        this.updateBatchById(entityList);
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

        //过滤字段
        wrapper.eq(Objects.nonNull(query.getTableId()), TableFieldEntity::getTableId, query.getTableId());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}