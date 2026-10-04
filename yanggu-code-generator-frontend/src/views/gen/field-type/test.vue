<template>
	<crud-page :table-hook="tableHook" :form-hook="formHook">
		<!-- ① 工具栏左侧：覆盖默认的新增+批量删除按钮 -->
		<template #table-toolbar-left="{ tableHook: t, dialog }">
			<el-button type="primary" :icon="Plus" @click="dialog?.open('add')">新增（自定义）</el-button>
			<el-button type="danger" :loading="t.action.delete.loading" :icon="Delete" @click="t.action.delete.execute()">批量删除（自定义）</el-button>
		</template>

		<!-- ② 操作列：覆盖默认的修改+行删除按钮 -->
		<template #table-action="{ row, tableHook: t, dialog }">
			<el-button type="primary" link :icon="View" @click="dialog?.open('detail', row[t.refs.primaryKey])">查看</el-button>
			<el-button type="primary" link :icon="Edit" @click="dialog?.open('update', row[t.refs.primaryKey])">编辑</el-button>
		</template>

		<!-- ③ 表格列：自定义 columnType 列渲染 -->
		<template #table-columnType="{ row }">
			<el-tag type="success">{{ row.columnType }}</el-tag>
		</template>

		<!-- ④ 表格列：自定义 attrType 列渲染 -->
		<template #table-attrType="{ row }">
			{{ row.attrType }}
		</template>

		<!-- ⑤ 搜索字段：自定义 columnType 搜索字段 -->
		<template #query-columnType="{ form }">
			<el-input v-model="form.columnType" placeholder="自定义搜索-字段类型" clearable style="width: 200px"></el-input>
		</template>

		<!-- ⑥ 表单字段：自定义 packageName 表单字段 -->
		<template #form-packageName="{ model }">
			<el-input v-model="model.packageName" placeholder="自定义表单-包名（带前缀）">
				<template #prepend>com.</template>
			</el-input>
		</template>
	</crud-page>
</template>

<script setup lang="ts">
import { Delete, Edit, Plus, View } from '@element-plus/icons-vue'
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

// 查询表单 schema（添加 key，统一用 key 作为插槽名第二段）
const searchSchema: SearchFieldSchema[] = [
	{
		key: 'columnType',
		prop: 'columnType',
		label: '字段类型'
	},
	{
		key: 'attrType',
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
