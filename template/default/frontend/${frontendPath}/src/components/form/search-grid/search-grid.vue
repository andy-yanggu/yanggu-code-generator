<template>
	<el-row v-for="(row, rowIndex) in visibleRows" :key="rowIndex">
		<!-- 条件项 -->
		<el-col v-for="col in row.items" :key="col.key" :span="colSpan">
			<slot :name="col.key"></slot>
		</el-col>

		<!-- 操作区 -->
		<el-col v-if="row.hasAction" :span="colSpan" :offset="row.actionOffset">
			<slot name="actions"></slot>
			<toggle-more v-if="row.showToggle" v-model:showMore="showMore"></toggle-more>
		</el-col>
	</el-row>
</template>

<script setup lang="ts">
import { computed, ref, useSlots } from 'vue'
import ToggleMore from '@/components/form/search-grid/toggle-more.vue'

const props = defineProps({
	// 每行最大条件数
	columns: {
		type: Number,
		default: 3
	}
})

const slots = useSlots()
const showMore = ref(false)

/**
 * 单列 span
 */
const colSpan = computed(() => Math.floor(24 / props.columns))

/**
 * 行结构
 */
type Row = {
	items: { key: string; type: 'item' }[]
	hasAction: boolean
	showToggle: boolean
	actionOffset: number
}

const actionOffsetInRow = (itemsInRow: number) => (props.columns - itemsInRow - 1) * colSpan.value

const actionOffsetNewRow = () => (props.columns - 1) * colSpan.value

const createRow = (items: Row['items'], hasAction = false, showToggle = false, actionOffset = 0): Row => ({
	items,
	hasAction,
	showToggle,
	actionOffset
})

/**
 * 可见行计算
 */
const visibleRows = computed<Row[]>(() => {
	const itemSlots = Object.keys(slots)
		.filter(key => key.startsWith('item-'))
		.sort((a, b) => Number(a.split('-')[1]) - Number(b.split('-')[1]))

	const items = itemSlots.map(key => ({ key, type: 'item' as const }))
	const rows: Row[] = []

	// ===== 条件数 < columns =====
	if (items.length < props.columns) {
		rows.push(createRow(items, true, false, actionOffsetInRow(items.length)))
		return rows
	}

	// ===== 收起态 =====
	if (!showMore.value) {
		const firstRowItems = items.splice(0, props.columns - 1)
		rows.push(createRow(firstRowItems, true, true, 0))
		return rows
	}

	// ===== 展开态 =====
	while (items.length) {
		const rowItems = items.splice(0, props.columns)
		const isLastRow = items.length === 0

		// 非最后一行：只放条件
		if (!isLastRow) {
			rows.push(createRow(rowItems))
			continue
		}

		// ===== 最后一行 =====
		if (rowItems.length < props.columns) {
			// 同行放 actions
			rows.push(createRow(rowItems, true, true, actionOffsetInRow(rowItems.length)))
		} else {
			// 行满 → actions 独立一行
			rows.push(createRow(rowItems))
			rows.push(createRow([], true, true, actionOffsetNewRow()))
		}
	}

	return rows
})
</script>
