package com.yanggu.code.generator.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "标签值数据DTO")
public class LabelValueDataDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7623840568314989902L;

    @Schema(description = "标签")
    private String label;

    @Schema(description = "值")
    private Object value;

}
