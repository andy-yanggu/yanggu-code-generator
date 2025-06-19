package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.dto.TableFieldDTO;
import com.yanggu.code.generator.domain.entity.FieldTypeEntity;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.query.TableFieldEntityQuery;
import com.yanggu.code.generator.domain.vo.TableFieldVO;
import com.yanggu.code.generator.enums.AutoFillEnum;
import com.yanggu.code.generator.mapper.TableFieldMapper;
import com.yanggu.code.generator.mapstruct.TableFieldMapstruct;
import com.yanggu.code.generator.service.FieldTypeService;
import com.yanggu.code.generator.service.TableFieldService;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void submitList(List<TableFieldDTO> submitList) {
        List<TableFieldEntity> entityList = tableFieldMapstruct.dtoToEntity(submitList);

        // 字段类型、属性类型映射
        Map<String, FieldTypeEntity> fieldTypeMap = fieldTypeService.getMap();
        entityList.forEach(tableField -> {

            //设置包名
            FieldTypeEntity fieldTypeEntity = fieldTypeMap.get(tableField.getFieldType());
            tableField.setPackageName(fieldTypeEntity.getPackageName());

            // 逻辑删除校验
            if (tableField.getLogicDelete() == 1) {
                if (StrUtil.isBlank(tableField.getLogicDeleteValue())) {
                    throw new BusinessException("逻辑删除值不能为空");
                }
                if (StrUtil.isBlank(tableField.getLogicNotDeleteValue())) {
                    throw new BusinessException("逻辑未删除值不能为空");
                }
            }
        });
        this.updateBatchById(entityList);
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
            field.setUniqueField(0);
            field.setFormItem(1);
            field.setGridItem(0);
            field.setQueryItem(0);
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

    private LambdaQueryWrapper<TableFieldEntity> buildQueryWrapper(TableFieldEntityQuery query) {
        LambdaQueryWrapper<TableFieldEntity> wrapper = Wrappers.lambdaQuery(TableFieldEntity.class);

        //过滤字段
        wrapper.eq(MybatisUtil.isNotEmpty(query.getTableId()), TableFieldEntity::getTableId, query.getTableId());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrderItemList());
        return wrapper;
    }

}