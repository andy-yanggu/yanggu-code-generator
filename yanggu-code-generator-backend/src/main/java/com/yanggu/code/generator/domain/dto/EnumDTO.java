package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 枚举DTO实体类
 */
@Data
@Schema(description = "枚举DTO实体类")
public class EnumDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@Schema(description = "id")
	private Long id;

	/**
	 * 枚举名称
	 */
	@Schema(description = "枚举名称")
	private String enumName;

	/**
	 * 枚举描述
	 */
	@Schema(description = "枚举描述")
	private String enumDesc;

	/**
	 * 项目ID
	 */
	@Schema(description = "项目ID")
	private Long projectId;

}