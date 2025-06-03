package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 模板组Entity查询实体类
 */
@Data
@Schema(description = "模板组Entity查询实体类")
@EqualsAndHashCode(callSuper = true)
public class TemplateGroupEntityQuery extends PageQuery<TemplateGroupEntity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板组名称
     */
    @Schema(description = "模板组名称")
    private String groupName;

    /**
     * 模板组类型（0-项目，1-表，2-枚举）
     */
    @Schema(description = "模板组类型（0-项目，1-表，2-枚举）")
    private Integer type;

}