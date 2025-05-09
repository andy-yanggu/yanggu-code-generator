package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.yanggu.code.generator.common.domain.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 模板组VO实体类
 */
@Data
@Schema(description = "模板组VO实体类")
@EqualsAndHashCode(callSuper = true)
public class TemplateGroupVO extends BaseVO implements Serializable {

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