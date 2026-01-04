package com.yanggu.code.generator.common.mybatis.typehandler.map;

import cn.hutool.v7.core.map.MapUtil;
import com.yanggu.code.generator.util.GenUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Map的value进行了特殊处理
 */
public class MapDataTypeHandler extends MapStringTypeHandler {

    @Override
    protected Map<String, Object> toObject(Object o) {
        Map<String, Object> map = super.toObject(o);
        if (MapUtil.isEmpty(map)) {
            return map;
        }
        Map<String, Object> result = new HashMap<>();
        map.forEach((k, v) -> {
            if (v == null) {
                result.put(k, null);
            } else {
                Object object = GenUtil.handleData(v.toString());
                result.put(k, object);
            }
        });
        return result;
    }
}
