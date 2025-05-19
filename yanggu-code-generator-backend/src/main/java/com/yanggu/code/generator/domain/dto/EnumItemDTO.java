package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 枚举项DTO实体类
 */
@Data
@Schema(description = "枚举项DTO实体类")
public class EnumItemDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@Schema(description = "id")
	private Long id;

	/**
	 * 枚举ID
	 */
	@Schema(description = "枚举ID")
	private Long enumId;

	/**
	 * 枚举项名称
	 */
	@Schema(description = "枚举项名称")
	private String enumItemName;

	/**
	 * 枚举项编码
	 */
	@Schema(description = "枚举项编码")
	private String enumItemCode;

	/**
	 * 枚举项描述
	 */
	@Schema(description = "枚举项描述")
	private String enumItemDesc;

}