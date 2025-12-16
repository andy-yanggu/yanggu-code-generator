package com.yanggu.code.generator.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yanggu.code.generator.common.domain.entity.BaseEntity;
import com.yanggu.code.generator.common.mybatis.typehandler.list.ListStringTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 基类Entity实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "gen_base_class", autoResultMap = true)
public class BaseClassEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 基类名称
     */
    @TableField(value = "base_class_name")
    private String baseClassName;

    /**
     * 基类包名
     */
    @TableField(value = "package_name")
    private String packageName;

    /**
     * 基类类名
     */
    @TableField(value = "class_name")
    private String className;

    /**
     * 基类字段
     */
    @TableField(value = "field_list", typeHandler = ListStringTypeHandler.class)
    private List<String> fieldList;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

}