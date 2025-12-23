<template>
	<el-row v-for="(row, rowIndex) in visibleRows" :key="rowIndex">
		<!-- 条件项 -->
		<el-col v-for="col in row.items" :key="col.key" :span="colSpan">
			<slot :name="col.key"></slot>
		</el-col>

		<!-- 操作区 -->
		<el-col v-if="row.hasAction" :span="colSpan" :offset="row.actionAlignRight ? actionOffset : 0">
			<slot name="actions"></slot>
			<toggle-more v-if="row.showToggle" v-model:showMore="showMore"></toggle-more>
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

/**
 * 单列 span
 */
const colSpan = computed(() => Math.floor(24 / props.columns))

/**
 * actions 右对齐时需要的 offset
 * 24 - colSpan = 推到最右一列
 */
const actionOffset = computed(() => 24 - colSpan.value)

/**
 * 行结构
 */
type Row = {
	items: { key: string; type: 'item' }[]
	hasAction: boolean
	showToggle: boolean
	actionAlignRight: boolean
}

/**
 * 可见行计算
 */
const visibleRows = computed<Row[]>(() => {
	// 获取 item-* 插槽并排序
	const itemSlots = Object.keys(slots)
		.filter(key => key.startsWith('item-'))
		.sort((a, b) => Number(a.split('-')[1]) - Number(b.split('-')[1]))

	const items = itemSlots.map(key => ({ key, type: 'item' as const }))
	const rows: Row[] = []

	// 条件数 < columns：actions 紧跟在后
	if (items.length < props.columns) {
		rows.push({
			items,
			hasAction: true,
			showToggle: false,
			actionAlignRight: false
		})
		return rows
	}

	// 条件数 > columns
	if (!showMore.value) {
		// 收起态：columns - 1 + actions + 展开
		const firstRowItems = items.splice(0, props.columns - 1)

		rows.push({
			items: firstRowItems,
			hasAction: true,
			showToggle: true,
			actionAlignRight: false
		})

		return rows
	}

	// 展开态：每行 columns 条，actions 放最后一行
	while (items.length) {
		const rowItems = items.splice(0, props.columns)
		const isLastRow = items.length === 0

		rows.push({
			items: rowItems,
			hasAction: isLastRow,
			showToggle: isLastRow,
			// ⭐ 关键逻辑：
			// 最后一行刚好占满 → actions 另起一行 → 右对齐
			actionAlignRight: isLastRow && rowItems.length === props.columns
		})
	}

	return rows
})
</script>
