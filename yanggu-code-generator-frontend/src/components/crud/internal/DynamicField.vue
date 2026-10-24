<template>
	<!-- 包裹 div 用于消除 el-dialog Transition 的动态组件警告，display: contents 不影响布局 -->
	<div style="display: contents">
		<component :is="currentComponent" v-model="model" v-bind="getComponentProps"></component>
	</div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { ElCascader, ElCheckboxGroup, ElDatePicker, ElInput, ElInputNumber, ElRadioGroup, ElSelect, ElSwitch, ElTreeSelect } from 'element-plus'
import type { ComponentType, FormFieldSchema } from '@/types/schema'
import { DEFAULT_FIELD_CLEARABLE, DEFAULT_FIELD_FILTERABLE } from '@/config/crud'

defineOptions({
	name: 'DynamicField'
})

const model = defineModel<any>()
const props = defineProps<{
	field: FormFieldSchema
}>()

// 选择类组件用"请选择"，其他用"请输入"；区间选择类用起止占位符，不使用 placeholder
const SELECT_TYPES = new Set(['select', 'radio', 'checkbox', 'tree-select', 'cascader'])
const RANGE_TYPES = new Set(['date-range', 'datetime-range'])

const computedPlaceholder = computed(() => {
	if (props.field.placeholder) return props.field.placeholder
	const component = props.field.component ?? 'input'
	if (RANGE_TYPES.has(component)) return ''
	const prefix = SELECT_TYPES.has(component) ? '请选择' : '请输入'
	return `${prefix}${props.field.label}`
})

defineExpose({ placeholder: computedPlaceholder })

// 获取组件对象
const currentComponent = computed(() => {
	const component = (props.field.component ?? 'input') as ComponentType
	switch (component) {
		case 'textarea':
		case 'input':
			return ElInput
		case 'select':
			return ElSelect
		case 'radio':
			return ElRadioGroup
		case 'checkbox':
			return ElCheckboxGroup
		case 'switch':
			return ElSwitch
		case 'date-picker':
		case 'datetime-picker':
		case 'date-range':
		case 'datetime-range':
			return ElDatePicker
		case 'input-number':
			return ElInputNumber
		case 'tree-select':
			return ElTreeSelect
		case 'cascader':
			return ElCascader
		default:
			return ElInput
	}
})

// 获取组件 props
const getComponentProps = computed(() => {
	const component = (props.field.component ?? 'input') as ComponentType
	const placeholder = computedPlaceholder.value
	const { options, width } = props.field
	// clearable / filterable 由 schema 决定，缺省为 true（与 constant/crud 中注入的字段默认值一致）。
	// 传统模式（直传 formSchema / searchSchema）的 normalize* 不补默认值，必须在这里兜底
	const clearable = props.field.clearable ?? DEFAULT_FIELD_CLEARABLE
	const filterable = props.field.filterable ?? DEFAULT_FIELD_FILTERABLE

	const style = width ? { width: typeof width === 'number' ? `${width}px` : width } : undefined

	const baseProps: Record<string, any> = {
		placeholder,
		clearable,
		filterable,
		style
	}

	// 区间选择器不吃 placeholder，改用起止占位符
	const rangeBaseProps: Record<string, any> = {
		clearable,
		startPlaceholder: '开始日期',
		endPlaceholder: '结束日期',
		rangeSeparator: '至',
		unlinkPanels: true,
		style
	}

	switch (component) {
		case 'textarea':
			return { ...baseProps, type: 'textarea', rows: 3 }
		case 'select':
			return { ...baseProps, options }
		case 'radio':
		case 'checkbox':
			return { options }
		case 'date-picker':
			return { ...baseProps, type: 'date', valueFormat: 'YYYY-MM-DD' }
		case 'datetime-picker':
			return { ...baseProps, type: 'datetime', valueFormat: 'YYYY-MM-DD HH:mm:ss' }
		case 'date-range':
			return { ...rangeBaseProps, type: 'daterange', valueFormat: 'YYYY-MM-DD' }
		case 'datetime-range':
			return {
				...rangeBaseProps,
				type: 'datetimerange',
				valueFormat: 'YYYY-MM-DD HH:mm:ss',
				startPlaceholder: '开始时间',
				endPlaceholder: '结束时间'
			}
		case 'tree-select':
			return { ...baseProps, data: options, checkStrictly: true }
		case 'cascader':
			return { ...baseProps, options }
		default:
			return baseProps
	}
})
</script>
