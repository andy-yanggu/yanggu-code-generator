package com.yanggu.code.generator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 表批量生成代码检测结果对象
 */
@Data
@Schema(description = "表批量生成代码检测结果对象")
public class TableGenerateCheckVO {

    /**
     * 检查结果，true-通过，false-未通过
     */
    @Schema(description = "检查结果，true-通过，false-未通过")
    private Boolean checkResult;

    /**
     * 生成方式（0-zip压缩包，1-服务器本地）
     */
    @Schema(description = "生成方式（0-zip压缩包，1-服务器本地）")
    private Integer generatorType;

}
