<#if index == 0>
import { EnumItem } from '@/util/enum'
</#if>

//${enumDesc}枚举
const ${enumNameAllUpper}_ENUM: EnumItem[] = [
<#list enumItemList as item >
    {  label: '${item.enumItemDesc}', value: '${item.enumItemCode}' }<#sep>,
</#list>
]
