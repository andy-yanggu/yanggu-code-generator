<template>
	<crud-page
		:search-schema="searchSchema"
		:form-schema="formSchema"
		:columns="columns"
		:data-list-api="genFieldTypeApi.entityPage"
		:delete-api="genFieldTypeApi.deleteList"
		:submit-api="genFieldTypeApi.submit"
		:detail-api="genFieldTypeApi.detail"
		:init-query-form="() => ({ columnType: '', attrType: '' })"
		:init-form-data="() => ({ id: null, columnType: '', attrType: '', packageName: '' })"
		subject="字段类型"
		delete-name-key="columnType"
		table-key="gen-field-type-test"
	/>
</template>

<script setup lang="ts">
import { genFieldTypeApi } from '@/api'
import { ATTR_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import type { SearchFieldSchema, FormFieldSchema, TableColumnSchema } from '@/types/schema'
import CrudPage from '@/components/crud/CrudPage.vue'

defineOptions({
	name: 'GenFieldTypeTest'
})

const searchSchema: SearchFieldSchema[] = [
	{ prop: 'columnType', label: '字段类型' },
	{ prop: 'attrType', label: '属性类型', component: 'select', options: ATTR_TYPES.items, filterable: true }
]

const formSchema: FormFieldSchema[] = [
	{ prop: 'columnType', label: '字段类型' },
	{ prop: 'attrType', label: '属性类型', component: 'select', options: ATTR_TYPES.items, filterable: true },
	{ prop: 'packageName', label: '包名' }
]

const columns: TableColumnSchema[] = [
	{ key: 'selection', type: 'selection', label: '', width: 50, align: 'center', fixed: 'left', disabled: true },
	{ key: 'index', type: 'index', label: '序号', width: 60, align: 'center', disabled: true },
	{ key: 'columnType', prop: 'columnType', label: '字段类型', align: 'center' },
	{ key: 'attrType', prop: 'attrType', label: '属性类型', align: 'center', formatter: getLabel(ATTR_TYPES) },
	{ key: 'packageName', prop: 'packageName', label: '包名' },
	{ key: 'createTime', prop: 'createTime', label: '创建时间', minWidth: 120, sortable: 'custom' as const, align: 'center' },
	{ key: 'updateTime', prop: 'updateTime', label: '修改时间', minWidth: 120, sortable: 'custom' as const, align: 'center' },
	{ key: 'operation', label: '操作', fixed: 'right' as const, width: 150, align: 'center', disabled: true }
]
</script>
