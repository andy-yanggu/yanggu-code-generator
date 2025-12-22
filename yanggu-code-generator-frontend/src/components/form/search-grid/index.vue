<template>
	<el-row v-for="(row, rowIndex) in visibleRows" :key="rowIndex">
		<el-col v-for="col in row.items" :key="col.key" :span="24 / columns">
			<slot :name="col.type === 'item' ? col.key : 'actions'"></slot>
		</el-col>

		<el-col v-show="row.hasAction" :span="24 / columns">
			<slot name="actions"></slot>
			<template v-if="row.showToggle">
				<toggle-more v-model:showMore="showMore"></toggle-more>
			</template>
		</el-col>
	</el-row>
</template>

<script setup lang="ts">
import { computed, ref, useSlots } from 'vue'
import ToggleMore from '@/components/form/search-grid/toggle-more.vue'

const props = defineProps({
	columns: { type: Number, default: 3 } // 每行最大条件数
})

const slots = useSlots()
const showMore = ref(false)

// 获取 item-* 插槽名称并排序
const itemSlots = computed(() =>
	Object.keys(slots)
		.filter(key => key.startsWith('item-'))
		.sort((a, b) => parseInt(a.split('-')[1]) - parseInt(b.split('-')[1]))
)

const totalItems = computed(() => itemSlots.value.length)

// 生成行数据，每行包含 item 数组，并标记是否显示操作区
const visibleRows = computed(() => {
	const items = [...itemSlots.value].map(key => ({ key, type: 'item' as const }))
	const rows: { items: typeof items; hasAction: boolean; showToggle: boolean }[] = []

	if (totalItems.value <= props.columns) {
		// 少于等于 columns 条，操作区放在最后一行
		rows.push({ items, hasAction: true, showToggle: false })
	} else {
		// 大于 columns 条
		if (!showMore.value) {
			// 收起状态：第一行放 columns - 1 条 + 操作区 + 展开
			const firstRowItems = items.splice(0, props.columns - 1)
			rows.push({ items: firstRowItems, hasAction: true, showToggle: true })
		} else {
			// 展开状态：每行最多 columns 条，操作区放在最后一行 + 收起
			while (items.length) {
				const rowItems = items.splice(0, props.columns)
				const isLastRow = items.length === 0
				rows.push({ items: rowItems, hasAction: isLastRow, showToggle: isLastRow })
			}
		}
	}

	return rows
})
</script>
