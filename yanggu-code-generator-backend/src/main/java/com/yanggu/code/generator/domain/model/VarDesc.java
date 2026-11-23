package com.yanggu.code.generator.domain.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模板变量说明，标注在 Model 字段上，供前端模板编辑页的变量目录读取
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VarDesc {

    /**
     * 面向模板作者的中文说明，取自该字段原有的 javadoc 文本
     */
    String value();

    /**
     * 默认循环变量名，只对 List / Set 字段有效，取自既有 .ftl 的 &lt;#list&gt; 约定
     */
    String loopVar() default "";

    /**
     * 渲染时可能为 null，模板需 &lt;#if xxx??&gt; 保护
     */
    boolean nullable() default false;

    /**
     * 敏感字段（数据源口令等），带值目录展示时一律掩码，不靠字段名白名单防止重命名漏脱敏
     */
    boolean sensitive() default false;
}
