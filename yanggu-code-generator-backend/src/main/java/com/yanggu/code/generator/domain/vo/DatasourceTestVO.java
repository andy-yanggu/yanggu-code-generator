package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据源检测VO实体类
 */
@Data
@Schema(description = "数据源检测VO实体类")
public class DatasourceTestVO {

    /**
     * 检测结果
     */
    @Schema(description = "检测结果")
    private Boolean result;

    /**
     * 数据库名称
     */
    @Schema(description = "数据库名称")
    private String databaseName;

    /**
     * 错误信息
     */
    private String errorMessage;

}
