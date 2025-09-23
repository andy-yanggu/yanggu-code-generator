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
    ${item.enumItemNameAllUpper}(<#if item.enumItemCode?is_string>"${item.enumItemCode}"<#else>${item.enumItemCode}</#if>, "${item.enumItemDesc}"),
    <#if item_has_next>

    </#if>
    </#list>
    ;

    /**
     * code值
     */
    @JsonValue
    @EnumValue
    private final ${enumCodeType} code;

    /**
     * 描述
     */
    private final String description;

}
