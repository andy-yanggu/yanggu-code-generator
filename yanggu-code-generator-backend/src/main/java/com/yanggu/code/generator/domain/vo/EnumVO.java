package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.common.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 枚举VO实体类
 */
@Data
@Schema(description = "枚举VO实体类")
@EqualsAndHashCode(callSuper = true)
public class EnumVO extends BaseVO implements Serializable {

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