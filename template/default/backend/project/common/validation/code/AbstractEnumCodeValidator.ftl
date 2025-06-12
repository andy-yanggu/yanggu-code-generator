package ${projectPackage}.${projectNameDot}.common.validation.code;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;
import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.dromara.hutool.core.reflect.FieldUtil;
import org.dromara.hutool.core.util.EnumUtil;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public abstract class AbstractEnumCodeValidator<T> implements ConstraintValidator<EnumCode, T> {

    protected String enumName;

    protected String enumFieldName;

    protected List<?> values;

    @Override
    @SneakyThrows
    public void initialize(EnumCode annotation) {
        Class<? extends Enum<?>> value = annotation.value();
        this.enumName = value.getName();
        this.enumFieldName = annotation.fieldName();
        if (!FieldUtil.hasField(value, this.enumFieldName)) {
            throw new IllegalArgumentException("枚举属性名称: " + enumName + ", 枚举属性名称: " + enumFieldName + " 不存在");
        }
        List<Object> fieldValues = EnumUtil.getFieldValues(value, this.enumFieldName);
        String[] valueList = annotation.valueList();
        if (ArrayUtil.isNotEmpty(valueList)) {
            Field declaredField = value.getDeclaredField(this.enumFieldName);
            List<?> list = ConvertUtil.toList(declaredField.getType(), valueList);
            //判断是否包含所有值
            if (new HashSet<>(fieldValues).containsAll(list)) {
                this.values = list;
            } else {
                throw new IllegalArgumentException("枚举属性名称: " + enumName + ", 属性名称: " + enumFieldName + ", 值: "
                        + list + " 不在枚举属性值范围内: " + fieldValues);
            }
        } else {
            this.values = fieldValues;
        }
    }

    /**
     * 子类实现校验逻辑
     */
    public abstract boolean isValid(T value);

    @Override
    public boolean isValid(T value, ConstraintValidatorContext context) {
        //进行校验
        boolean valid = isValid(value);
        //如果校验不通过，则设置错误提示语句
        if (!valid) {
            setMessageTemplate(value, context);
        }
        return valid;
    }

    protected void setMessageTemplate(Object value, ConstraintValidatorContext context) {
        //校验不通过，自定义提示语句
        context.disableDefaultConstraintViolation();
        //禁用默认的message 的值
        String messageTemplate = context.getDefaultConstraintMessageTemplate()
                //重新添加错误提示语句
                .replace("{enumName}", enumName)
                .replace("{enumFieldName}", enumFieldName)
                .replace("{enumValue}", Objects.toString(value))
                .replace("{valueList}", values.toString());
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation();
    }

}
