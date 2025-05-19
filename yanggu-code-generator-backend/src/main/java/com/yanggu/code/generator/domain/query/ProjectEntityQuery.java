package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 项目Entity查询实体类
 */
@Data
@Schema(description = "项目Entity查询实体类")
@EqualsAndHashCode(callSuper = true)
public class ProjectEntityQuery extends PageQuery<ProjectEntity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目名
     */
    @Schema(description = "项目名")
    private String projectName;

}