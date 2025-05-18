package com.yanggu.code.generator.common.validation.enumd.path;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 校验路径必须为 Unix 风格（以 '/' 分隔）
 */
@Documented
@Constraint(validatedBy = UnixPathValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface UnixPath {

    String message() default "路径不能出现'\\'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
