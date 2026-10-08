<template>
	<query-table ref="queryTableRef" :table-hook="tableHook" :show-toolbar-left="false" @selection-change="onSelectionChange">
		<!-- 操作列 -->
		<template #operation="scope">
			<el-button type="primary" link :icon="View" @click="enumItemIndexShow(scope.row.id, scope.row.enumName)">查看枚举项</el-button>
		</template>
	</query-table>

	<enum-item-index ref="enumItemIndexRef"></enum-item-index>
</template>

<script setup lang="ts">
import { genEnumApi } from '@/api'
import type { GenEnumEntity, GenEnumQuery, SearchFieldSchema, TableColumnSchema, UseCrudTableOptions } from '@/types'
import { useCrudTable } from '@/hooks'
import QueryTable from '@/components/crud/QueryTable.vue'
import EnumItemIndex from '@/views/gen/project/enum-item-index.vue'
import { View } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenProjectEnum'
})

const props = defineProps<{
	selectedIds?: number[]
}>()

const emit = defineEmits(['selectChange'])
const queryTableRef = ref()
const enumItemIndexRef = ref()

// 搜索字段
const searchFields: SearchFieldSchema[] = [
	{ key: 'enumName', label: '枚举名称' }
]

// 表格列配置
const tableColumns: TableColumnSchema[] = [
	{ key: 'selection', type: 'selection', label: '', width: 50, align: 'center', fixed: 'left', disabled: true, reserveSelection: true },
	{ key: 'index', type: 'index', label: '序号', width: 60, align: 'center', disabled: true },
	{ key: 'enumName', label: '枚举名称' },
	{ key: 'enumDesc', label: '枚举描述' },
	{ key: 'createTime', label: '创建时间' },
	{ key: 'updateTime', label: '更新时间' }
]

// 查询上下文（响应式，init 时更新 projectId）
const queryContext = reactive({
	projectId: '' as number | string
})

// 初始化查询表单数据
const initQueryForm = (): GenEnumQuery => ({
	enumName: '',
	projectId: ''
})

// 表格配置
const tableHook = useCrudTable<GenEnumEntity, GenEnumQuery>({
	dataListApi: genEnumApi.entityPage,
	tableKey: 'gen-project-enum',
	initQueryForm,
	searchSchema: searchFields,
	tableColumns,
	queryContext,
	mountedGetData: false,
	subject: '枚举'
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

// 查看枚举项
const enumItemIndexShow = (enumId: number, enumName: string) => {
	enumItemIndexRef.value.init(enumId, enumName)
}

defineExpose({
	init
})
</script>
