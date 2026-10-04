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
			<!-- 透传搜索字段插槽（剥 query- 前缀） -->
			<template v-for="(fn, name) in searchFieldSlots" :key="name" #[name]="slotData">
				<component :is="() => fn(slotData ?? {})" />
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
			<!-- 透传表格列插槽（剥 table- 前缀） -->
			<template v-for="(fn, name) in tableColumnSlots" :key="name" #[name]="scope">
				<component :is="() => fn(scope ?? {})" />
			</template>
		</data-table>
	</div>
</template>

<script setup lang="ts">
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'

defineOptions({
	name: 'QueryTable'
})

// 接收 hook 返回值作为 prop（纯展示组件，不再内部调用 hook）
// 用 any 避免泛型 ReturnType 与页面具体实体类型之间的 TS2719 冲突
defineProps<{
	tableHook: any
	maxHeight?: string
	pageSizes?: number[]
}>()

// 插槽分路由：按前缀分类 → 剥前缀 → 分发到对应子组件
// table-* 剥 table- 给 DataTable，query-* 剥 query- 给 SearchForm
const allSlots = useSlots()

/** table-* 插槽：剥 table- 前缀，转发给 DataTable */
const tableColumnSlots = computed(() => {
	const result: Record<string, any> = {}
	for (const name in allSlots) {
		if (name.startsWith('table-')) {
			result[name.slice(6)] = allSlots[name]
		}
	}
	return result
})

/** query-* 插槽：剥 query- 前缀，转发给 SearchForm */
const searchFieldSlots = computed(() => {
	const result: Record<string, any> = {}
	for (const name in allSlots) {
		if (name.startsWith('query-')) {
			result[name.slice(6)] = allSlots[name]
		}
	}
	return result
})
</script>
