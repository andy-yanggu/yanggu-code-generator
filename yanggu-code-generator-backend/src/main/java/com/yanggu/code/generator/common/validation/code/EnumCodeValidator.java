package com.yanggu.code.generator.common.validation.code;

public class EnumCodeValidator extends AbstractEnumCodeValidator<Object> {

    @Override
    public boolean isValid(Object value) {
        //为空时，默认不校验，即认为通过
        if (value == null) {
            return true;
        }
        for (Object item : values) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }

}

