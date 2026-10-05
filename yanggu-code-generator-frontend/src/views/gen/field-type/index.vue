<template>
	<crud-page :table-hook="tableHook" :form-hook="formHook"></crud-page>
</template>

<script setup lang="ts">
import { genFieldTypeApi } from '@/api'
import { ATTR_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import type { CrudField } from '@/types/schema'
import type { GenFieldTypeEntity, GenFieldTypeQuery, UseCrudFormOptions, UseCrudTableOptions } from '@/types'
import { useCrud } from '@/hooks/use-crud'
import CrudPage from '@/components/crud/CrudPage.vue'

defineOptions({
	name: 'GenFieldType'
})

// 统一字段声明
const fields: CrudField[] = [
	{
		key: 'columnType',
		label: '字段类型',
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			tooltip: '数据库中的字段类型；字段类型具有唯一性，不能重复',
			ruleList: [{ required: true, message: '字段类型不能为空', trigger: 'blur' }]
		}
	},
	{
		key: 'attrType',
		label: '属性类型',
		component: 'select',
		options: ATTR_TYPES.items,
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			ruleList: [{ required: true, message: '属性类型不能为空', trigger: 'blur' }]
		},
		table: { formatter: getLabel(ATTR_TYPES) }
	},
	{ key: 'packageName', label: '包名', inForm: true, inTable: true }
]

// 初始化表单数据
const initFormData = (): GenFieldTypeEntity => ({ id: null, columnType: '', attrType: '', packageName: '' })

// 表单配置
const formConfig = reactive({
	initFormData,
	submitApi: genFieldTypeApi.submit,
	detailApi: genFieldTypeApi.detail
} as UseCrudFormOptions)

// 初始化查询表单数据
const initQueryForm = (): GenFieldTypeQuery => ({ columnType: '', attrType: '' })

// 表格配置
const tableConfig = reactive({
	initQueryForm,
	dataListApi: genFieldTypeApi.entityPage,
	deleteApi: genFieldTypeApi.deleteList,
	deleteNameKey: 'columnType',
	tableKey: 'gen-field-type-test'
} as UseCrudTableOptions)

// 统一 hook：fields 自动派生 formSchema / searchSchema / tableColumns
const { tableHook, formHook } = useCrud<GenFieldTypeEntity, GenFieldTypeQuery, GenFieldTypeEntity>({
	primaryKey: 'id',
	subject: '字段类型',
	fields,
	tableConfig,
	formConfig
})
</script>
