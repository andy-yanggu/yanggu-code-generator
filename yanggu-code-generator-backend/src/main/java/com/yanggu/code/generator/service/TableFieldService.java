package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yanggu.code.generator.domain.dto.TableFieldDTO;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.query.TableFieldEntityQuery;
import com.yanggu.code.generator.domain.vo.TableFieldVO;

import java.util.List;

/**
 * 表字段服务层
 */
public interface TableFieldService extends IService<TableFieldEntity> {

    /**
     * 简单列表
     */
    List<TableFieldVO> entityList(TableFieldEntityQuery query);

    /**
     * 表字段提交
     */
    void submitList(List<TableFieldDTO> submitList);

    /**
     * 初始化字段数据
     */
    void initFieldList(List<TableFieldEntity> tableFieldList);

    /**
     * 删除字段
     */
    default void deleteByTableIdList(List<Long> tableIdList) {
        LambdaQueryWrapper<TableFieldEntity> queryWrapper = Wrappers.lambdaQuery(TableFieldEntity.class)
                .in(TableFieldEntity::getTableId, tableIdList);
        getBaseMapper().delete(queryWrapper);
    }

    /**
     * 根据表ID查询字段
     */
    default List<TableFieldEntity> getByTableId(Long tableId) {
        LambdaQueryWrapper<TableFieldEntity> queryWrapper = Wrappers.<TableFieldEntity>lambdaQuery()
                .eq(TableFieldEntity::getTableId, tableId);
        return getBaseMapper().selectList(queryWrapper);
    }

}