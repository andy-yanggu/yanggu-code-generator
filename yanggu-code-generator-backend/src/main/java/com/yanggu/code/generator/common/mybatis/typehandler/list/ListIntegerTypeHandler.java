package com.yanggu.code.generator.common.mybatis.typehandler.list;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Integer类型List类型转换器
 */
@NoArgsConstructor
public class ListIntegerTypeHandler extends AbstractListTypeHandler<Integer> {

    @Override
    protected TypeReference<List<Integer>> getTypeReference() {
        return new TypeReference<>() {
        };
    }

}
