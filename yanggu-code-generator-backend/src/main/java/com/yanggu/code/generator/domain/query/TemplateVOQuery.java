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
     * 模板组名称
     */
    @Schema(description = "模板组名称")
    private String templateGroupName;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 目录/文件名称
     */
    @Schema(description = "目录/文件名称")
    private String fileName;

    /**
     * 模板类型（0-目录，1-模板文件，2-二进制文件）
     */
    @Schema(description = "模板类型（0-目录，1-模板文件，2-二进制文件）")
    private Integer templateType;

    /**
     * 模板组类型（0-项目，1-表，2-枚举）
     */
    @Schema(description = "模板组类型（0-项目，1-表，2-枚举）")
    private Integer templateGroupType;

}
