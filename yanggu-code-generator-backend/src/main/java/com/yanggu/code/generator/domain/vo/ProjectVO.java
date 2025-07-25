package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.common.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 项目VO实体类
 */
@Data
@Schema(description = "项目VO实体类")
@EqualsAndHashCode(callSuper = true)
public class ProjectVO extends BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
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
     * 枚举模板组ID
     */
    @Schema(description = "枚举模板组ID")
    private Long enumTemplateGroupId;

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
     * Entity基类ID
     */
    @Schema(description = "Entity基类ID")
    private Long entityBaseClassId;

    /**
     * VO基类ID
     */
    @Schema(description = "VO基类ID")
    private Long voBaseClassId;

    /**
     * 生成方式（0-zip压缩包，1-服务器本地）
     */
    @Schema(description = "生成方式（0-zip压缩包，1-服务器本地）")
    private Integer generatorType;

}