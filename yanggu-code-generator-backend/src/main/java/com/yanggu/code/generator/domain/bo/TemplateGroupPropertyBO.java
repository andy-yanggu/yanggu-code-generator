package com.yanggu.code.generator.domain.bo;

import com.yanggu.code.generator.domain.entity.LabelValueDataEntity;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 模板组属性BO实体类
 */
@Data
public class TemplateGroupPropertyBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 属性标题
     */
    private String propTitle;

    /**
     * 属性键
     */
    private String propKey;

    /**
     * 属性默认值
     */
    private String propDefaultValue;

    /**
     * 组件类型，前端显示的组件类型（0-input，1-select、2-radio）
     */
    private Integer componentType;

    /**
     * 组件选项，多选组件的选项配置
     */
    private List<LabelValueDataEntity> componentOptions;

    /**
     * 必填，1：是，0：否
     */
    private Integer required;

    /**
     * 字段布局方式（1-独占一行，2-一行两个字段）
     */
    private Integer columnSpan;

    /**
     * 排序
     */
    private Integer propOrder;

    /**
     * 备注
     */
    private String remark;

}
