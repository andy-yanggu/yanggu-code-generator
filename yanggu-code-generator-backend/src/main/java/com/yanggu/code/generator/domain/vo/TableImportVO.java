package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 表导入VO实体类
 */
@Data
@Schema(description = "表导入VO实体类")
public class TableImportVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 表名
     */
    @Schema(description = "表名")
    private String tableName;

    /**
     * 说明
     */
    @Schema(description = "说明")
    private String tableComment;

    /**
     * 是否存在
     */
    @Schema(description = "是否存在")
    private Boolean exist;

}
