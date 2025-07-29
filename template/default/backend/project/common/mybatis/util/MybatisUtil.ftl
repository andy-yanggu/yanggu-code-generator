package ${projectPackage}.${projectNameDot}.common.mybatis.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.yanggu.code.generator.common.domain.query.OrderItemQuery;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.bean.BeanUtil;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.func.LambdaFactory;
import org.dromara.hutool.core.reflect.FieldUtil;
import org.dromara.hutool.core.reflect.method.MethodInvoker;
import org.dromara.hutool.core.text.NamingCase;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.util.ObjUtil;

import java.util.List;

/**
 * mybatis工具类
 */
@Slf4j
@UtilityClass
public class MybatisUtil {

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
            throw new IllegalArgumentException("column is blank");
        }
        //获取字段对应的getter方法
        MethodInvoker getter = (MethodInvoker) BeanUtil.getBeanDesc(clazz).getGetter(column);
        if (getter == null) {
            throw new IllegalArgumentException(StrUtil.format("column: {} is not exist", column));
        }
        return LambdaFactory.build(SFunction.class, getter.getMethod());
    }

    /**
     * 判断对象不为空
     */
    public static boolean isNotEmpty(Object object) {
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
