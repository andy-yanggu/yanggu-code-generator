<template>
	<el-card v-if="visible" class="layout-query-card" shadow="hover">
		<el-form ref="formRef" :inline="true" :model="model" :rules="fieldRules" @keyup.enter="handleSearch">
			<el-form-item v-for="field in normalizedFields" :key="field.key" :label="field.label" :prop="field.prop">
				<!-- 插槽覆盖（按 field.key 匹配） -->
				<slot :name="field.key" :field="field" :form="model">
					<dynamic-field v-model="model[field.prop]" :field="searchToFormField(field)"></dynamic-field>
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
	for (const field of props.fields) {
		if (field.ruleList?.length) {
			rules[field.prop] = [...field.ruleList]
		}
	}
	return rules
})

// SearchFieldSchema → FormFieldSchema（DynamicField 接收 FormFieldSchema）
const searchToFormField = (field: SearchFieldSchema): FormFieldSchema => ({
	prop: field.prop,
	label: field.label,
	component: field.component,
	placeholder: field.placeholder,
	options: field.options,
	filterable: field.filterable
})

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
