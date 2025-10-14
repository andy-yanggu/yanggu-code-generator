package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 模板组属性Entity查询实体类
 */
@Data
@Schema(description = "模板组属性Entity查询实体类")
@EqualsAndHashCode(callSuper = true)
public class TemplateGroupPropertyEntityQuery extends PageQuery<TemplateGroupPropertyEntity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板组ID
     */
    @Schema(description = "模板组ID")
    private Long templateGroupId;

    /**
     * 属性标题
     */
    @Schema(description = "属性标题")
    private String propTitle;

    /**
     * 属性键
     */
    @Schema(description = "属性键")
    private String propKey;

}
