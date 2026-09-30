<template>
	<query-table
		v-model:query-form="table.search.form"
		v-model:show-search="table.search.visible"
		v-model:maximized="table.refs.cardMaximized"
		v-model:columns="table.columnConfig.list"
		:search-fields="table.search.fields"
		:data="table.table.data"
		:loading="table.table.loading"
		:table-loading="table.table.loading"
		:pagination="table.table.pagination"
		:page-sizes="pageSizes"
		@get-data-list="table.query.getDataList()"
		@reset-query="table.query.reset()"
		@selection-change="table.selection.onChange"
		@sort-change="table.query.onSortChange"
		@page-change="table.query.onPageChange"
		@size-change="table.query.onSizeChange"
		@reset-columns="table.columnConfig.reset()"
	>
		<!-- 工具栏左侧：默认新增+批量删除 -->
		<template #toolbar-left>
			<slot name="toolbar-left" :table="table" :form="form">
				<el-space size="default">
					<el-button type="primary" :icon="Plus" @click="form.dialog.open('add')">新增</el-button>
					<el-button type="danger" :loading="table.delete.loading" :icon="Delete" @click="table.delete.execute()">删除</el-button>
				</el-space>
			</slot>
		</template>
		<!-- 操作列：默认修改+行删除 -->
		<template #operation="{ row }">
			<slot name="action-column" :row="row" :table="table" :form="form">
				<el-button type="primary" link :icon="Edit" @click="form.dialog.open('update', row[primaryKey])">修改</el-button>
				<el-button type="primary" link :icon="Delete" @click="table.delete.execute(row)">删除</el-button>
			</slot>
		</template>
	</query-table>

	<!-- 弹窗表单 -->
	<crud-dialog
		v-model:visible="form.dialog.visible"
		:mode="form.dialog.mode"
		:form-schema="formSchema"
		:form-data="form.form.data"
		:rules="form.form.rules"
		:label-width="form.form.labelWidth"
		:subject="subject"
		:submit-loading="form.submit.loading"
		@submit="form.submit.execute()"
	>
		<!-- 透传所有插槽（用户用字段 prop 名作为插槽名，如 #columnType） -->
		<template v-for="(_, name) in dialogSlots" :key="name" #[name]="slotData">
			<slot :name="name" v-bind="slotData ?? {}" />
		</template>
	</crud-dialog>
</template>

<script setup lang="ts">
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import type { FormItemRule } from 'element-plus'
import type { SearchFieldSchema, FormFieldSchema, TableColumnSchema } from '@/types/schema'
import type { Key, PageVO } from '@/types'
import { useCrudTable } from '@/hooks/use-crud-table'
import { useCrudForm } from '@/hooks/use-crud-form'
import QueryTable from './QueryTable.vue'
import CrudDialog from './CrudDialog.vue'

defineOptions({
	name: 'CrudPage'
})

// ====== Props ======
const props = withDefaults(
	defineProps<{
		searchSchema?: SearchFieldSchema[]
		formSchema: FormFieldSchema[]
		columns: TableColumnSchema[]
		dataListApi: (query: any) => Promise<PageVO<any>>
		deleteApi?: (ids: Key[]) => Promise<void>
		submitApi: (data: any) => Promise<any>
		detailApi?: (id: Key) => Promise<any>
		initQueryForm: () => any
		initFormData: () => any
		pageSize?: number
		pageSizes?: number[]
		subject?: string
		deleteNameKey?: string
		primaryKey?: string
		labelWidth?: string
		rules?: Record<string, FormItemRule[]>
		tableKey?: string
		submitAfter?: (data: any) => void
	}>(),
	{
		searchSchema: () => [],
		pageSize: 10,
		pageSizes: () => [10, 20, 50, 100, 200],
		subject: '数据',
		primaryKey: 'id',
		labelWidth: '100px'
	}
)

// ====== Hooks ======
const table = useCrudTable({
	dataListApi: props.dataListApi,
	deleteApi: props.deleteApi,
	initQueryForm: props.initQueryForm,
	columns: props.columns,
	searchSchema: props.searchSchema,
	pageSize: props.pageSize,
	pageSizes: props.pageSizes,
	primaryKey: props.primaryKey,
	subject: props.subject,
	deleteNameKey: props.deleteNameKey,
	tableKey: props.tableKey
})

const form = useCrudForm({
	submitApi: props.submitApi,
	detailApi: props.detailApi,
	initFormData: props.initFormData,
	subject: props.subject,
	rules: props.rules,
	labelWidth: props.labelWidth,
	submitAfter: props.submitAfter ?? (() => table.query.getDataList())
})

// ====== 插槽透传计算 ======
const reservedSlots = new Set(['toolbar-left', 'action-column'])
const dialogSlots = computed(() => {
	const slots: Record<string, any> = {}
	for (const name in useSlots()) {
		if (!reservedSlots.has(name)) {
			slots[name] = true
		}
	}
	return slots
})
</script>
