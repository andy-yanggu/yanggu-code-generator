package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.common.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

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
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 父级ID
     */
    @Schema(description = "父级ID")
    private Long parentId;

    /**
     * 模板组ID
     */
    @Schema(description = "模板组ID")
    private Long templateGroupId;

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

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 文件或者目录名称
     */
    @Schema(description = "文件或者目录名称")
    private String fileName;

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
     * 模板类型（0-目录，1-模板文件，2-二进制文件）
     */
    @Schema(description = "模板类型（0-目录，1-模板文件，2-二进制文件）")
    private Integer templateType;

    /**
     * 模板内容
     */
    @Schema(description = "模板内容")
    private String templateContent;

    /**
     * 二进制原始文件名
     */
    @Schema(description = "二进制原始文件名")
    private String binaryOriginalFileName;

    /**
     * 模板排序
     */
    @Schema(description = "模板排序")
    private Integer templateOrder;

    /**
     * 子节点列表
     */
    @Schema(description = "子节点列表")
    private List<TemplateVO> children;

}