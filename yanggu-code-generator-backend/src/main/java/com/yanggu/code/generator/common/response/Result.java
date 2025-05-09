package com.yanggu.code.generator.common.response;

import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.exception.IException;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import static com.yanggu.code.generator.common.response.ResultEnum.INTERNAL_SERVER_ERROR;
import static com.yanggu.code.generator.common.response.ResultEnum.SUCCESS;

/**
 * 统一返回的响应封装结果
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 3782047511903624813L;

    /**
     * 返回状态码
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 返回的数据
     */
    private T data;

    /**
     * 无参
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS.getCode());
        result.setMessage(SUCCESS.getMessage());
        return result;
    }

    /**
     * 一个参数
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.setData(data);
        return result;
    }

    /**
     * 两个参数
     *
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = success(data);
        result.setMessage(message);
        return result;
    }

    /**
     * 服务稳定-两个参数
     *
     * @param code
     * @param message
     * @param <T>
     * @return
     */
    public static <T> Result<T> build(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 三个参数
     *
     * @param code
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> build(Integer code, String message, T data) {
        Result<T> result = build(code, message);
        result.setData(data);
        return result;
    }


    /**
     * 服务异常时的响应 -无参
     */
    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.setCode(INTERNAL_SERVER_ERROR.getCode());
        result.setMessage(INTERNAL_SERVER_ERROR.getMessage());
        return result;
    }

    /**
     * 服务异常时的响应 -一个参数
     */
    public static <T> Result<T> fail(T data) {
        Result<T> result = fail();
        result.setData(data);
        return result;
    }

    /**
     * 服务异常时的响应 -两个参数
     */
    public static <T> Result<T> fail(String message, T data) {
        Result<T> result = fail(data);
        result.setMessage(message);
        return result;
    }

    /**
     * 异常信息处理
     */
    public static <T> Result<T> fail(String message) {
        Result<T> result = fail();
        result.setMessage(message);
        return result;
    }

    /**
     * 服务异常时的响应 -两个参数
     */
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = fail();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 返回异常
     */
    public static <T> Result<T> fail(BusinessException businessException) {
        return fail(businessException.getCode(), businessException.getMessage());
    }

    /**
     * 返回异常
     */
    public static <T> Result<T> fail(IException exception) {
        Result<T> result = new Result<>();
        result.setCode(exception.getCode());
        result.setMessage(exception.getMessage());
        return result;
    }

    /**
     * 返回异常枚举
     */
    public static <T> Result<T> fail(IResultError error) {
        return fail(error.getCode(), error.getMessage());
    }

    /**
     * 服务异常时的响应 -三个参数
     */
    public static <T> Result<T> fail(Integer code, String message, T data) {
        Result<T> result = fail(message, data);
        result.setCode(code);
        return result;
    }

}
