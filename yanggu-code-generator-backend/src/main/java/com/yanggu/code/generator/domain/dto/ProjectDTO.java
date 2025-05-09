package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 项目DTO实体类
 */
@Data
@Schema(description = "项目DTO实体类")
public class ProjectDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * ID主键自增
	 */
	@Schema(description = "ID主键自增")
	private Long id;

	/**
	 * 项目名
	 */
	@Schema(description = "项目名")
	private String projectName;

	/**
	 * 项目包名
	 */
	@Schema(description = "项目包名")
	private String projectPackage;

	/**
	 * 项目版本
	 */
	@Schema(description = "项目版本")
	private String projectVersion;

	/**
	 * 数据源ID
	 */
	@Schema(description = "数据源ID")
	private Long datasourceId;

	/**
	 * 项目模板组ID
	 */
	@Schema(description = "项目模板组ID")
	private Long projectTemplateGroupId;

	/**
	 * 表模板组ID
	 */
	@Schema(description = "表模板组ID")
	private Long tableTemplateGroupId;

	/**
	 * 后端路径
	 */
	@Schema(description = "后端路径")
	private String backendPath;

	/**
	 * 前端路径
	 */
	@Schema(description = "前端路径")
	private String frontendPath;

	/**
	 * 项目描述
	 */
	@Schema(description = "项目描述")
	private String projectDesc;

	/**
	 * 作者
	 */
	@Schema(description = "作者")
	private String author;

	/**
	 * 基类ID
	 */
	@Schema(description = "基类ID")
	private Long baseClassId;

	/**
	 * 生成方式（0-zip压缩包，1-服务器本地）
	 */
	@Schema(description = "生成方式（0-zip压缩包，1-服务器本地）")
	private Integer generatorType;

}