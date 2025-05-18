package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.dromara.hutool.core.comparator.ComparatorChain;

import java.util.Comparator;
import java.util.List;

/**
 * 树形数据VO实体类
 */
@Data
@Schema(description = "树形数据VO实体类")
public class TreeVO {

    public static final Comparator<TreeVO> TREE_COMPARATOR = ComparatorChain.of(
            Comparator.comparing(TreeVO::getIsFile), Comparator.comparing(TreeVO::getLabel)
    );

    /**
     * 树节点标签
     */
    @Schema(description = "树节点标签")
    private String label;

    /**
     * 模板ID
     */
    @Schema(description = "模板ID")
    private Long templateId;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String filePath;

    /**
     * 树节点层级
     */
    @Schema(description = "树节点层级")
    private Integer level;

    /**
     * 是否是文件
     */
    @Schema(description = "是否是文件")
    private Boolean isFile;

    /**
     * 子节点列表
     */
    @Schema(description = "子节点列表")
    private List<TreeVO> children;

}
