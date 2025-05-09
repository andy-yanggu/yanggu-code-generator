package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 表DTO实体类
 */
@Data
@Schema(description = "表DTO实体类")
public class TableDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@Schema(description = "id")
	private Long id;

	/**
	 * 表名
	 */
	@Schema(description = "表名")
	private String tableName;

	/**
	 * 数据库名
	 */
	@Schema(description = "数据库名")
	private String databaseName;

	/**
	 * 类名
	 */
	@Schema(description = "类名")
	private String className;

	/**
	 * 说明
	 */
	@Schema(description = "说明")
	private String tableComment;

	/**
	 * 项目ID
	 */
	@Schema(description = "项目ID")
	private Long projectId;

	/**
	 * 作者
	 */
	@Schema(description = "作者")
	private String author;

	/**
	 * 项目版本号
	 */
	@Schema(description = "项目版本号")
	private String version;

	/**
	 * 功能名
	 */
	@Schema(description = "功能名")
	private String functionName;

	/**
	 * 表单布局  1：一列   2：两列
	 */
	@Schema(description = "表单布局  1：一列   2：两列")
	private Integer formLayout;

}