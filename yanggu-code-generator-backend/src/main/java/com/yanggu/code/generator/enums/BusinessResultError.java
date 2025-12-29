package com.yanggu.code.generator.enums;

import com.yanggu.code.generator.common.response.IResultError;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessResultError implements IResultError {

    EXECUTION_EXPRESS_ERROR(700, "表达式执行为false，模板不渲染"),
    ;

    /**
     * 请求结果状态码
     */
    private final Integer code;

    /**
     * 请求结果描述信息
     */
    private final String message;


}
