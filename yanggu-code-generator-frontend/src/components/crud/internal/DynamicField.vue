<template>
	<component :is="getComponent()" v-model="model" v-bind="getComponentProps"></component>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { ElCascader, ElCheckboxGroup, ElDatePicker, ElInput, ElInputNumber, ElRadioGroup, ElSelect, ElSwitch, ElTreeSelect } from 'element-plus'
import type { ComponentType, FormFieldSchema } from '@/types/schema'

defineOptions({
	name: 'DynamicField'
})

const model = defineModel<any>()
const props = defineProps<{
	field: FormFieldSchema
}>()

// 选择类组件用"请选择"，其他用"请输入"
const SELECT_TYPES = new Set(['select', 'radio', 'checkbox', 'tree-select', 'cascader'])

const computedPlaceholder = computed(() => {
	if (props.field.placeholder) return props.field.placeholder
	const component = props.field.component ?? 'input'
	const prefix = SELECT_TYPES.has(component) ? '请选择' : '请输入'
	return `${prefix}${props.field.label}`
})

defineExpose({ placeholder: computedPlaceholder })

// 获取组件对象
const getComponent = () => {
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
}

// 获取组件 props
const getComponentProps = computed(() => {
	const component = (props.field.component ?? 'input') as ComponentType
	const placeholder = computedPlaceholder.value
	const { options, filterable } = props.field

	const baseProps: Record<string, any> = {
		placeholder,
		clearable: true,
		filterable
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
		case 'tree-select':
			return { ...baseProps, data: options, checkStrictly: true }
		case 'cascader':
			return { ...baseProps, options }
		default:
			return baseProps
	}
})
</script>
