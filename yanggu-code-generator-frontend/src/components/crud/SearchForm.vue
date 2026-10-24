<template>
	<el-card v-if="visible" class="layout-query-card" shadow="hover">
		<el-form ref="formRef" :inline="true" :model="model" :rules="fieldRules" @keyup.enter="handleSearch">
			<el-form-item v-for="(field, index) in normalizedFields" :key="field.key" :prop="field.prop">
				<template #label>
					<form-label-tooltip v-if="field.tooltip" :label="field.label" :tooltip="field.tooltip" />
					<span v-else>{{ field.label }}</span>
				</template>
				<!-- 插槽覆盖（按 field.key 匹配） -->
				<slot :name="field.key" :field="field" :form="model">
					<dynamic-field v-model="model[field.prop]" :field="formFields[index]"></dynamic-field>
				</slot>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" :icon="Search" :loading="loading" @click="handleSearch">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button :icon="Refresh" @click="handleReset">重置</el-button>
			</el-form-item>
		</el-form>
	</el-card>
</template>

<script setup lang="ts">
import { Refresh, Search } from '@element-plus/icons-vue'
import type { FormFieldSchema, SearchFieldSchema } from '@/types/schema'
import { normalizeSearchField } from '@/types/schema'
import type { FormItemRule } from 'element-plus'
import DynamicField from './internal/DynamicField.vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'

defineOptions({
	name: 'CrudSearchForm'
})

const model = defineModel<Record<string, any>>({ required: true })

const props = defineProps<{
	fields: SearchFieldSchema[]
	visible: boolean
	loading?: boolean
}>()

const emit = defineEmits<{
	(e: 'search'): void
	(e: 'reset'): void
}>()

const formRef = ref()

// 归一化搜索字段
const normalizedFields = computed(() => props.fields.map(normalizeSearchField))

// 从字段 schema 收集校验规则
const fieldRules = computed<Record<string, FormItemRule[]>>(() => {
	const rules: Record<string, FormItemRule[]> = {}
	for (const field of normalizedFields.value) {
		if (field.ruleList?.length) {
			rules[field.prop] = [...field.ruleList]
		}
	}
	return rules
})

// SearchFieldSchema → FormFieldSchema（DynamicField 接收 FormFieldSchema）
// 逐项白名单映射：这里没列出的属性会被静默丢掉（clearable 曾因此在查询区失效），新增字段属性需同步补到这里
const searchToFormField = (field: SearchFieldSchema): FormFieldSchema => ({
	prop: field.prop,
	label: field.label,
	component: field.component,
	placeholder: field.placeholder,
	width: field.width,
	options: field.options,
	clearable: field.clearable,
	filterable: field.filterable
})

// 提前映射成 DynamicField 需要的字段（与 normalizedFields 下标一一对应）。
// 不要在模板里直接调 searchToFormField：模板读了 model[field.prop]，输入一个字符就会重渲染，
// 每次渲染都为每个字段新建对象，导致所有 DynamicField 的 field prop 全部换引用。
const formFields = computed(() => normalizedFields.value.map(searchToFormField))

// 查询（内部校验）
const handleSearch = async () => {
	try {
		await formRef.value?.validate()
		emit('search')
	} catch {
		// 校验不通过
	}
}

// 重置
const handleReset = () => {
	formRef.value?.resetFields()
	emit('reset')
}
</script>
