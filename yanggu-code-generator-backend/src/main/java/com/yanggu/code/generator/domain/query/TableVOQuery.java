package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.vo.TableVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 表VO查询实体类
 */
@Data
@Schema(description = "表VO查询实体类")
@EqualsAndHashCode(callSuper = true)
public class TableVOQuery extends PageQuery<TableVO> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 表名
     */
    @Schema(description = "表名")
    private String tableName;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

}