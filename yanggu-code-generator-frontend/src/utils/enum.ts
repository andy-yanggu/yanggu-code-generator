import type { VNode } from 'vue'
import type { AnyEnum } from 'enum-plus'
import DictTag from '@/components/dict-tag/index.vue'

/**
 * 获取表格枚举文本（单独给el-table-column的formatter属性使用）
 */
export const getLabel = (enumObj: AnyEnum) => {
	return (_: any, __: any, value: any): string => {
		return enumObj.label(value) ?? ''
	}
}

/**
 * 获取表格枚举标签组件（单独给el-table-column的formatter属性使用，渲染为dict-tag）
 * el-table 的 formatter 原生支持返回 VNode；枚举项声明 type 字段（constant/enum.ts 的 TagType）
 * 即按对应颜色渲染 el-tag，未声明时走 dict-tag 的默认展示。
 * 参数签名与 getLabel 逐位对齐 TableColumnSchema.formatter 契约。
 */
export const getDictTag = (enumObj: AnyEnum) => {
	return (_row: any, _column?: any, cellValue?: any): VNode => {
		return h(DictTag, { value: cellValue, enumObj })
	}
}
