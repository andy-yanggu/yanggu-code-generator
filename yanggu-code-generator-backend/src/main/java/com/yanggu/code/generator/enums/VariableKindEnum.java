package com.yanggu.code.generator.enums;

/**
 * 模板变量分类，前端据此决定标签文案与点击插入的文本形态
 * 注意：不加 @JsonValue，Jackson 按枚举名序列化，前端契约是字符串
 */
public enum VariableKindEnum {

    /**
     * 标量：String / 数值 / Boolean / Object，插入 ${path}
     */
    SCALAR,

    /**
     * 标量集合：List&lt;String&gt;、Set&lt;String&gt;、List&lt;Integer&gt;，插入循环骨架
     */
    SCALAR_LIST,

    /**
     * 模型集合：List&lt;某 Model 类&gt;，插入循环骨架，子变量带循环变量名前缀
     */
    MODEL_LIST,

    /**
     * 嵌套模型对象，子变量直接 ${obj.field} 取，无循环
     */
    OBJECT,

    /**
     * Map&lt;String, Object&gt;，key 由模板组属性配置决定
     */
    MAP,
    ;

}
