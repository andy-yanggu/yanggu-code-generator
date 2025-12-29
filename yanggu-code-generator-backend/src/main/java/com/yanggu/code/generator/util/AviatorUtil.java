package com.yanggu.code.generator.util;

import cn.hutool.v7.core.bean.BeanUtil;
import cn.hutool.v7.core.text.StrUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.JavaMethodReflectionFunctionMissing;
import lombok.extern.slf4j.Slf4j;

import static com.googlecode.aviator.Options.USE_USER_ENV_AS_TOP_ENV_DIRECTLY;

/**
 * Aviator表达式工具类
 */
@Slf4j
public class AviatorUtil {

    /**
     * 编译表达式
     */
    public static Expression compile(String expression) {
        if (StrUtil.isBlank(expression)) {
            throw new RuntimeException("Aviator表达式不能为空");
        }
        AviatorEvaluatorInstance aviatorEvaluatorInstance = AviatorEvaluator.getInstance();
        //设置Java反射调用
        aviatorEvaluatorInstance.setFunctionMissing(JavaMethodReflectionFunctionMissing.getInstance());
        //设置不使用客户传入的env
        aviatorEvaluatorInstance.setOption(USE_USER_ENV_AS_TOP_ENV_DIRECTLY, false);
        try {
            //编译表达式
            return aviatorEvaluatorInstance.compile(expression, true);
        } catch (Exception e) {
            log.error("Aviator表达式编译失败, 请检查表达式编写是否正确或者传参是否正确", e);
            throw new RuntimeException("Aviator表达式编译失败, 请检查表达式编写是否正确或者传参是否正确");
        }
    }

    /**
     * 执行表达式
     */
    public static Object execute(Expression expression, Object data) {
        try {
            return expression.execute(BeanUtil.beanToMap(data));
        } catch (Exception e) {
            log.error("Aviator表达式执行失败, 请检查表达式编写是否正确或者传参是否正确", e);
            throw new RuntimeException("Aviator表达式执行失败, 请检查表达式编写是否正确或者传参是否正确");
        }
    }

}
