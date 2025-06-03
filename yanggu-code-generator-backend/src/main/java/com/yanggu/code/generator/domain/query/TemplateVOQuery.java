package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 模板VO查询实体类
 */
@Data
@Schema(description = "模板VO查询实体类")
@EqualsAndHashCode(callSuper = true)
public class TemplateVOQuery extends PageQuery<TemplateVO> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板组ID
     */
    @Schema(description = "模板组ID")
    private Long templateGroupId;

    /**
     * 模板组ID列表
     */
    @Schema(description = "模板组ID列表")
    private List<Long> templateGroupIdList;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 模板类型（0-文件，1-目录）
     */
    @Schema(description = "模板类型（0-文件，1-目录）")
    private Integer templateType;

    /**
     * 模板组类型（0-项目，1-表，2-枚举）
     */
    @Schema(description = "模板组类型（0-项目，1-表，2-枚举）")
    private Integer templateGroupType;

}