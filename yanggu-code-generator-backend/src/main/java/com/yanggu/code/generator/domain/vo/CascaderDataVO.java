package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "级联选择器数据VO实体类")
public class CascaderDataVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "标签")
    private String label;

    @Schema(description = "值")
    private String value;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "生成器类型")
    private Integer generatorType;

    @Schema(description = "子级数据")
    private List<CascaderDataVO> children;

}
