package com.yanggu.code.generator.common.response;

import java.lang.annotation.*;

/**
 * 方法加上这个注解代表无需自动封装 {@link Result} 返回值
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreResult {
}