package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.dromara.hutool.core.comparator.ComparatorChain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * 树形数据VO实体类
 */
@Data
@Schema(description = "树形数据VO实体类")
public class TreeVO implements Serializable {

    /**
     * 树形数据排序。类型，标签
     */
    public static final Comparator<TreeVO> TREE_COMPARATOR = ComparatorChain.of(
            Comparator.comparing(TreeVO::getType).reversed(), Comparator.comparing(TreeVO::getLabel)
    );

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String filePath;

    /**
     * 树节点标签
     */
    @Schema(description = "树节点标签")
    private String label;

    /**
     * 树节点层级
     */
    @Schema(description = "树节点层级")
    private Integer level;

    /**
     * 树类型（0-文件，1-文件夹）
     */
    @Schema(description = "树类型（0-文件，1-文件夹）")
    private Integer type;

    /**
     * 子节点列表
     */
    @Schema(description = "子节点列表")
    private List<TreeVO> children;

    /**
     * 是否是模板
     */
    @Schema(description = "是否是模板")
    private Boolean isTemplate;

    /**
     * 模板ID
     */
    @Schema(description = "模板ID")
    private Long templateId;

    /**
     * 模板类型（0-文件，1-目录）
     */
    @Schema(description = "模板类型（0-文件，1-目录）")
    private Integer templateType;

}
