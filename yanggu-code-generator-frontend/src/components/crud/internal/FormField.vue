<template>
	<el-form-item :prop="field.prop">
		<template #label>
			<form-label-tooltip v-if="field.tooltip" :label="field.label" :tooltip="field.tooltip" />
			<span v-else>{{ field.label }}</span>
		</template>
		<!-- 插槽覆盖（按 field.key 匹配，未填时回退到 field.prop） -->
		<slot :name="`field-${field.key ?? field.prop}`" :model="model" :field="field">
			<!-- 详情模式：纯文本展示 -->
			<span v-if="mode === 'detail'" class="detail-text">
				{{ field.detailFormatter ? field.detailFormatter(model[field.prop]) : model[field.prop] }}
			</span>
			<!-- 编辑模式：动态字段 -->
			<dynamic-field v-else v-model="model[field.prop]" :field="field"></dynamic-field>
		</slot>
	</el-form-item>
</template>

<script setup lang="ts">
import type { FormFieldSchema, FormMode } from '@/types/schema'
import DynamicField from './DynamicField.vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'

defineOptions({
	name: 'FormField'
})

const model = defineModel<Record<string, any>>({ required: true })

defineProps<{
	field: FormFieldSchema
	mode: FormMode
}>()
</script>

<style scoped>
.detail-text {
	color: var(--el-text-color-regular);
	line-height: 32px;
}
</style>
