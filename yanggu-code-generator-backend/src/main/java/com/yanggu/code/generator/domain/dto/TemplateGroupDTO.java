package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 模板组DTO实体类
 */
@Data
@Schema(description = "模板组DTO实体类")
public class TemplateGroupDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@Schema(description = "主键ID")
	private Long id;

	/**
	 * 模板组名称
	 */
	@Schema(description = "模板组名称")
	private String groupName;

	/**
	 * 模板组类型（0-项目，1-表）
	 */
	@Schema(description = "模板组类型（0-项目，1-表）")
	private Integer type;

	/**
	 * 模板组描述
	 */
	@Schema(description = "模板组描述")
	private String groupDesc;

}