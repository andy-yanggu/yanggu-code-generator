package ${projectPackage}.${projectNameDot}.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * ${enumDesc}枚举
 */
@Getter
@AllArgsConstructor
public enum ${enumNamePascal}Enum {

    <#list enumItemList as item >
    /**
     * ${item.enumItemDesc}
     */
    ${item.enumItemNameAllUpper}(${item.enumItemCode}, "${item.enumItemDesc}"),
    <#if item_has_next>

    </#if>
    </#list>
    ;

    /**
     * code值
     */
    @JsonValue
    @EnumValue
    private final Integer code;

    /**
     * 描述
     */
    private final String description;

}
