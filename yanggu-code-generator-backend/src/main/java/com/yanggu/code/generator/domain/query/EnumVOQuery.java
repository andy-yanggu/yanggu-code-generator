package com.yanggu.code.generator.domain.query;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.domain.vo.EnumVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 枚举VO查询实体类
 */
@Data
@Schema(description = "枚举VO查询实体类")
@EqualsAndHashCode(callSuper = true)
public class EnumVOQuery extends PageQuery<EnumVO> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 枚举名称
     */
    @Schema(description = "枚举名称")
    private String enumName;

}