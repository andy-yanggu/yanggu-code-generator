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
			<!-- 透传搜索字段插槽（query-xxx 剥前缀后作为 SearchForm 的字段插槽名） -->
			<!-- 用 <slot> 直接渲染，不要用 <component :is="() => fn(...)"> 包一层：后者每次 render
			     都产生新的函数式组件类型，会卸载重建插槽内容（el-select 展开态、输入焦点全丢）。
			     注意两个名字不能混用：#[s.short] 是对外插槽名（SearchForm 按 field.key 查找），
			     <slot :name="s.full"> 才是本组件 $slots 的真实键名（带 query- 前缀），
			     写成 <slot :name="s.short"> 会匹配不到而渲染成空注释。 -->
			<template v-for="s in searchFieldSlots" :key="s.full" #[s.short]="slotData">
				<slot :name="s.full" v-bind="slotData ?? {}"></slot>
			</template>
		</search-form>

		<!-- 表格 -->
		<data-table
			ref="dataTableRef"
			v-model:show-search="tableHook.search.visible"
			v-model:maximized="tableHook.refs.cardMaximized"
			v-model:columns="tableHook.columnConfig.list"
			:data="tableHook.table.data"
			:loading="tableHook.table.loading"
			:pagination="tableHook.table.pagination"
			:page-sizes="pageSizes"
			:max-height="maxHeight"
			:row-key="tableHook.refs.primaryKey"
			:row-index="tableHook.table.index"
			:show-toolbar-left="tableHook.refs.showToolbarLeft"
			@selection-change="onSelectionChange"
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
			<!-- 透传表格列插槽（table-xxx 剥前缀后作为 DataTable 的列插槽名） -->
			<!-- 同搜索插槽：# 用剥前缀的 s.short，<slot> 用带前缀的 s.full -->
			<template v-for="s in tableColumnSlots" :key="s.full" #[s.short]="scope">
				<slot :name="s.full" v-bind="scope ?? {}"></slot>
			</template>
		</data-table>
	</div>
</template>

<script setup lang="ts">
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'
import type { UseCrudTableReturn } from '@/types'

defineOptions({
	name: 'QueryTable'
})

// 接收 hook 返回值作为 prop（纯展示组件，不再内部调用 hook）
// 用具名契约类型而非 any：恢复组件内部对 tableHook 的类型检查（属性名写错能编译期拦住）
const props = defineProps<{
	tableHook: UseCrudTableReturn
	maxHeight?: string
	pageSizes?: number[]
}>()

const emit = defineEmits<{
	(e: 'selectionChange', selections: any[]): void
}>()

// 勾选变化：内部更新 hook 状态 + 向父组件转发事件
const onSelectionChange = (selections: any[]) => {
	props.tableHook.selection.onChange(selections)
	emit('selectionChange', selections)
}

// 插槽分路由：按前缀分类 → 剥前缀 → 分发到对应子组件
// table-* 剥 table- 给 DataTable，query-* 剥 query- 给 SearchForm
const allSlots = useSlots()

/**
 * 收集带指定前缀的插槽，并同时保留两个名字：
 * - full：本组件 $slots 里的真实键名，渲染 `<slot :name>` 时必须用它，否则取不到内容
 * - short：剥掉前缀后给子组件的插槽名，用于 `#[short]`
 * 只保留 short 会让 `<slot :name="short">` 匹配不到自己的插槽，内容渲染成空注释。
 */
const splitSlotNames = (prefix: string) =>
	Object.keys(allSlots)
		.filter(name => name.startsWith(prefix))
		.map(full => ({ full, short: full.slice(prefix.length) }))

/** table-* 插槽：剥 table- 前缀，转发给 DataTable */
const tableColumnSlots = computed(() => splitSlotNames('table-'))

/** query-* 插槽：剥 query- 前缀，转发给 SearchForm */
const searchFieldSlots = computed(() => splitSlotNames('query-'))

// 暴露 DataTable 引用，供外层穿透访问 el-table（如 toggleRowSelection）
const dataTableRef = ref()

// el-table 实例接线：hook 的 selection.clear() 需要调表格实例的 clearSelection()
// 才能真正清掉内部 selection store（reserveSelection 模式下已删除的行不会自行剔除）。
// 放在 onMounted：此时 DataTable 已挂载，defineExpose 的 ref 经 proxyRefs 自动解包为 el-table 实例
onMounted(() => {
	props.tableHook.selection.bindTable(dataTableRef.value?.tableRef)
})

defineExpose({
	get tableRef() {
		return dataTableRef.value?.tableRef
	}
})
</script>
