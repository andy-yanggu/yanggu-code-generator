package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.vo.DatasourceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 数据源VO查询实体类
 */
@Data
@Schema(description = "数据源VO查询实体类")
@EqualsAndHashCode(callSuper = true)
public class DatasourceVOQuery extends PageQuery<DatasourceVO> implements Serializable {

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