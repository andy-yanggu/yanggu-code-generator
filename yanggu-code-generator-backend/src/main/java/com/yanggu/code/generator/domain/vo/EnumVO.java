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
     * 主键ID
     */
    @Schema(description = "主键ID")
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

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String projectName;

    /**
     * 枚举模板组ID
     */
    @Schema(description = "枚举模板组ID")
    private Long enumTemplateGroupId;

    /**
     * 生成方式（0-zip压缩包，1-服务器本地）
     */
    @Schema(description = "生成方式（0-zip压缩包，1-服务器本地）")
    private Integer generatorType;

}