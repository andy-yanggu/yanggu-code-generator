package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.common.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 枚举项VO实体类
 */
@Data
@Schema(description = "枚举项VO实体类")
@EqualsAndHashCode(callSuper = true)
public class EnumItemVO extends BaseVO implements Serializable {

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