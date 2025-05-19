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
 * 基类Entity实体类
 */
@Data
@TableName(value = "gen_base_class")
@EqualsAndHashCode(callSuper = true)
public class BaseClassEntity extends BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 基类包名
	 */
	@TableField(value = "package_name")
	private String packageName;

	/**
	 * 基类编码
	 */
	@TableField(value = "code")
	private String code;

	/**
	 * 基类字段，多个用英文逗号分隔
	 */
	@TableField(value = "fields")
	private String fields;

	/**
	 * 备注
	 */
	@TableField(value = "remark")
	private String remark;

}