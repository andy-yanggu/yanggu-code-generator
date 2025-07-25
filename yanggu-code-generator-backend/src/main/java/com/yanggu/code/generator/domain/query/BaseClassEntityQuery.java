package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.entity.BaseClassEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 基类Entity查询实体类
 */
@Data
@Schema(description = "基类Entity查询实体类")
@EqualsAndHashCode(callSuper = true)
public class BaseClassEntityQuery extends PageQuery<BaseClassEntity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 基类包名
     */
    @Schema(description = "基类包名")
    private String packageName;

    /**
     * 基类类名
     */
    @Schema(description = "基类类名")
    private String className;

}