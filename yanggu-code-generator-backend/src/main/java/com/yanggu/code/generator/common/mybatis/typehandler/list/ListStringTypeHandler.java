package com.yanggu.code.generator.common.mybatis.typehandler.list;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * String类型List类型转换器
 */
@NoArgsConstructor
public class ListStringTypeHandler extends AbstractListTypeHandler<String> {

    @Override
    protected TypeReference<List<String>> getTypeReference() {
        return new TypeReference<>() {
        };
    }

}
