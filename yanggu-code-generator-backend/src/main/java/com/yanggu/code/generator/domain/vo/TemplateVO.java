package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.common.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 模板VO实体类
 */
@Data
@Schema(description = "模板VO实体类")
@EqualsAndHashCode(callSuper = true)
public class TemplateVO extends BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 模板组ID
     */
    @Schema(description = "模板组ID")
    private Long templateGroupId;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 生成代码的路径
     */
    @Schema(description = "生成代码的路径")
    private String generatorPath;

    /**
     * 模板描述
     */
    @Schema(description = "模板描述")
    private String templateDesc;

    /**
     * 模板内容
     */
    @Schema(description = "模板内容")
    private String templateContent;

    /**
     * 模板类型（0-文件，1-目录）
     */
    @Schema(description = "模板类型（0-文件，1-目录）")
    private Integer templateType;

    /**
     * 模板组名称
     */
    @Schema(description = "模板组名称")
    private String templateGroupName;

    /**
     * 模板组类型（0-项目，1-表，2-枚举）
     */
    @Schema(description = "模板组类型（0-项目，1-表，2-枚举）")
    private Integer templateGroupType;

}