package com.yanggu.code.generator.util;

import cn.hutool.v7.core.reflect.ClassUtil;
import cn.hutool.v7.json.JSONArray;
import cn.hutool.v7.json.JSONObject;
import cn.hutool.v7.json.JSONPrimitive;
import cn.hutool.v7.json.JSONUtil;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class GenUtil {

    public static Object handleData(String value) {
        // 先尝试作为 JSON 解析
        Object json = tryParseJson(value);
        if (json != null) {
            return deepConvert(json);
        }

        // 否则转成 number/boolean/null/原字符串
        return convertStringToAppropriateType(value);
    }

    /**
     * 尝试将字符串解析为 JSONObject 或 JSONArray
     */
    private static Object tryParseJson(String value) {
        try {
            if (JSONUtil.isTypeJSONArray(value)) {
                return JSONUtil.parseArray(value);
            }
            if (JSONUtil.isTypeJSONObject(value)) {
                return JSONUtil.parseObj(value);
            }
        } catch (Exception ignored) {
            // 若解析失败则返回 null
        }
        return null;
    }

    /**
     * 递归转换 JSONObject / JSONArray
     */
    private static Object deepConvert(Object obj) {
        if (obj instanceof JSONArray arr) {
            List<Object> result = new ArrayList<>();
            for (Object item : arr) {
                result.add(deepConvert(item));
            }
            return result;
        }

        if (obj instanceof JSONObject jsonObj) {
            Map<String, Object> newObj = new HashMap<>();
            jsonObj.forEach((k, v) -> newObj.put(k, deepConvert(v)));
            return newObj;
        }

        // 叶子节点：做字符串 → number/boolean/null 处理
        if (obj instanceof JSONPrimitive jsonPrimitive) {
            return convertStringToAppropriateType(jsonPrimitive.getValue().toString());
        }

        return obj;
    }

    public static Object convertStringToAppropriateType(String value) {
        if (value == null) {
            return null;
        }

        // 尝试转换为布尔值
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }

        // 尝试转换为整数
        try {
            if (value.matches("-?\\d+")) {
                long longValue = Long.parseLong(value);
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return (int) longValue;
                } else {
                    return longValue;
                }
            }
        } catch (NumberFormatException e) {
            // 不是有效的整数格式，继续尝试其他类型
        }

        // 尝试转换为浮点数
        try {
            if (value.matches("-?\\d+\\.\\d+")) {
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            // 不是有效的浮点数格式
        }

        // 默认返回原始字符串
        return value;
    }

    public String getStringValue(Object value) {
        String data;
        if (ClassUtil.isSimpleValueType(value.getClass())) {
            data = value.toString();
        } else {
            data = JSONUtil.toJsonStr(value);
        }
        return data;
    }

}
