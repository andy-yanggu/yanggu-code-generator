<template>
	<div>
		<!-- 搜索表单（由独立的 searchFields 定义驱动） -->
		<search-form
			:fields="search.fields"
			:form="search.form"
			:visible="search.visible"
			:loading="table.loading"
			@search="query.getDataList()"
			@reset="query.reset()"
		/>

		<el-card ref="table.cardRef" class="layout-table-card" :class="{ 'is-maximized': table.cardMaximized }" shadow="hover">
			<!-- 表格工具栏 -->
			<template #header>
				<table-tool-bar
					v-model:show-search="search.visible"
					v-model:query-loading="table.loading"
					v-model:maximized="table.cardMaximized"
					v-model:columns="columnConfig.list"
					use-column-settings
					@get-data-list="query.getDataList()"
				>
					<template #left>
						<el-space size="default">
							<el-button type="primary" :icon="Plus" @click="formInitHandle()">新增</el-button>
							<el-button type="danger" :loading="deleteOps.loading" :icon="Delete" @click="deleteOps.execute()">删除</el-button>
						</el-space>
					</template>
				</table-tool-bar>
			</template>
			<config-table
				:columns="columnConfig.visible"
				:data="table.data"
				:loading="table.loading"
				:pagination="table.pagination"
				border
				row-key="id"
				max-height="60vh"
				@selection-change="selection.onChange"
				@sort-change="query.onSortChange"
				@page-change="query.onPageChange"
				@size-change="query.onSizeChange"
			>
				<template #operation="{ row }">
					<el-button type="primary" link :icon="Edit" @click="formInitHandle(row.id)">修改</el-button>
					<el-button type="primary" link :icon="Delete" @click="deleteOps.execute(row)">删除</el-button>
				</template>
			</config-table>
		</el-card>
		<!-- 弹窗表单 -->
		<field-type-form ref="formRef" @refresh-data-list="query.getDataList()"></field-type-form>
	</div>
</template>

<script setup lang="ts">
import { useInitForm } from '@/hooks'
import { useTableV2 } from '@/hooks/use-table-v2'
import FieldTypeForm from '@/views/gen/field-type/form.vue'
import { ATTR_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import { genFieldTypeApi } from '@/api'
import type { ColumnConfig } from '@/types'
import type { SearchFieldConfig } from '@/types/hooks/search-field'
import { GenFieldTypeEntity, GenFieldTypeQuery } from '@/types'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import ConfigTable from '@/components/table/config-table/index.vue'
import TableToolBar from '@/components/table/tool-bar/index.vue'
import SearchForm from '@/components/table/search-form/index.vue'

defineOptions({
	name: 'GenFieldType'
})

// 表格列配置（纯表格，不含搜索逻辑）
const columnsDef: ColumnConfig[] = [
	{ key: 'selection', type: 'selection', label: '', width: 50, align: 'center', fixed: 'left', disabled: true },
	{ key: 'index', type: 'index', label: '序号', width: 60, align: 'center', disabled: true },
	{ key: 'columnType', prop: 'columnType', label: '字段类型', align: 'center' },
	{ key: 'attrType', prop: 'attrType', label: '属性类型', align: 'center', formatter: getLabel(ATTR_TYPES) },
	{ key: 'packageName', prop: 'packageName', label: '包名' },
	{ key: 'createTime', prop: 'createTime', label: '创建时间', minWidth: 120, sortable: 'custom' as const, align: 'center' },
	{ key: 'updateTime', prop: 'updateTime', label: '修改时间', minWidth: 120, sortable: 'custom' as const, align: 'center' },
	{ key: 'operation', label: '操作', fixed: 'right' as const, width: 150, align: 'center', disabled: true }
]

// 搜索字段定义（独立于表格列，顺序/字段可自由控制）
const searchFieldsDef: SearchFieldConfig[] = [
	{ prop: 'columnType', label: '字段类型' },
	{ prop: 'attrType', label: '属性类型', component: 'select', options: ATTR_TYPES.items, filterable: true }
]

const {
	search,
	query,
	table,
	selection,
	delete: deleteOps,
	columnConfig
} = useTableV2<GenFieldTypeEntity, GenFieldTypeQuery>({
	tableKey: 'gen-field-type',
	tableSubject: '字段类型',
	deleteNameKey: 'columnType',
	dataListApi: genFieldTypeApi.entityPage,
	deleteListApi: genFieldTypeApi.deleteList,
	initQueryFormData: () => ({
		columnType: '',
		attrType: ''
	}),
	columns: columnsDef,
	searchFields: searchFieldsDef
})

const { formRef, formInitHandle } = useInitForm()
</script>
