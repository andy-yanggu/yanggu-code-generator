package com.yanggu.code.generator.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import com.yanggu.code.generator.common.domain.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 模板Entity实体类
 */
@Data
@TableName(value = "gen_template", schema = "yanggu_code_generator")
@EqualsAndHashCode(callSuper = true)
public class TemplateEntity extends BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 模板组ID
	 */
	@TableField(value = "template_group_id")
	private Long templateGroupId;

	/**
	 * 模板名称
	 */
	@TableField(value = "template_name")
	private String templateName;

	/**
	 * 生成代码的路径
	 */
	@TableField(value = "generator_path")
	private String generatorPath;

	/**
	 * 模板描述
	 */
	@TableField(value = "template_desc")
	private String templateDesc;

	/**
	 * 模板内容
	 */
	@TableField(value = "template_content")
	private String templateContent;

	/**
	 * 模板类型（0-文件，1-文件夹）
	 */
	@TableField(value = "template_type")
	private Integer templateType;

}