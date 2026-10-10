<template>
	<query-table ref="queryTableRef" :table-hook="tableHook" @selection-change="onSelectionChange"></query-table>
</template>

<script setup lang="ts">
import { genTableApi } from '@/api'
import type { GenTableEntity, GenTableQuery, SearchFieldSchema, TableColumnSchema, UseCrudTableOptions } from '@/types'
import { useCrudTable } from '@/hooks'
import QueryTable from '@/components/crud/QueryTable.vue'

defineOptions({
	name: 'GenProjectTable'
})

const props = defineProps<{
	selectedIds?: number[]
}>()

const emit = defineEmits(['selectChange'])
const queryTableRef = ref()

// 搜索字段
const searchFields: SearchFieldSchema[] = [
	{ key: 'tableName', label: '表名' },
	{ key: 'className', label: '类名' }
]

// 表格列配置
const tableColumns: TableColumnSchema[] = [
	{ key: 'selection', type: 'selection', label: '', width: 50, align: 'center', fixed: 'left', disabled: true, reserveSelection: true },
	{ key: 'index', type: 'index', label: '序号', width: 60, align: 'center', disabled: true },
	{ key: 'tableName', label: '表名' },
	{ key: 'className', label: '类名' },
	{ key: 'functionName', label: '功能名' },
	{ key: 'tableComment', label: '注释' },
	{ key: 'version', label: '版本' },
	{ key: 'author', label: '作者' }
]

// 查询上下文（响应式，init 时更新 projectId）
const queryContext = reactive({
	projectId: -1 as number | string
})

// 初始化查询表单数据
const initQueryForm = (): GenTableQuery => ({
	tableName: '',
	className: '',
	projectId: -1
})

// 表格配置
const tableHook = useCrudTable<GenTableEntity, GenTableQuery>({
	dataListApi: genTableApi.entityPage,
	tableKey: 'gen-project-table',
	initQueryForm,
	searchSchema: searchFields,
	tableColumns,
	queryContext,
	mountedGetData: false,
	subject: '表',
	showToolbarLeft: false
} as UseCrudTableOptions)

// 勾选变化：转发给父组件
const onSelectionChange = (selections: any[]) => {
	emit('selectChange', selections)
}

// 数据加载后，根据 selectedIds 自动恢复勾选
watch(
	() => tableHook.table.data,
	data => {
		if (data.length === 0 || !props.selectedIds?.length) return
		const elTableRef = queryTableRef.value?.tableRef
		if (!elTableRef) return
		const rows = data.filter((row: any) => props.selectedIds!.includes(row.id))
		rows.forEach((row: any) => elTableRef.toggleRowSelection(row, true))
	},
	{ flush: 'post' }
)

// 对外暴露的初始化方法
const init = (projectId: number) => {
	queryContext.projectId = projectId
	tableHook.query.reset()
}

defineExpose({
	init
})
</script>
