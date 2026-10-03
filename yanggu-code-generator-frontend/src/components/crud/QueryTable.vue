<template>
	<div>
		<!-- 搜索表单 -->
		<!-- eslint-disable vue/no-mutating-props -->
		<!-- v-model 绑定到 prop 的嵌套 reactive 属性是安全的（修改的是 hook 内部的 ref，不是 prop 引用本身） -->
		<search-form
			v-model="tableHook.search.form"
			:fields="tableHook.search.fields"
			:visible="tableHook.search.visible"
			:loading="tableHook.table.loading"
			@search="tableHook.query.getDataList()"
			@reset="tableHook.query.reset()"
		>
			<!-- 透传搜索字段插槽 -->
			<template v-for="(_, name) in $slots" :key="name" #[name]="slotData">
				<slot :name="name" v-bind="slotData ?? {}"></slot>
			</template>
		</search-form>

		<!-- 表格 -->
		<data-table
			v-model:show-search="tableHook.search.visible"
			v-model:maximized="tableHook.refs.cardMaximized"
			v-model:columns="tableHook.columnConfig.list"
			:data="tableHook.table.data"
			:loading="tableHook.table.loading"
			:pagination="tableHook.table.pagination"
			:page-sizes="pageSizes"
			:max-height="maxHeight"
			@selection-change="tableHook.selection.onChange"
			@sort-change="tableHook.query.onSortChange"
			@page-change="tableHook.query.onPageChange"
			@size-change="tableHook.query.onSizeChange"
			@get-data-list="tableHook.query.getDataList()"
			@reset-columns="tableHook.columnConfig.reset()"
		>
			<!-- 工具栏左侧 -->
			<template #toolbar-left>
				<slot name="toolbar-left" :table-hook="tableHook"></slot>
			</template>
			<!-- 操作列 -->
			<template #operation="scope">
				<slot name="operation" v-bind="scope" :table-hook="tableHook"></slot>
			</template>
			<!-- 透传表格列插槽 -->
			<template v-for="(_, name) in columnSlots" :key="name" #[name]="scope">
				<slot :name="name" v-bind="scope"></slot>
			</template>
		</data-table>
	</div>
</template>

<script setup lang="ts">
import type { useCrudTable } from '@/hooks/use-crud-table'
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'

defineOptions({
	name: 'QueryTable'
})

// 接收 hook 返回值作为 prop（纯展示组件，不再内部调用 hook）
defineProps<{
	tableHook: ReturnType<typeof useCrudTable>
	maxHeight?: string
	pageSizes?: number[]
}>()

// 过滤出表格列插槽（排除已知非列插槽）
const knownSlots = new Set(['toolbar-left', 'operation'])
const columnSlots = computed(() => {
	const slots: Record<string, any> = {}
	for (const name in useSlots()) {
		if (!knownSlots.has(name)) {
			slots[name] = true
		}
	}
	return slots
})
</script>
