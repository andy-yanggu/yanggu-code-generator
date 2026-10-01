<template>
	<div>
		<!-- 搜索表单 -->
		<search-form
			v-model="tableHooks.search.form"
			:fields="tableHooks.search.fields"
			:visible="tableHooks.search.visible"
			:loading="tableHooks.table.loading"
			@search="tableHooks.query.getDataList()"
			@reset="tableHooks.query.reset()"
		>
			<!-- 透传搜索字段插槽 -->
			<template v-for="(_, name) in $slots" :key="name" #[name]="slotData">
				<slot :name="name" v-bind="slotData ?? {}"></slot>
			</template>
		</search-form>

		<!-- 表格 -->
		<data-table
			v-model:show-search="tableHooks.search.visible"
			v-model:maximized="tableHooks.refs.cardMaximized"
			v-model:columns="tableHooks.columnConfig.list"
			:data="tableHooks.table.data"
			:loading="tableHooks.table.loading"
			:pagination="tableHooks.table.pagination"
			:page-sizes="pageSizes"
			:max-height="maxHeight"
			@selection-change="tableHooks.selection.onChange"
			@sort-change="tableHooks.query.onSortChange"
			@page-change="tableHooks.query.onPageChange"
			@size-change="tableHooks.query.onSizeChange"
			@get-data-list="tableHooks.query.getDataList()"
			@reset-columns="tableHooks.columnConfig.reset()"
		>
			<!-- 工具栏左侧 -->
			<template #toolbar-left>
				<slot name="toolbar-left" :table="tableHooks"></slot>
			</template>
			<!-- 操作列 -->
			<template #operation="scope">
				<slot name="operation" v-bind="scope" :table="tableHooks"></slot>
			</template>
			<!-- 透传表格列插槽 -->
			<template v-for="(_, name) in columnSlots" :key="name" #[name]="scope">
				<slot :name="name" v-bind="scope"></slot>
			</template>
		</data-table>
	</div>
</template>

<script setup lang="ts">
import type { UseCrudTableOptions } from '@/types/hooks/use-crud-table'
import { useCrudTable } from '@/hooks/use-crud-table'
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'

defineOptions({
	name: 'QueryTable'
})

const props = withDefaults(defineProps<UseCrudTableOptions & { maxHeight?: string }>(), {
	pageSize: 10,
	pageSizes: () => [10, 20, 50, 100, 200],
	primaryKey: 'id',
	subject: '数据'
})

// 内部创建表格 hook
const tableHooks = useCrudTable(props)

// 暴露 hook 供父组件使用
defineExpose({
	tableHooks
})

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
