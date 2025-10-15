<template>
	<template v-for="item in propertyList" :key="item.id">
		<el-form-item :prop="`${modelValueProp}.${item.propKey}`">
			<!-- label标签 -->
			<template #label>
				<div style="display: flex; align-items: center">
					<span>{{ item.propTitle }}</span>
					<el-tooltip v-if="item.remark" :content="item.remark" effect="dark" placement="top">
						<el-icon style="margin-left: 5px; cursor: pointer"><InfoFilled></InfoFilled></el-icon>
					</el-tooltip>
				</div>
			</template>

			<!-- 文本输入 -->
			<el-input v-if="item.componentType === 0" v-model="formData[item.propKey]" :placeholder="`请输入${item.propTitle}`" clearable></el-input>

			<!-- 数字输入 -->
			<el-input-number v-else-if="item.componentType === 1" v-model="formData[item.propKey]" :min="0"></el-input-number>

			<!-- 下拉框 -->
			<el-select v-else-if="item.componentType === 2" v-model="formData[item.propKey]" :placeholder="`请选择${item.propTitle}`" clearable>
				<el-option v-for="selectItem in item.componentOptions" :key="selectItem.value" :value="selectItem.value">{{ selectItem.label }}</el-option>
			</el-select>

			<!-- 单选 -->
			<el-radio-group v-else-if="item.componentType === 3" v-model="formData[item.propKey]">
				<el-radio v-for="selectItem in item.componentOptions" :key="selectItem.value" :value="selectItem.value">
					{{ selectItem.label }}
				</el-radio>
			</el-radio-group>
		</el-form-item>
	</template>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'

/**
 * 下拉或单选项结构
 */
interface ComponentOption {
	label: string
	value: string | number
}

/**
 * 动态属性项定义
 */
export interface PropertyItem {
	id: number | string
	propKey: string
	propTitle: string
	componentType: number // 0: input, 1: number, 2: select, 3: radio
	required: number
	remark?: string
	defaultValue?: string | number | boolean
	componentOptions?: ComponentOption[]
}

const props = defineProps({
	propertyList: {
		type: Array as PropType<PropertyItem[]>,
		default: [] as PropertyItem[]
	},
	modelValue: {
		type: Object,
		required: true
	},
	modelValueProp: {
		type: String,
		default: 'modelValue'
	}
})

const emit = defineEmits(['update:modelValue'])

// 使用 computed 代理 modelValue，实现双向绑定
const formData = computed({
	get: () => props.modelValue,
	set: val => {
		// 监听 propertyList 变化，补齐默认值
		props.propertyList.forEach((item: PropertyItem) => {
			if (!(item.propKey in props.modelValue)) {
				val[item.propKey] = item.defaultValue ?? ''
			}
		})
		emit('update:modelValue', val)
	}
})
</script>
