<template>
	<query-table ref="queryTableRef" :table-hook="tableHook" @selection-change="onSelectionChange"></query-table>
</template>

<script setup lang="ts">
import { TEMPLATE_GROUP_TYPES, TEMPLATE_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import { genTemplateApi } from '@/api'
import type { GenTemplateEntity, GenTemplateQuery, SearchFieldSchema, TableColumnSchema, UseCrudTableOptions } from '@/types'
import { useCrudTable } from '@/hooks'
import QueryTable from '@/components/crud/QueryTable.vue'

defineOptions({
	name: 'GenProjectTemplate'
})

const props = defineProps<{
	selectedIds?: number[]
}>()

const emit = defineEmits(['selectChange'])
const queryTableRef = ref()

// 搜索字段
const searchFields: SearchFieldSchema[] = [
	{ key: 'templateGroupName', label: '模板组名称' },
	{ key: 'templateGroupType', label: '模板组类型', component: 'select', options: TEMPLATE_GROUP_TYPES.items },
	{ key: 'fileName', label: '目录/文件名称' },
	{ key: 'templateType', label: '模板类型', component: 'select', options: TEMPLATE_TYPES.items }
]

// 表格列配置
const tableColumns: TableColumnSchema[] = [
	{ key: 'selection', type: 'selection', label: '', width: 50, align: 'center', fixed: 'left', disabled: true, reserveSelection: true },
	{ key: 'index', type: 'index', label: '序号', width: 60, align: 'center', disabled: true },
	{ key: 'templateGroupName', label: '模板组名称' },
	{ key: 'templateGroupType', label: '模板组类型', formatter: getLabel(TEMPLATE_GROUP_TYPES) },
	{ key: 'fileName', label: '目录/文件名称' },
	{ key: 'templateType', label: '模板类型', formatter: getLabel(TEMPLATE_TYPES) },
	{ key: 'generatorPath', label: '模板路径' },
	{ key: 'templateDesc', label: '描述' }
]

// 查询上下文（响应式，init 时更新 templateGroupIdList）
const queryContext = reactive({
	templateGroupIdList: [] as number[]
})

// 初始化查询表单数据
const initQueryForm = (): GenTemplateQuery => ({
	templateGroupIdList: [],
	templateGroupName: '',
	fileName: '',
	templateGroupType: '',
	templateType: ''
})

// 表格配置
const tableHook = useCrudTable<GenTemplateEntity, GenTemplateQuery>({
	dataListApi: genTemplateApi.voPage,
	tableKey: 'gen-project-template',
	initQueryForm,
	searchSchema: searchFields,
	tableColumns,
	queryContext,
	mountedGetData: false,
	subject: '模板',
	showToolbarLeft: false
} as UseCrudTableOptions)

// 勾选变化：转发给父组件
const onSelectionChange = (selections: any[]) => {
	emit('selectChange', selections)
}

// 数据加载后，根据 selectedIds 自动恢复勾选（v-if 重建时 prop 先于数据到达，watch 等数据到了再选）
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
const init = (templateGroupIdList: number[]) => {
	queryContext.templateGroupIdList = templateGroupIdList
	tableHook.query.reset()
}

defineExpose({
	init
})
</script>
