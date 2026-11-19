package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "标签值数据VO实体类")
public class LabelValueDataVO {

    @Schema(description = "标签")
    private String label;

    @Schema(description = "值")
    private Object value;

}
