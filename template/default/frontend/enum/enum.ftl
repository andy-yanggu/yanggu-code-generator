import { EnumItem } from '@/utils/enum'

//${enumDesc}枚举
const ${enumNameAllUpper}_ENUM: EnumItem[] = [
<#list enumItemList as item >
    {  label: '${item.enumItemDesc}', value: '${item.enumItemCode}' }<#if item_has_next>,</#if>
</#list>
]
