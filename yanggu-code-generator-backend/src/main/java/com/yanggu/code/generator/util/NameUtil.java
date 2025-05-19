package com.yanggu.code.generator.util;


import org.dromara.hutool.core.text.NamingCase;
import org.dromara.hutool.core.text.StrUtil;

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

}
