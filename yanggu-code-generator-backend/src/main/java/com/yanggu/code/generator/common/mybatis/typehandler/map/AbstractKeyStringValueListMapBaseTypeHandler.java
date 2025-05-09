package com.yanggu.code.generator.common.mybatis.typehandler.map;


import java.util.List;

/**
 * Map的key是String类型，value是List<V>类型
 *
 * @param <V> List的泛型
 */
public abstract class AbstractKeyStringValueListMapBaseTypeHandler<V> extends AbstractKeyStringMapBaseTypeHandler<List<V>> {
}
