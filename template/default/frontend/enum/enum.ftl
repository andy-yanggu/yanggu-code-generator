import { EnumItem } from '@/util/enum'

//${enumDesc}枚举
const ${enumNamePascal}_Enum = [
<#list enumItemList as item >
    {  label: '${item.enumItemDesc}', value: '${item.enumItemValue}' }<#sep>,
</#list>
]
