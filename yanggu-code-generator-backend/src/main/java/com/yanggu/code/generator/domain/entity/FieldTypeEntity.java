package com.yanggu.code.generator.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import com.yanggu.code.generator.common.domain.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 字段类型Entity实体类
 */
@Data
@TableName(value = "gen_field_type")
@EqualsAndHashCode(callSuper = true)
public class FieldTypeEntity extends BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 字段类型
	 */
	@TableField(value = "column_type")
	private String columnType;

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

}