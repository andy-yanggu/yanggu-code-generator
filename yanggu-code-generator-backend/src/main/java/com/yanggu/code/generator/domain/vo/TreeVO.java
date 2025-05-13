package com.yanggu.code.generator.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class TreeVO {

    private String label;

    private Long templateId;

    private String filePath;

    private Integer level;

    private Boolean isFile;

    private List<TreeVO> children;

}
