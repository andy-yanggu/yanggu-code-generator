package ${projectPackage}.${projectNameDot}.common.requestresponselog;


import java.lang.annotation.*;

/**
 * controller日志注解
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestResponseLog {

    /**
     * 是否忽略日志记录
     */
    boolean isIgnoreLog() default false;

    /**
     * 是否日志记录请求的参数
     */
    boolean isLogRequestData() default true;

    /**
     * 是否日志记录响应的参数
     */
    boolean isLogResponseData() default true;

    /**
     * 排除指定的请求参数。支持beanPath的方式，删除属性。例如person.friend[5].name
     */
    String[] excludeRequestParamNames() default {};

    /**
     * 排除指定的响应参数。支持beanPath的方式，删除属性。例如person.friend[5].name
     */
    String[] excludeResponseFieldNames() default {};

}
