package com.yanggu.code.generator.util;


import lombok.experimental.UtilityClass;
import org.dromara.hutool.core.text.NamingCase;
import org.dromara.hutool.core.text.StrUtil;

import static org.dromara.hutool.core.text.CharPool.DASHED;

@UtilityClass
public class NameUtil {

    /**
     * 中横线转下划线。demo-test => demo_test
     */
    public static String toUnderLine(String name) {
        return StrUtil.replace(name, "-", "_");
    }

    /**
     * 中横线转大驼峰。demo-test => DemoTest
     */
    public static String toPascal(String name) {
        return NamingCase.toPascalCase(toUnderLine(name));
    }

    /**
     * 中横线转大写和空格。demo-test => Demo Test
     */
    public static String toSpaceUpperCase(String name) {
        return StrUtil.upperFirst(NamingCase.toSymbolCase(toPascal(name), ' '));
    }

    /**
     * 中横线转点。demo-test => demo.test
     */
    public static String toDot(String name) {
        return StrUtil.replace(name, "-", ".");
    }

    /**
     * 中横线转斜线。demo-test => demo/test
     */
    public static String toSlash(String name) {
        return StrUtil.replace(name, "-", "/");
    }

    /**
     * 中横线转全部大写，下划线分割。demo-test => DEMO_TEST
     */
    public static String toAllUpperCase(String name) {
        return StrUtil.toUnderlineCase(toPascal(name)).toUpperCase();
    }

}
