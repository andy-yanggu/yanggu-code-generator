package com.yanggu.code.generator.common.validation.path;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.dromara.hutool.core.text.StrUtil;

public class UnixPathValidator implements ConstraintValidator<UnixPath, String> {

    private static final String UNIX_PATH_REGEX = "^[^\\\\]*$";

    @Override
    public void initialize(UnixPath constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StrUtil.isBlank(value)) {
            return true;
        }
        return value.matches(UNIX_PATH_REGEX);
    }

}
