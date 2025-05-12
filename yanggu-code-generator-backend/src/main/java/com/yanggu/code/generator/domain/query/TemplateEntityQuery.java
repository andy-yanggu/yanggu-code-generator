package com.yanggu.code.generator.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.entity.TemplateEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 模板Entity查询实体类
 */
@Data
@Schema(description = "模板Entity查询实体类")
@EqualsAndHashCode(callSuper = true)
public class TemplateEntityQuery extends PageQuery<TemplateEntity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板组ID
     */
    @Schema(description = "模板组ID")
    private Long templateGroupId;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 模板类型
     */
    @Schema(description = "模板类型")
    private Integer templateType;

}