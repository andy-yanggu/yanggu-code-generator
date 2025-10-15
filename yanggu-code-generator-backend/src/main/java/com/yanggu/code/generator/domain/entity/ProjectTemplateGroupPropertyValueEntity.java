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
 * 项目模板组属性值Entity实体类
 */
@Data
@TableName(value = "gen_project_template_group_property_value")
@EqualsAndHashCode(callSuper = true)
public class ProjectTemplateGroupPropertyValueEntity extends BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 项目ID
	 */
	@TableField(value = "project_id")
	private Long projectId;

	/**
	 * 模板组ID
	 */
	@TableField(value = "template_group_id")
	private Long templateGroupId;

	/**
	 * 模板组属性ID
	 */
	@TableField(value = "template_group_property_id")
	private Long templateGroupPropertyId;

	/**
	 * 模板组属性值
	 */
	@TableField(value = "template_group_property_value")
	private String templateGroupPropertyValue;

}
