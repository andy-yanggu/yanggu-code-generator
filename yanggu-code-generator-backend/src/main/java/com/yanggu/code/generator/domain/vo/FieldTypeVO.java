package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import com.yanggu.code.generator.common.domain.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 字段类型VO实体类
 */
@Data
@Schema(description = "字段类型VO实体类")
@EqualsAndHashCode(callSuper = true)
public class FieldTypeVO extends BaseVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@Schema(description = "id")
	private Long id;

	/**
	 * 字段类型
	 */
	@Schema(description = "字段类型")
	private String columnType;

	/**
	 * 属性类型
	 */
	@Schema(description = "属性类型")
	private String attrType;

	/**
	 * 属性包名
	 */
	@Schema(description = "属性包名")
	private String packageName;

}