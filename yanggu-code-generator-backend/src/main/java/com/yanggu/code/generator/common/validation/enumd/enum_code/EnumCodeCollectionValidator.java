package com.yanggu.code.generator.common.validation.enumd.enum_code;


import org.dromara.hutool.core.collection.CollUtil;

import java.util.Collection;

public class EnumCodeCollectionValidator extends AbstractEnumCodeValidator<Collection<?>> {

    @Override
    public boolean isValid(Collection<?> value) {
        return CollUtil.containsAll(values, value);
    }

}

