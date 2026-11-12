<template>
	<!-- bare：独占整行的非数据项虚拟字段（分段标题 / 动态子表单容器）。
	     不套 el-form-item、无 label，内容完全交给 field-[key] 插槽；
	     未提供插槽时 <slot> 无兜底内容即整条不渲染，避免落进 DynamicField default 分支被兜底成空 el-input。 -->
	<slot v-if="field.display === 'bare'" :name="`field-${field.key}`" :model="model" :field="field" :mode="mode"></slot>
	<el-form-item v-else :prop="field.prop">
		<template #label>
			<form-label-tooltip v-if="field.tooltip" :label="field.label" :tooltip="field.tooltip" />
			<span v-else>{{ field.label }}</span>
		</template>
		<!-- 插槽覆盖（按 field.key 匹配；已经 normalizeFormField 补全，key 必定有值） -->
		<slot :name="`field-${field.key}`" :model="model" :field="field" :mode="mode">
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
import type { FormMode, NormalizedFormField } from '@/types/schema'
import DynamicField from './DynamicField.vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'

defineOptions({
	name: 'FormField'
})

const model = defineModel<Record<string, any>>({ required: true })

// 字段类型用归一化后的 NormalizedFormField：prop 必定为 string，
// 否则 model[field.prop] 会报 TS2538「undefined 不能作索引类型」
defineProps<{
	field: NormalizedFormField
	mode: FormMode
}>()
</script>

<style scoped>
.detail-text {
	color: var(--el-text-color-regular);
	line-height: 32px;
}
</style>
