<!--
	SearchForm：由 SearchFieldConfig[] 配置驱动的查询表单组件
	- 根据 fields 数组自动渲染 el-form-item + 对应控件（input / select / date-range）
	- 支持具名插槽覆盖单个字段（用于自定义选项、option-label 等复杂场景）
	- 查询/重置按钮内置，通过事件通知父组件
-->
<template>
	<el-card v-if="visible" class="layout-query-card" shadow="hover">
		<el-form ref="formRef" :inline="true" :model="form" @keyup.enter="handleSearch">
			<el-form-item v-for="field in fields" :key="field.prop" :label="field.label" :prop="field.prop">
				<!-- 插槽优先：页面可自定义复杂搜索字段（如带 option-label 的 select） -->
				<slot :name="field.prop" :field="field" :form="form">
					<!-- input -->
					<el-input
						v-if="field.component === 'input' || !field.component"
						v-model="form[field.prop]"
						clearable
						:placeholder="field.placeholder"
						:style="inputStyle(field.width)"
					/>
					<!-- select -->
					<el-select
						v-else-if="field.component === 'select'"
						v-model="form[field.prop]"
						:options="field.options"
						:filterable="field.filterable"
						clearable
						:placeholder="field.placeholder"
						:style="inputStyle(field.width)"
					/>
					<!-- date-range / datetime-range -->
					<el-date-picker
						v-else-if="field.component === 'date-range' || field.component === 'datetime-range'"
						v-model="form[field.prop]"
						:type="field.component === 'datetime-range' ? 'datetimerange' : 'daterange'"
						range-separator="至"
						start-placeholder="开始日期"
						end-placeholder="结束日期"
						value-format="YYYY-MM-DD HH:mm:ss"
						:style="inputStyle((Number(field.width) ?? 160) + 180)"
					/>
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
import { Search, Refresh } from '@element-plus/icons-vue'
import type { SearchFieldConfig } from '@/types/hooks/search-field'

defineOptions({
	name: 'SearchForm'
})

defineProps<{
	/** 搜索字段配置列表 */
	fields: SearchFieldConfig[]
	/** 查询表单数据（reactive 对象，双向绑定） */
	form: Record<string, any>
	/** 是否显示 */
	visible: boolean
	/** 查询按钮加载态 */
	loading?: boolean
}>()

const emit = defineEmits<{
	/** 点击查询 */
	(e: 'search'): void
	/** 点击重置 */
	(e: 'reset'): void
}>()

const formRef = ref()

const inputStyle = (width?: number | string) => {
	const w = width ?? 160
	return { width: `${w}px` }
}

const handleSearch = async () => {
	try {
		await formRef.value?.validate()
		emit('search')
	} catch {
		// 校验不通过，不触发查询
	}
}

const handleReset = () => {
	formRef.value?.resetFields()
	emit('reset')
}
</script>
