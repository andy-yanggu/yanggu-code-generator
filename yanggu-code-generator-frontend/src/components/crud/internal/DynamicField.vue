<template>
	<!-- textarea -->
	<el-input
		v-if="field.component === 'textarea'"
		v-model="model"
		type="textarea"
		:rows="3"
		:placeholder="field.placeholder"
		clearable
	/>
	<!-- input（默认） -->
	<el-input
		v-else-if="field.component === 'input' || !field.component"
		v-model="model"
		:placeholder="field.placeholder"
		clearable
	/>
	<!-- select -->
	<el-select
		v-else-if="field.component === 'select'"
		v-model="model"
		:placeholder="field.placeholder"
		:filterable="field.filterable"
		clearable
	>
		<el-option v-for="opt in field.options" :key="opt.value" :label="opt.label" :value="opt.value" />
	</el-select>
	<!-- radio -->
	<el-radio-group v-else-if="field.component === 'radio'" v-model="model">
		<el-radio v-for="opt in field.options" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
	</el-radio-group>
	<!-- checkbox -->
	<el-checkbox-group v-else-if="field.component === 'checkbox'" v-model="model">
		<el-checkbox v-for="opt in field.options" :key="opt.value" :value="opt.value" :label="opt.label" />
	</el-checkbox-group>
	<!-- switch -->
	<el-switch v-else-if="field.component === 'switch'" v-model="model" />
	<!-- date-picker -->
	<el-date-picker
		v-else-if="field.component === 'date-picker'"
		v-model="model"
		type="date"
		:placeholder="field.placeholder"
		value-format="YYYY-MM-DD"
		clearable
	/>
	<!-- input-number -->
	<el-input-number v-else-if="field.component === 'input-number'" v-model="model" />
	<!-- tree-select -->
	<el-tree-select
		v-else-if="field.component === 'tree-select'"
		v-model="model"
		:data="field.options"
		:placeholder="field.placeholder"
		check-strictly
		clearable
	/>
	<!-- cascader -->
	<el-cascader
		v-else-if="field.component === 'cascader'"
		v-model="model"
		:options="field.options"
		:placeholder="field.placeholder"
		clearable
	/>
</template>

<script setup lang="ts">
import type { FormFieldSchema } from '@/types/schema'

defineOptions({
	name: 'DynamicField'
})

const model = defineModel<any>()

defineProps<{
	field: FormFieldSchema
}>()
</script>
