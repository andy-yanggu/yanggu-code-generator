package com.yanggu.code.generator.domain.vo;

import com.yanggu.code.generator.enums.VariableKindEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 模板变量目录节点VO
 */
@Data
@Schema(description = "模板变量目录节点VO")
public class VariableNodeVO {

    /**
     * 属性名，如 fieldList；MAP 节点的子节点为模板组属性 propKey
     */
    @Schema(description = "属性名")
    private String name;

    /**
     * 从根算起的完整属性路径，如 tableModelList.fieldList
     */
    @Schema(description = "从根算起的完整属性路径")
    private String path;

    /**
     * Java 类型简名，如 List<TableFieldModel>、String、Object
     */
    @Schema(description = "Java 类型简名")
    private String javaType;

    /**
     * 变量分类，前端渲染与插入行为的唯一依据
     */
    @Schema(description = "变量分类（SCALAR/SCALAR_LIST/MODEL_LIST/OBJECT/MAP）")
    private VariableKindEnum kind;

    /**
     * {@link com.yanggu.code.generator.domain.model.VarDesc#value()}，缺注解时为空串
     */
    @Schema(description = "中文说明，缺注解时为空串")
    private String desc;

    /**
     * 默认循环变量名，kind 为集合类时有值
     */
    @Schema(description = "默认循环变量名")
    private String loopVar;

    /**
     * 渲染时是否可能为 null
     */
    @Schema(description = "渲染时是否可能为 null")
    private Boolean nullable;

    /**
     * 子变量列表
     */
    @Schema(description = "子变量列表")
    private List<VariableNodeVO> children;

}
