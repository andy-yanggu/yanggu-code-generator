package com.yanggu.code.generator.common.mybatis.util;

import cn.hutool.v7.core.bean.BeanUtil;
import cn.hutool.v7.core.collection.CollUtil;
import cn.hutool.v7.core.func.LambdaFactory;
import cn.hutool.v7.core.reflect.FieldUtil;
import cn.hutool.v7.core.reflect.method.MethodInvoker;
import cn.hutool.v7.core.text.NamingCase;
import cn.hutool.v7.core.text.StrUtil;
import cn.hutool.v7.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.yanggu.code.generator.common.domain.query.OrderItemQuery;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.response.ResultEnum;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.regex.Pattern;

/**
 * mybatis工具类
 */
@Slf4j
@UtilityClass
public class MybatisUtil {

    /**
     * 排序字段合法格式：仅允许字母、数字、下划线
     */
    private static final Pattern COLUMN_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");

    /**
     * 设置排序字段
     */
    public static <T> void orderBy(LambdaQueryWrapper<T> wrapper, List<OrderItemQuery> orderItemList) {
        if (CollUtil.isEmpty(orderItemList)) {
            return;
        }
        if (wrapper == null) {
            throw new IllegalArgumentException("wrapper is null");
        }
        Class<T> entityClass = wrapper.getEntityClass();
        if (entityClass == null) {
            throw new IllegalArgumentException("entityClass is null");
        }
        orderItemList.forEach(orderItem -> {
            SFunction<T, ?> column = buildGetter(entityClass, orderItem.getColumn());
            if (orderItem.getAsc()) {
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
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }
        if (StrUtil.isBlank(column)) {
            //空字段名同样由调用方传入，不是框架内部误用
            throw new BusinessException(ResultEnum.PARAM_ERR.getCode(), "排序字段不能为空");
        }
        //获取字段对应的getter方法
        MethodInvoker getter = (MethodInvoker) BeanUtil.getBeanDesc(clazz).getGetter(column);
        if (getter == null) {
            //排序字段由调用方 JSON 传入，属客户端可触发错误，带出具体字段名便于定位
            throw new BusinessException(ResultEnum.PARAM_ERR.getCode(), "排序字段: {} 不存在", column);
        }
        return LambdaFactory.build(SFunction.class, getter.getMethod());
    }

    /**
     * 校验排序字段合法性（防SQL注入）并转为下划线列名
     * <p>仅允许字母、数字、下划线，拒绝空格、括号、逗号、引号等SQL语法字符，非法时快速失败</p>
     */
    public static String toSafeUnderlineCase(String column) {
        if (StrUtil.isBlank(column)) {
            throw new BusinessException(ResultEnum.PARAM_ERR.getCode(), "排序字段不能为空");
        }
        if (!COLUMN_PATTERN.matcher(column).matches()) {
            throw new BusinessException(ResultEnum.PARAM_ERR.getCode(), "排序字段不合法: {}", column);
        }
        return NamingCase.toUnderlineCase(column);
    }

    /**
     * 判断对象不为空
     */
    public static boolean isNotEmpty(Object object) {
        // 如果是字符串，则去除首尾空格
        if (object instanceof CharSequence) {
            object = StrUtil.trim((CharSequence) object);
        }

        return ObjUtil.isNotEmpty(object);
    }

    /**
     * 判断对象中某个字段不为空
     */
    public static boolean isNotEmpty(Object object, String propertyName) {
        if (object == null) {
            log.warn("object is null");
            return false;
        }
        if (StrUtil.isBlank(propertyName)) {
            log.warn("propertyName is null or blank");
            return false;
        }
        boolean hasField = FieldUtil.hasField(object.getClass(), propertyName);
        if (!hasField) {
            return false;
        }
        Object fieldValue = FieldUtil.getFieldValue(object, propertyName);

        return isNotEmpty(fieldValue);
    }

    /**
     * 小驼峰转下划线
     */
    public static String toUnderlineCase(String str) {
        return NamingCase.toUnderlineCase(str);
    }

}
