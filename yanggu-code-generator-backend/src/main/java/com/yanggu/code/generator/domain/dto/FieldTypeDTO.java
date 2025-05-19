package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字段类型DTO实体类
 */
@Data
@Schema(description = "字段类型DTO实体类")
public class FieldTypeDTO implements Serializable {

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