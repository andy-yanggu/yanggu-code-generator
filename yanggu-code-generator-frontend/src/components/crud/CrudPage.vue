<template>
	<query-table
		v-model:query-form="tableHooks.search.form"
		v-model:show-search="tableHooks.search.visible"
		v-model:maximized="tableHooks.refs.cardMaximized"
		v-model:columns="tableHooks.columnConfig.list"
		:search-fields="tableHooks.search.fields"
		:data="tableHooks.table.data"
		:loading="tableHooks.table.loading"
		:table-loading="tableHooks.table.loading"
		:pagination="tableHooks.table.pagination"
		:page-sizes="pageSizes"
		@get-data-list="tableHooks.query.getDataList()"
		@reset-query="tableHooks.query.reset()"
		@selection-change="tableHooks.selection.onChange"
		@sort-change="tableHooks.query.onSortChange"
		@page-change="tableHooks.query.onPageChange"
		@size-change="tableHooks.query.onSizeChange"
		@reset-columns="tableHooks.columnConfig.reset()"
	>
		<!-- 工具栏左侧：默认新增+批量删除 -->
		<template #toolbar-left>
			<slot name="toolbar-left" :table="tableHooks" :form="formHooks">
				<el-space size="default">
					<el-button type="primary" :icon="Plus" @click="formHooks.dialog.open('add')">新增</el-button>
					<el-button type="danger" :loading="tableHooks.delete.loading" :icon="Delete" @click="tableHooks.delete.execute()">删除</el-button>
				</el-space>
			</slot>
		</template>
		<!-- 操作列：默认修改+行删除 -->
		<template #operation="{ row }">
			<slot name="action-column" :row="row" :table="tableHooks" :form="formHooks">
				<el-button type="primary" link :icon="Edit" @click="formHooks.dialog.open('update', row[primaryKey])">修改</el-button>
				<el-button type="primary" link :icon="Delete" @click="tableHooks.delete.execute(row)">删除</el-button>
			</slot>
		</template>
	</query-table>

	<!-- 弹窗表单 -->
	<crud-dialog
		v-model:visible="formHooks.dialog.visible"
		v-model:form-data="formHooks.form.data"
		:mode="formHooks.dialog.mode"
		:form-schema="formSchema"
		:form-ref="formHooks.form.formRef"
		:rules="formHooks.form.rules"
		:label-width="formHooks.form.labelWidth"
		:subject="subject"
		:submit-loading="formHooks.submit.loading"
		@submit="formHooks.submit.execute()"
	>
		<!-- 透传所有插槽（用户用字段 prop 名作为插槽名，如 #columnType） -->
		<template v-for="(_, name) in dialogSlots" :key="name" #[name]="slotData">
			<slot :name="name" v-bind="slotData ?? {}"></slot>
		</template>
	</crud-dialog>
</template>

<script setup lang="ts">
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import type { FormItemRule } from 'element-plus'
import type { FormFieldSchema, SearchFieldSchema, TableColumnSchema } from '@/types/schema'
import type { Key, PageVO } from '@/types'
import { useCrudTable } from '@/hooks/use-crud-table'
import { useCrudForm } from '@/hooks/use-crud-form'
import QueryTable from './QueryTable.vue'
import CrudDialog from './CrudDialog.vue'

defineOptions({
	name: 'CrudPage',
	Generic: ['VO', 'Query', 'DTO']
})

// ====== Props ======
interface CrudPageProps<VO = any, Query = any, DTO = any> {
	searchSchema?: SearchFieldSchema[]
	formSchema: FormFieldSchema[]
	columns: TableColumnSchema[]
	dataListApi: (query: Query) => Promise<PageVO<VO>>
	deleteApi?: (ids: Key[]) => Promise<void>
	submitApi: (data: DTO) => Promise<any>
	detailApi?: (id: Key) => Promise<VO>
	initQueryForm: () => Query
	initFormData: () => DTO
	pageSize?: number
	pageSizes?: number[]
	subject?: string
	deleteNameKey?: string
	primaryKey?: string
	labelWidth?: string
	rules?: Record<string, FormItemRule[]>
	tableKey?: string
	submitAfter?: (data: any) => void
}

const props = withDefaults(defineProps<CrudPageProps>(), {
	searchSchema: () => [],
	pageSize: 10,
	pageSizes: () => [10, 20, 50, 100, 200],
	subject: '数据',
	primaryKey: 'id',
	labelWidth: '100px'
})

// ====== Hooks ======
const tableHooks = useCrudTable({
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

const formHooks = useCrudForm({
	submitApi: props.submitApi,
	detailApi: props.detailApi,
	initFormData: props.initFormData,
	subject: props.subject,
	rules: props.rules,
	labelWidth: props.labelWidth,
	submitAfter: props.submitAfter ?? (() => tableHooks.query.getDataList())
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
