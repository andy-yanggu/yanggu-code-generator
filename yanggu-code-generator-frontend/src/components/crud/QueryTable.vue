<template>
	<div>
		<!-- 搜索表单 -->
		<search-form
			v-model="queryForm"
			:fields="searchFields"
			:visible="showSearch"
			:loading="tableLoading"
			@search="emit('getDataList')"
			@reset="emit('resetQuery')"
		>
			<!-- 透传搜索字段插槽 -->
			<template v-for="(_, name) in $slots" :key="name" #[name]="slotData">
				<slot :name="name" v-bind="slotData ?? {}" />
			</template>
		</search-form>

		<!-- 表格 -->
		<data-table
			v-model:show-search="showSearch"
			v-model:maximized="maximized"
			v-model:columns="columns"
			:data="data"
			:loading="loading"
			:pagination="pagination"
			:page-sizes="pageSizes"
			:max-height="maxHeight"
			@selection-change="(s: any[]) => emit('selectionChange', s)"
			@sort-change="(s: any) => emit('sortChange', s)"
			@page-change="(p: number) => emit('pageChange', p)"
			@size-change="(s: number) => emit('sizeChange', s)"
			@get-data-list="emit('getDataList')"
			@reset-columns="emit('resetColumns')"
		>
			<!-- 工具栏左侧 -->
			<template #toolbar-left>
				<slot name="toolbar-left" />
			</template>
			<!-- 操作列 -->
			<template #operation="scope">
				<slot name="operation" v-bind="scope" />
			</template>
			<!-- 透传表格列插槽 -->
			<template v-for="(_, name) in columnSlots" :key="name" #[name]="scope">
				<slot :name="name" v-bind="scope" />
			</template>
		</data-table>
	</div>
</template>

<script setup lang="ts">
import type { Pagination, SearchFieldSchema, TableColumnSchema } from '@/types/schema'
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'

defineOptions({
	name: 'QueryTable'
})

// ====== defineModel ======
const queryForm = defineModel<Record<string, any>>('queryForm', { required: true })
const showSearch = defineModel<boolean>('showSearch', { default: true })
const maximized = defineModel<boolean>('maximized', { default: false })
const columns = defineModel<TableColumnSchema[]>('columns', { default: () => [] })

defineProps<{
	searchFields: SearchFieldSchema[]
	data: any[]
	loading?: boolean
	tableLoading?: boolean
	pagination?: Pagination
	pageSizes?: number[]
	maxHeight?: string
}>()

const emit = defineEmits<{
	(e: 'getDataList'): void
	(e: 'resetQuery'): void
	(e: 'selectionChange', selections: any[]): void
	(e: 'sortChange', sort: any): void
	(e: 'pageChange', page: number): void
	(e: 'sizeChange', size: number): void
	(e: 'resetColumns'): void
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
