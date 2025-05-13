package com.yanggu.code.generator.domain.query;

import lombok.Data;

import java.util.List;

@Data
public class GeneratorTableQuery {

    private Long tableId;

    private List<Long> templateIdList;

}
