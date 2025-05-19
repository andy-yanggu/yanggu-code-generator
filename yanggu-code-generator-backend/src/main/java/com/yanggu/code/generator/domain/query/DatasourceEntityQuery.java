package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 数据源Entity查询实体类
 */
@Data
@Schema(description = "数据源Entity查询实体类")
@EqualsAndHashCode(callSuper = true)
public class DatasourceEntityQuery extends PageQuery<DatasourceEntity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据库类型
     */
    @Schema(description = "数据库类型")
    private String dbType;

    /**
     * 连接名
     */
    @Schema(description = "连接名")
    private String connName;

}