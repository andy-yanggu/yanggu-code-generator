package com.yanggu.code.generator.domain.query;

import lombok.Data;

import java.util.List;

@Data
public class GeneratorProjectQuery {

    private Long projectId;

    private List<Long> tableIdList;

    private List<Long> projectTemplateIdList;

    private List<Long> tableTemplateIdList;

}
