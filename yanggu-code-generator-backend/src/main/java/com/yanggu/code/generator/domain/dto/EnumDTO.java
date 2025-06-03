package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
	@NotNull(message = "id不能为空", groups = {UpdateGroup.class})
	private Long id;

	/**
	 * 枚举名称
	 */
	@Schema(description = "枚举名称")
 	@NotBlank(message = "枚举名称不能为空")
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
	@NotNull(message = "项目ID不能为空")
	private Long projectId;

}