package com.yanggu.code.generator.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GenUtil {

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

}
