package com.yanggu.code.generator.common.mybatis.typehandler.list;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Long类型List类型转换器
 */
@NoArgsConstructor
public class ListLongTypeHandler extends AbstractListTypeHandler<Long> {

    @Override
    protected TypeReference<List<Long>> getTypeReference() {
        return new TypeReference<>() {
        };
    }

}
