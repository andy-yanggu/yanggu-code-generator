package com.yanggu.code.generator.domain.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 表导入DTO
 */
@Data
@Schema(description = "表导入DTO")
public class TableImportDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 表列表
     */
    @Schema(description = "表列表")
    private List<String> tableNameList;

}
