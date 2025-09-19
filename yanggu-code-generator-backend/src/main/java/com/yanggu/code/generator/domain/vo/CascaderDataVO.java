package com.yanggu.code.generator.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class CascaderDataVO {

    private Long id;

    private String label;

    private String value;

    private String type;

    private Integer generatorType;

    private List<CascaderDataVO> children;

}
