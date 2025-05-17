package ${projectPackage}.${projectNameDot}.common.exception;

import ${projectPackage}.${projectNameDot}.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Set;
import java.util.StringJoiner;

import static ${projectPackage}.${projectNameDot}.common.response.Result.fail;
import static ${projectPackage}.${projectNameDot}.common.response.ResultEnum.*;

/**
 * 通用异常处理
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnProperty(name = "web.exception.enable", havingValue = "true", matchIfMissing = true)
public class GlobalExceptionHandler {

    @Autowired
    private HttpServletRequest request;

    /**
     * 获取业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> businessException(BusinessException exception,
                                          HandlerMethod handlerMethod) {
        log.error("业务处理不成功, 请求方式: {}, 请求uri: {}, 请求方法: {}, 异常code: {}, 异常message: {}",
                request.getMethod(), request.getRequestURI(), handlerMethod.getMethod().getName(), exception.getCode(), exception.getMessage(), exception);
        return fail(exception);
    }

    /**
     * 捕获异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> serverException(Exception exception) {
        log.error("服务处理不成功, 请求方式: {}, 请求uri: {}, 异常message: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return fail(INTERNAL_SERVER_ERROR);
    }

    /**
     * 捕获运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> runtimeException(RuntimeException exception) {
        log.error("服务处理不成功, 请求方式: {}, 请求uri: {}, 发生运行时异常, 异常message: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return fail(INTERNAL_SERVER_ERROR);
    }

    /**
     * 捕获字段校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> methodArgumentNotValidException(MethodArgumentNotValidException exception) {
        StringJoiner stringJoiner = new StringJoiner(";");
        exception.getBindingResult().getFieldErrors().forEach(fieldError -> stringJoiner.add(fieldError.getDefaultMessage()));
        log.error("字段校验不通过, 请求方式: {}, 请求uri: {}, 字段校验异常信息: {}",
                request.getMethod(), request.getRequestURI(), stringJoiner, exception);
        return fail(PARAM_ERR);
    }

    /**
     * 捕获参数校验异常
     */
    @ExceptionHandler(ValidationException.class)
    public Result<Void> validationException(ValidationException exception) {
        log.error("参数校验不通过, 请求方式: {}, 请求uri: {}, 异常信息: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return fail(PARAM_ERR);
    }

    /**
     * 捕获字段校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> constraintViolationException(ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
        StringJoiner stringJoiner = new StringJoiner(";");
        constraintViolations.forEach(temp -> stringJoiner.add(temp.getMessage()));
        log.error("参数校验不通过, 请求方式: {}, 请求uri: {}, 异常信息: {}",
                request.getMethod(), request.getRequestURI(), stringJoiner, exception);
        return fail(PARAM_ERR);
    }

    /**
     * 捕获参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> bindException(BindException exception) {
        StringJoiner stringJoiner = new StringJoiner(";");
        exception.getBindingResult().getFieldErrors().forEach(fieldError -> stringJoiner.add(fieldError.getDefaultMessage()));
        log.error("参数绑定不通过, 请求方式: {}, 请求uri: {}, 异常信息: {}",
                request.getMethod(), request.getRequestURI(), stringJoiner, exception);
        return fail(PARAM_ERR);
    }

    /**
     * 捕获请求方式不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.error("请求方法不支持, 请求方式: {}, 请求uri: {}, 异常信息: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return fail(METHOD_NOT_ALLOWED);
    }

    /**
     * 捕获cookie参数丢失
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    public Result<Void> handleMissingRequestCookieException(MissingRequestCookieException exception) {
        log.error("请求cookie缺失, 请求方式: {}, 请求uri: {}, 异常信息: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return fail(COOKIE_LOST);
    }

    /**
     * 捕获缺失请求头异常
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public Result<Void> handleMissingRequestHeaderException(MissingRequestHeaderException exception) {
        log.error("请求头缺失, 请求方式: {}, 请求uri: {}, 异常信息: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return fail(HEADER_LOST);
    }

    /**
     * 捕获路径参数丢失异常
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public Result<Void> handleMissingPathVariableException(MissingPathVariableException exception) {
        log.error("路径参数缺失, 请求方式: {}, 请求uri: {}, 异常信息: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return fail(PATH_LOST);
    }

    /**
     * 捕获参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        log.error("请求参数缺失, 请求方式: {}, 请求uri: {}, 异常信息: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return fail(PARAM_LOST);
    }

    /**
     * 捕获文件太大异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exception) {
        log.error("上传文件大小超出限制, 请求方式: {}, 请求uri: {}, 异常信息: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return fail(FILE_TOO_LARGE);
    }

    /**
     * 捕获资源不存在异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> noResourceFoundException(NoResourceFoundException exception) {
        log.error("请求资源不存在, 请求方式: {}, 请求uri: {}, 异常信息: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return fail(NOT_FOUND);
    }

    /**
     * 捕获请求参数解析异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.error("请求参数解析失败, 请求方式: {}, 请求uri: {}, 异常信息: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return fail(REQUEST_ERR.getCode(), exception.getMessage());
    }

}
