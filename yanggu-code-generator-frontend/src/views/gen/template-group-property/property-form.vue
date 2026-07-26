<template>
	<template v-for="(row, rowIndex) in rows" :key="rowIndex">
		<el-row>
			<el-col v-for="item in row" :key="item.id as number" :span="item.columnSpan === 1 ? 24 : 12">
				<el-form-item :prop="`${modelValueProp}.${item.propKey}`">
					<!-- label -->
					<template #label>
						<form-label-tooltip :label="item.propTitle" :tooltip="item.remark"></form-label-tooltip>
					</template>

					<!-- 动态渲染组件 -->
					<component :is="getComponentType(item)" v-model="formData[item.propKey]" v-bind="getComponentProps(item)"></component>
				</el-form-item>
			</el-col>
		</el-row>
	</template>
</template>
<script setup lang="ts">
import { computed, PropType } from 'vue'
import { ElInput, ElInputNumber, ElSelect, ElRadioGroup, ElCheckboxGroup, ElSwitch } from 'element-plus'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'
import { GenTemplateGroupPropertyEntity } from '@/types'

defineOptions({
	name: 'GenTemplateGroupPropertyPropertyForm'
})

const props = defineProps({
	propertyList: {
		type: Array as PropType<GenTemplateGroupPropertyEntity[]>,
		required: true
	},
	modelValueProp: {
		type: String,
		default: 'templateGroupPropertyData'
	}
})

const formData = defineModel('formData', { type: Object })

// -------- 行布局计算（核心部分）-----------
const rows = computed(() => {
	const res: GenTemplateGroupPropertyEntity[][] = []
	const list = props.propertyList ?? []
	if (list.length === 0) {
		return res
	}

	// 计算一个 item 占用的实际 span（标准化为 24 格布局）
	const calcSpan = (item: GenTemplateGroupPropertyEntity) => {
		const cs = Number(item.columnSpan) || 1
		// 你的逻辑：columnSpan 越大，占越少格；columnSpan=1 -> 24，columnSpan=2 -> 12，columnSpan=3 -> 8...
		const span = Math.floor(24 / cs)
		return Math.min(Math.max(span, 1), 24)
	}

	// 1) 按 columnSpan 建立分组
	const groups = new Map<number, GenTemplateGroupPropertyEntity[]>()
	for (const item of list) {
		const key = Number(item.columnSpan) || 1
		if (!groups.has(key)) {
			groups.set(key, [])
		}
		groups.get(key)!.push(item)
	}

	// 2) 对 key 进行降序排序（columnSpan 大的优先）
	const sortedKeys = Array.from(groups.keys()).sort((a, b) => b - a)

	// 3) 遍历每个降序 key，组内按 propOrder 排序，并按内容塞满 24
	for (const key of sortedKeys) {
		const groupItems = groups.get(key)!

		// 组内按 propOrder 排序
		const sorted = groupItems.slice().sort((a, b) => (a.propOrder ?? 0) - (b.propOrder ?? 0))

		let currentRow: GenTemplateGroupPropertyEntity[] = []
		let currentUsed = 0

		for (const item of sorted) {
			const span = calcSpan(item)

			// 单项独占整行
			if (span >= 24) {
				if (currentRow.length > 0) {
					res.push(currentRow)
					currentRow = []
					currentUsed = 0
				}
				res.push([item])
				continue
			}

			// 本行放不下时，先推入当前行，再开新行
			if (currentUsed + span > 24) {
				if (currentRow.length > 0) {
					res.push(currentRow)
				}
				currentRow = []
				currentUsed = 0
			}

			// 放入当前行
			currentRow.push(item)
			currentUsed += span

			// 正好满行
			if (currentUsed === 24) {
				res.push(currentRow)
				currentRow = []
				currentUsed = 0
			}
		}

		// 当前组的剩余内容
		if (currentRow.length > 0) {
			res.push(currentRow)
		}
	}

	return res
})

// -------- 动态渲染组件 --------
// 注意：必须返回组件对象而非字符串，因为 <component :is> 使用函数返回的字符串时，
// unplugin-vue-components 无法在编译时静态分析，导致组件未被解析，props 被当作 HTML 属性
const componentMap: Record<number, object> = {
	0: ElInput,
	1: ElInputNumber,
	2: ElSelect,
	3: ElRadioGroup,
	4: ElCheckboxGroup,
	5: ElSwitch
}

const getComponentType = (item: GenTemplateGroupPropertyEntity) => {
	return componentMap[Number(item.componentType)] ?? ElInput
}

const getComponentProps = (item: GenTemplateGroupPropertyEntity) => {
	switch (item.componentType) {
		case 0:
			return { placeholder: `请输入${item.propTitle}`, clearable: true }

		case 1:
			return {}

		case 2:
			return {
				placeholder: `请选择${item.propTitle}`,
				filterable: true,
				clearable: true,
				options: item.componentOptions
			}

		case 3:
			return {
				options: item.componentOptions
			}
		case 4:
			return {
				options: item.componentOptions
			}

		case 5:
			return {
				inactiveText: item.componentOptions?.[0]?.label,
				inactiveValue: item.componentOptions?.[0]?.value,
				activeText: item.componentOptions?.[1]?.label,
				activeValue: item.componentOptions?.[1]?.value,
				inlinePrompt: true
			}

		default:
			return {}
	}
}
</script>
