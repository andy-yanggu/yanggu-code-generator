package com.yanggu.code.generator.common.mybatis.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.dromara.hutool.core.bean.BeanUtil;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.func.LambdaFactory;
import org.dromara.hutool.core.reflect.method.MethodInvoker;
import org.dromara.hutool.core.text.StrUtil;

import java.util.List;

/**
 * mybatis工具类
 */
public class MybatisUtil {

    /**
     * 设置排序字段
     */
    public static <T> void orderBy(LambdaQueryWrapper<T> wrapper, List<OrderItem> orders) {
        if (CollUtil.isEmpty(orders)) {
            return;
        }
        Class<T> entityClass = wrapper.getEntityClass();
        if (entityClass == null) {
            throw new IllegalArgumentException("entityClass is null");
        }
        orders.forEach(orderItem -> {
            SFunction<T, ?> column = buildGetter(entityClass, orderItem.getColumn());
            if (orderItem.isAsc()) {
                wrapper.orderByAsc(column);
            } else {
                wrapper.orderByDesc(column);
            }
        });
    }

    /**
     * 获取getter方法的lambda表达式
     */
    public static <T> SFunction<T, ?> buildGetter(Class<T> clazz, String column) {
        if (StrUtil.isBlank(column)) {
            throw new IllegalArgumentException("column is blank");
        }
        //user_name转换成userName
        String camelCase = StrUtil.toCamelCase(column);
        //获取字段对应的getter方法
        MethodInvoker getter = (MethodInvoker) BeanUtil.getBeanDesc(clazz).getGetter(camelCase);
        if (getter == null) {
            throw new IllegalArgumentException("column is not exist");
        }
        return LambdaFactory.build(SFunction.class, getter.getMethod());
    }

}
