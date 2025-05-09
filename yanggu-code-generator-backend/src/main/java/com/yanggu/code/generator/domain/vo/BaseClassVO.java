package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.yanggu.code.generator.common.domain.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 基类VO实体类
 */
@Data
@Schema(description = "基类VO实体类")
@EqualsAndHashCode(callSuper = true)
public class BaseClassVO extends BaseVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@Schema(description = "id")
	private Long id;

	/**
	 * 基类包名
	 */
	@Schema(description = "基类包名")
	private String packageName;

	/**
	 * 基类编码
	 */
	@Schema(description = "基类编码")
	private String code;

	/**
	 * 基类字段，多个用英文逗号分隔
	 */
	@Schema(description = "基类字段，多个用英文逗号分隔")
	private String fields;

	/**
	 * 备注
	 */
	@Schema(description = "备注")
	private String remark;

}