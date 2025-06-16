package com.yanggu.code.generator.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yanggu.code.generator.common.domain.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 表字段Entity实体类
 */
@Data
@TableName(value = "gen_table_field")
@EqualsAndHashCode(callSuper = true)
public class TableFieldEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 表ID
     */
    @TableField(value = "table_id")
    private Long tableId;

    /**
     * 字段名称
     */
    @TableField(value = "field_name")
    private String fieldName;

    /**
     * 字段类型
     */
    @TableField(value = "field_type")
    private String fieldType;

    /**
     * 字段说明
     */
    @TableField(value = "field_comment")
    private String fieldComment;

    /**
     * 属性名
     */
    @TableField(value = "attr_name")
    private String attrName;

    /**
     * 属性类型
     */
    @TableField(value = "attr_type")
    private String attrType;

    /**
     * 属性包名
     */
    @TableField(value = "package_name")
    private String packageName;

    /**
     * 字段顺序
     */
    @TableField(value = "field_sort")
    private Integer fieldSort;

    /**
     * 自动填充  DEFAULT、INSERT、UPDATE、INSERT_UPDATE
     */
    @TableField(value = "auto_fill")
    private String autoFill;

    /**
     * 主键 0：否  1：是
     */
    @TableField(value = "primary_pk")
    private Integer primaryPk;

    /**
     * 逻辑删除 0：否  1：是
     */
    @TableField(value = "logic_delete")
    private Integer logicDelete;

    /**
     * 逻辑删除值
     */
    @TableField(value = "logic_delete_value")
    private String logicDeleteValue;

    /**
     * 逻辑未删除值
     */
    @TableField(value = "logic_not_delete_value")
    private String logicNotDeleteValue;

    /**
     * 枚举ID
     */
    @TableField(value = "enum_id")
    private Long enumId;

    /**
     * 唯一字段 0：否 1：是
     */
    @TableField(value = "unique_field")
    private Integer uniqueField;

    /**
     * 表单项 0：否  1：是
     */
    @TableField(value = "form_item")
    private Integer formItem;

    /**
     * 表单必填 0：否  1：是
     */
    @TableField(value = "form_required")
    private Integer formRequired;

    /**
     * 表单类型
     */
    @TableField(value = "form_type")
    private String formType;

    /**
     * 表单效验
     */
    @TableField(value = "form_validator")
    private String formValidator;

    /**
     * 表单字段顺序
     */
    @TableField(value = "form_field_sort")
    private Integer formFieldSort;

    /**
     * 列表项 0：否  1：是
     */
    @TableField(value = "grid_item")
    private Integer gridItem;

    /**
     * 列表字段顺序
     */
    @TableField(value = "grid_field_sort")
    private Integer gridFieldSort;

    /**
     * 列表排序 0：否  1：是
     */
    @TableField(value = "grid_sort")
    private Integer gridSort;

    /**
     * 查询项 0：否  1：是
     */
    @TableField(value = "query_item")
    private Integer queryItem;

    /**
     * 查询方式
     */
    @TableField(value = "query_type")
    private String queryType;

    /**
     * 查询字段顺序
     */
    @TableField(value = "query_field_sort")
    private Integer queryFieldSort;

    /**
     * 查询表单类型
     */
    @TableField(value = "query_form_type")
    private String queryFormType;

}