package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.common.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字段类型VO实体类
 */
@Data
@Schema(description = "字段类型VO实体类")
@EqualsAndHashCode(callSuper = true)
public class FieldTypeVO extends BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 字段类型
     */
    @Schema(description = "字段类型")
    private String columnType;

    /**
     * 属性类型
     */
    @Schema(description = "属性类型")
    private String attrType;

    /**
     * 属性包名
     */
    @Schema(description = "属性包名")
    private String packageName;

}