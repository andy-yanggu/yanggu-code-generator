package com.yanggu.code.generator.domain.vo;

import lombok.Data;

import java.util.Comparator;
import java.util.List;

@Data
public class TreeVO {

    public static final Comparator<TreeVO> TREE_COMPARATOR = (tree1, tree2) -> {
        if (tree1.getIsFile() && !tree2.getIsFile()) {
            return 1;
        } else if (!tree1.getIsFile() && tree2.getIsFile()) {
            return -1;
        } else {
            return tree1.getLabel().compareTo(tree2.getLabel());
        }
    };

    private String label;

    private Long templateId;

    private String filePath;

    private Integer level;

    private Boolean isFile;

    private List<TreeVO> children;

}
