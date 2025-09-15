import { EnumItem } from '@/utils/enum'

<#list enumModelList as enumModel>
// ${enumModel.enumDesc}枚举
const ${enumModel.enumNameAllUpper}_ENUM: EnumItem[] = [
<#list enumModel.enumItemList as item >
    {  label: '${item.enumItemDesc}', value: '${item.enumItemCode}' }<#if item_has_next>,</#if>
</#list>
]

</#list>