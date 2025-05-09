package com.yanggu.code.generator.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应结果枚举
 */
@Getter
@AllArgsConstructor
public enum ResultEnum implements IResultError {

    /**
     * 请求成功
     */
    SUCCESS(200, "success"),

    /**
     * 请求参数错误
     */
    PARAM_ERR(300, "request param error"),

    /**
     * 请求参数丢失
     */
    PARAM_LOST(301, "request param lost"),

    /**
     * 请求头参数丢失
     */
    HEADER_LOST(302, "request header lost"),

    /**
     * 路径参数丢失
     */
    PATH_LOST(303, "request path lost"),

    /**
     * cookie值丢失
     */
    COOKIE_LOST(304, "request cookie lost"),

    /**
     * 客户端错误
     */
    REQUEST_ERR(400, "request error"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "not found"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "this method is not supported"),

    /**
     * 服务器错误
     */
    INTERNAL_SERVER_ERROR(500, "internal server error"),

    /**
     * 文件太大
     */
    FILE_TOO_LARGE(501, "file too large"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXIST(1000, "{}数据ID: {}不存在"),

    /**
     * 数据已经存在
     */
    DATA_ALREADY_EXIST(1001, "{}数据ID: {}已存在"),

    /**
     * 数据不能为空
     */
    DATA_NOT_NULL(1002, "数据不能为空"),
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
