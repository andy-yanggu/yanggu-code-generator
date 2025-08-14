package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.common.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.hutool.core.comparator.ComparatorChain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * 预览模板VO实体类
 */
@Data
@Schema(description = "预览模板VO实体类")
@EqualsAndHashCode(callSuper = true)
public class PreviewTemplateVO extends BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Comparator<PreviewTemplateVO> TREE_COMPARATOR = ComparatorChain.of(
            Comparator.comparing(PreviewTemplateVO::getTemplateType), Comparator.comparing(PreviewTemplateVO::getFileName)
    );

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 表ID
     */
    @Schema(description = "表ID")
    private Long tableId;

    /**
     * 枚举ID
     */
    @Schema(description = "枚举ID")
    private Long enumId;

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
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String filePath;

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
     * 树节点层级
     */
    @Schema(description = "树节点层级")
    private Integer level;

    /**
     * 子节点列表
     */
    @Schema(description = "子节点列表")
    private List<PreviewTemplateVO> children;

}