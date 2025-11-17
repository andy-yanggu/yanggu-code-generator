<template>
	<template v-for="item in propertyList" :key="item.id">
		<el-form-item :prop="`${modelValueProp}.${item.propKey}`">
			<!-- label标签 -->
			<template #label>
				<form-label-tooltip :label="item.propTitle" :tooltip="item.remark"></form-label-tooltip>
			</template>

			<!-- 文本输入 -->
			<el-input v-if="item.componentType === 0" v-model="formData[item.propKey]" :placeholder="`请输入${item.propTitle}`" clearable></el-input>

			<!-- 数字输入 -->
			<el-input-number v-else-if="item.componentType === 1" v-model="formData[item.propKey]"></el-input-number>

			<!-- 下拉框 -->
			<el-select v-else-if="item.componentType === 2" v-model="formData[item.propKey]" :placeholder="`请选择${item.propTitle}`" filterable clearable>
				<el-option v-for="selectItem in item.componentOptions" :key="selectItem.value" :value="selectItem.value">{{ selectItem.label }}</el-option>
			</el-select>

			<!-- 单选 -->
			<el-radio-group v-else-if="item.componentType === 3" v-model="formData[item.propKey]">
				<el-radio v-for="selectItem in item.componentOptions" :key="selectItem.value" :value="selectItem.value">
					{{ selectItem.label }}
				</el-radio>
			</el-radio-group>

			<!-- 多选框	-->
			<el-checkbox-group v-else-if="item.componentType === 4" v-model="formData[item.propKey]">
				<el-checkbox
					v-for="selectItem in item.componentOptions"
					:key="selectItem.value"
					:value="selectItem.value"
					:label="selectItem.label"
				></el-checkbox>
			</el-checkbox-group>

			<!-- 开关 -->
			<el-switch
				v-else-if="item.componentType === 5"
				v-model="formData[item.propKey]"
				:inactive-value="item.componentOptions![0].value"
				:active-value="item.componentOptions![1].value"
				:inactive-text="item.componentOptions![0].label"
				:active-text="item.componentOptions![1].label"
				inline-prompt
			></el-switch>
		</el-form-item>
	</template>
</template>

<script setup lang="ts">
import { nextTick, onMounted, PropType } from 'vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'
import { GenTemplateGroupPropertyEntity } from '@/api/gen/template-group-property'

defineOptions({
	name: 'GenTemplateGroupPropertyPropertyForm'
})

const props = defineProps({
	propertyList: {
		type: Array as PropType<GenTemplateGroupPropertyEntity[]>,
		default: [] as GenTemplateGroupPropertyEntity[]
	},
	modelValueProp: {
		type: String,
		default: 'modelValue'
	}
})

// 定义双向绑定表单数据
const formData = defineModel('formData', {
	type: Object
})

onMounted(() => {
	nextTick(() => {
		// 补齐默认值
		props.propertyList.forEach((item: GenTemplateGroupPropertyEntity) => {
			if (!formData.value[item.propKey] && item.propDefaultValue) {
				formData.value[item.propKey] = item.propDefaultValue
			}
		})
	})
})
</script>
