package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.common.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 基类VO实体类
 */
@Data
@Schema(description = "基类VO实体类")
@EqualsAndHashCode(callSuper = true)
public class BaseClassVO extends BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 基类名称
     */
    @Schema(description = "基类名称")
    private String baseClassName;

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

    /**
     * 基类字段
     */
    @Schema(description = "基类字段")
    @NotEmpty(message = "基类字段不能为空")
    private List<String> fieldList;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}