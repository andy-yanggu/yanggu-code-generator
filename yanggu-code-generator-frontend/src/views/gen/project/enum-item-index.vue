<template>
	<el-dialog v-model="dialogVisible" :title="`枚举项（${enumNameRef}）`" width="80%" @close="dialogVisible = false">
		<query-table :table-hook="tableHook"></query-table>
	</el-dialog>
</template>

<script setup lang="ts">
import { genEnumItemApi } from '@/api'
import type { GenEnumItemEntity, GenEnumItemQuery, SearchFieldSchema, TableColumnSchema, UseCrudTableOptions } from '@/types'
import { useCrudTable } from '@/hooks'
import QueryTable from '@/components/crud/QueryTable.vue'

defineOptions({
	name: 'ProjectEnumItem'
})

const dialogVisible = ref(false)
const enumNameRef = ref('')

// 搜索字段
const searchFields: SearchFieldSchema[] = [
	{ key: 'enumItemName', label: '枚举项名称' }
]

// 表格列配置
const tableColumns: TableColumnSchema[] = [
	{ key: 'index', type: 'index', label: '序号', width: 60, align: 'center', disabled: true },
	{ key: 'enumItemName', label: '枚举项名称' },
	{ key: 'enumItemCode', label: '枚举项编码' },
	{ key: 'enumItemDesc', label: '枚举项描述' },
	{ key: 'enumItemOrder', label: '枚举项排序' },
	{ key: 'createTime', label: '创建时间', sortable: 'custom', minWidth: 120 },
	{ key: 'updateTime', label: '修改时间', sortable: 'custom', minWidth: 120 }
]

// 查询上下文（响应式，init 时更新 enumId）
const queryContext = reactive({
	enumId: -1
})

// 初始化查询表单数据
const initQueryForm = (): GenEnumItemQuery => ({
	enumItemName: '',
	enumId: -1
})

// 表格配置
const tableHook = useCrudTable<GenEnumItemEntity, GenEnumItemQuery>({
	dataListApi: genEnumItemApi.entityPage,
	tableKey: 'gen-project-enum-item',
	initQueryForm,
	searchSchema: searchFields,
	tableColumns,
	queryContext,
	mountedGetData: false,
	subject: '枚举项',
	showToolbarLeft: false
} as UseCrudTableOptions)

// 对外暴露的初始化方法
const init = (enumId: number, enumName: string) => {
	dialogVisible.value = true
	enumNameRef.value = enumName
	queryContext.enumId = enumId
	tableHook.query.reset()
}

defineExpose({
	init
})
</script>
