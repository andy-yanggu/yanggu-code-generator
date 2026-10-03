<template>
	<crud-page :table-hook="tableHook" :form-hook="formHook"></crud-page>
</template>

<script setup lang="ts">
import { genFieldTypeApi } from '@/api'
import { ATTR_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import type { FormFieldSchema, SearchFieldSchema, TableColumnSchema } from '@/types/schema'
import type { GenFieldTypeEntity, GenFieldTypeQuery, UseCrudFormOptions, UseCrudTableOptions } from '@/types'
import { useCrud } from '@/hooks/use-crud'
import CrudPage from '@/components/crud/CrudPage.vue'

defineOptions({
	name: 'GenFieldTypeTest'
})

// 初始化表单数据
const initFormData = (): GenFieldTypeEntity => ({
	id: null,
	columnType: '',
	attrType: '',
	packageName: ''
})

// 表单字段
const formSchema: FormFieldSchema[] = [
	{
		prop: 'columnType',
		label: '字段类型',
		ruleList: [{ required: true, message: '字段类型不能为空', trigger: 'blur' }]
	},
	{
		prop: 'attrType',
		label: '属性类型',
		component: 'select',
		options: ATTR_TYPES.items,
		filterable: true,
		ruleList: [{ required: true, message: '属性类型不能为空', trigger: 'change' }]
	},
	{
		prop: 'packageName',
		label: '包名'
	}
]

// 表单配置
const formConfig = reactive({
	formSchema,
	submitApi: genFieldTypeApi.submit,
	detailApi: genFieldTypeApi.detail,
	initFormData
} as UseCrudFormOptions)

// 查询表单 schema
const searchSchema: SearchFieldSchema[] = [
	{
		prop: 'columnType',
		label: '字段类型'
	},
	{
		prop: 'attrType',
		label: '属性类型',
		component: 'select',
		options: ATTR_TYPES.items,
		filterable: true
	}
]

// 初始化查询表单数据
const initQueryForm = (): GenFieldTypeQuery => ({
	columnType: '',
	attrType: ''
})

// 表格字段
const tableColumns: TableColumnSchema[] = [
	{
		key: 'selection',
		type: 'selection',
		label: '',
		width: 50,
		align: 'center',
		fixed: 'left',
		disabled: true
	},
	{
		key: 'index',
		type: 'index',
		label: '序号',
		width: 60,
		align: 'center',
		disabled: true
	},
	{
		key: 'columnType',
		prop: 'columnType',
		label: '字段类型',
		align: 'center'
	},
	{
		key: 'attrType',
		prop: 'attrType',
		label: '属性类型',
		align: 'center',
		formatter: getLabel(ATTR_TYPES)
	},
	{
		key: 'packageName',
		prop: 'packageName',
		label: '包名'
	},
	{
		key: 'createTime',
		prop: 'createTime',
		label: '创建时间',
		minWidth: 120,
		sortable: 'custom' as const,
		align: 'center'
	},
	{
		key: 'updateTime',
		prop: 'updateTime',
		label: '修改时间',
		minWidth: 120,
		sortable: 'custom' as const,
		align: 'center'
	},
	{
		key: 'operation',
		label: '操作',
		fixed: 'right' as const,
		width: 150,
		align: 'center',
		disabled: true
	}
]

// 表格配置
const tableConfig = reactive({
	initQueryForm,
	searchSchema,
	tableColumns,
	dataListApi: genFieldTypeApi.entityPage,
	deleteApi: genFieldTypeApi.deleteList,
	deleteNameKey: 'columnType',
	tableKey: 'gen-field-type-test'
} as UseCrudTableOptions)

// 统一 hook：共享 primaryKey / subject，table / form 各自独立
const { tableHook, formHook } = useCrud<GenFieldTypeEntity, GenFieldTypeQuery, GenFieldTypeEntity>({
	primaryKey: 'id',
	subject: '字段类型',
	tableConfig,
	formConfig
})
</script>
