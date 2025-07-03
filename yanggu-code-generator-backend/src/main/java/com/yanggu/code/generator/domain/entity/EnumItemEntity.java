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
 * 枚举项Entity实体类
 */
@Data
@TableName(value = "gen_enum_item", schema = "yanggu_code_generator")
@EqualsAndHashCode(callSuper = true)
public class EnumItemEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 枚举ID
     */
    @TableField(value = "enum_id")
    private Long enumId;

    /**
     * 枚举项名称
     */
    @TableField(value = "enum_item_name")
    private String enumItemName;

    /**
     * 枚举项编码
     */
    @TableField(value = "enum_item_code")
    private String enumItemCode;

    /**
     * 枚举项描述
     */
    @TableField(value = "enum_item_desc")
    private String enumItemDesc;

    /**
     * 枚举项排序
     */
    @TableField(value = "enum_item_order")
    private Integer enumItemOrder;

}