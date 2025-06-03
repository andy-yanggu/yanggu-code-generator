package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.code.EnumCode;
import com.yanggu.code.generator.common.validation.group.UpdateGroup;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
 	@NotNull(message = "主键ID不能为空", groups = {UpdateGroup.class})
	private Long id;

	/**
	 * 模板组名称
	 */
	@Schema(description = "模板组名称")
	@NotBlank(message = "模板组名称不能为空")
	private String groupName;

	/**
	 * 模板组类型（0-项目，1-表，2-枚举）
	 */
	@Schema(description = "模板组类型（0-项目，1-表，2-枚举）")
	@NotNull(message = "模板组类型不能为空")
	@EnumCode(TemplateGroupTypeEnum.class)
	private Integer type;

	/**
	 * 模板组描述
	 */
	@Schema(description = "模板组描述")
	private String groupDesc;

}