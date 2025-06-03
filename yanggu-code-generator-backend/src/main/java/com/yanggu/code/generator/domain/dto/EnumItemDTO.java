package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
	@NotNull(message = "id不能为空", groups = {UpdateGroup.class})
	private Long id;

	/**
	 * 枚举ID
	 */
	@Schema(description = "枚举ID")
	@NotNull(message = "枚举ID不能为空")
	private Long enumId;

	/**
	 * 枚举项名称
	 */
	@Schema(description = "枚举项名称")
	@NotBlank(message = "枚举项名称不能为空")
	private String enumItemName;

	/**
	 * 枚举项编码
	 */
	@Schema(description = "枚举项编码")
	@NotBlank(message = "枚举项编码不能为空")
	private String enumItemCode;

	/**
	 * 枚举项描述
	 */
	@Schema(description = "枚举项描述")
	@NotBlank(message = "枚举项描述不能为空")
	private String enumItemDesc;

}