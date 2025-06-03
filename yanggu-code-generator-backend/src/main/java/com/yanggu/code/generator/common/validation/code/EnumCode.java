package com.yanggu.code.generator.common.validation.code;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * 枚举校验注解。字段的值需要在枚举的指定字段的指定值范围内
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Constraint(validatedBy = {EnumCodeValidator.class, EnumCodeCollectionValidator.class})
public @interface EnumCode {

    /**
     * 需要校验的枚举类型
     */
    Class<? extends Enum<?>> value();

    /**
     * 枚举类型校验值字段名称
     */
    String fieldName() default "code";

    /**
     * 枚举类型校验值列表。为空代表全部
     */
    String[] valueList() default {};

    /**
     * 异常信息，支持{enumName}、{enumFieldName}、{enumValue}和{valueList}占位替换
     */
    String message() default "枚举属性名称: {enumName}.{enumFieldName}, 传入的枚举属性枚举值: {enumValue}, 必须在指定范围: {valueList}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
