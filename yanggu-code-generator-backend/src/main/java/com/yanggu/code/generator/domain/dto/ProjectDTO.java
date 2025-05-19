package com.yanggu.code.generator.domain.dto;

import com.yanggu.code.generator.common.validation.enumd.path.UnixPath;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

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
	 * 项目名称
	 */
	@Schema(description = "项目名称")
	@NotBlank(message = "项目名称不能为空")
	private String projectName;

	/**
	 * 项目包名
	 */
	@Schema(description = "项目包名")
	@NotBlank(message = "项目包名不能为空")
	private String projectPackage;

	/**
	 * 项目版本
	 */
	@Schema(description = "项目版本")
	@NotBlank(message = "项目版本不能为空")
	private String projectVersion;

	/**
	 * 数据源ID
	 */
	@Schema(description = "数据源ID")
	@NotNull(message = "数据源ID不能为空")
	private Long datasourceId;

	/**
	 * 项目模板组ID
	 */
	@Schema(description = "项目模板组ID")
	@NotNull(message = "项目模板组ID不能为空")
	private Long projectTemplateGroupId;

	/**
	 * 表模板组ID
	 */
	@Schema(description = "表模板组ID")
	@NotNull(message = "表模板组ID不能为空")
	private Long tableTemplateGroupId;

	/**
	 * 生成方式（0-zip压缩包，1-服务器本地）
	 */
	@Schema(description = "生成方式（0-zip压缩包，1-服务器本地）")
	private Integer generatorType;

	/**
	 * 后端路径
	 */
	@Schema(description = "后端路径")
	@UnixPath(message = "后端路径格式不正确")
	private String backendPath;

	/**
	 * 前端路径
	 */
	@Schema(description = "前端路径")
	@UnixPath(message = "前端路径格式不正确")
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

}