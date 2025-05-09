package com.yanggu.code.generator.common.response;

/**
 * 自定义枚举继承该接口
 */
public interface IResultError {

    /**
     * 错误码
     */
    Integer getCode();

    /**
     * 错误信息
     */
    String getMessage();

}
