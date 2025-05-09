package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 模板DTO实体类
 */
@Data
@Schema(description = "模板DTO实体类")
public class TemplateDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@Schema(description = "id")
	private Long id;

	/**
	 * 模板组id
	 */
	@Schema(description = "模板组id")
	private Long templateGroupId;

	/**
	 * 模板名称
	 */
	@Schema(description = "模板名称")
	private String templateName;

	/**
	 * 生成代码的路径
	 */
	@Schema(description = "生成代码的路径")
	private String generatorPath;

	/**
	 * 模板描述
	 */
	@Schema(description = "模板描述")
	private String templateDesc;

	/**
	 * 模板内容
	 */
	@Schema(description = "模板内容")
	private String templateContent;

	/**
	 * 模板类型（0-文件，1-文件夹）
	 */
	@Schema(description = "模板类型（0-文件，1-文件夹）")
	private Integer templateType;

}