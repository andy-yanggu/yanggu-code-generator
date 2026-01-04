package com.yanggu.code.generator.common.mybatis.typehandler.map;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

/**
 * Map的key是String类型
 */
public class MapStringTypeHandler extends AbstractKeyStringMapBaseTypeHandler<Object>{

    @Override
    protected TypeReference<Map<String, Object>> getTypeReference() {
        return new TypeReference<>() {
        };
    }

}
