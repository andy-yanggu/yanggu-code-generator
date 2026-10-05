<template>
	<crud-page :table-hook="tableHook" :form-hook="formHook">
		<!-- 操作列：测试 + 修改 + 复制 + 删除（2×2 网格） -->
		<template #table-action="{ row, tableHook: t, dialog }">
			<el-row>
				<el-col :span="12">
					<el-button type="primary" link :icon="Connection" @click="datasourceTestHandle(row.id)">测试</el-button>
				</el-col>
				<el-col :span="12">
					<el-button type="primary" link :icon="Edit" @click="dialog?.open('update', row[t.refs.primaryKey])">修改</el-button>
				</el-col>
			</el-row>
			<el-row>
				<el-col :span="12">
					<el-button type="primary" link :icon="CopyDocument" @click="dialog?.open('copy', row[t.refs.primaryKey])">复制</el-button>
				</el-col>
				<el-col :span="12">
					<el-button type="primary" link :icon="Delete" @click="t.action.delete.execute(row)">删除</el-button>
				</el-col>
			</el-row>
		</template>

		<!-- 表单字段 password：密码输入框 -->
		<template #form-password="{ model }">
			<el-input v-model="model.password" type="password" show-password clearable placeholder="请输入密码"></el-input>
		</template>
	</crud-page>
</template>

<script setup lang="ts">
import { genDatasourceApi } from '@/api'
import { DB_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import type { FormFieldSchema, SearchFieldSchema, TableColumnSchema } from '@/types/schema'
import type { GenDatasourceEntity, GenDatasourceQuery, UseCrudFormOptions, UseCrudTableOptions } from '@/types'
import { useCrud } from '@/hooks/use-crud'
import CrudPage from '@/components/crud/CrudPage.vue'
import { Connection, CopyDocument, Delete, Edit } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenDatasource'
})

// 初始化表单数据
const initFormData = (): GenDatasourceEntity => ({ id: null, dbType: '', connName: '', connUrl: '', username: '', password: '', datasourceDesc: '' })

// 表单字段
const formSchema: FormFieldSchema[] = [
	{
		key: 'connName',
		label: '连接名称',
		tooltip: '连接名称具有唯一性',
		ruleList: [{ required: true, message: '连接名称不能为空', trigger: 'blur' }]
	},
	{
		key: 'dbType',
		label: '数据库类型',
		component: 'select',
		options: DB_TYPES.items,
		filterable: true,
		ruleList: [{ required: true, message: '数据库类型不能为空', trigger: 'blur' }]
	},
	{ key: 'connUrl', label: 'URL', ruleList: [{ required: true, message: 'URL不能为空', trigger: 'blur' }] },
	{ key: 'username', label: '用户名', ruleList: [{ required: true, message: '用户名不能为空', trigger: 'blur' }] },
	{ key: 'password', label: '密码', ruleList: [{ required: true, message: '密码不能为空', trigger: 'blur' }] },
	{ key: 'datasourceDesc', label: '描述' }
]

// 表单配置
const formConfig = reactive({
	formSchema,
	submitApi: genDatasourceApi.submit,
	detailApi: genDatasourceApi.detail,
	initFormData
} as UseCrudFormOptions)

// 查询表单 schema
const searchSchema: SearchFieldSchema[] = [
	{ key: 'connName', label: '连接名称' },
	{ key: 'dbType', label: '数据库类型', component: 'select', options: DB_TYPES.items, filterable: true }
]

// 初始化查询表单数据
const initQueryForm = (): GenDatasourceQuery => ({ dbType: '', connName: '' })

// 表格字段
const tableColumns: TableColumnSchema[] = [
	{ key: 'selection', type: 'selection', label: '', width: 50, align: 'center', fixed: 'left', disabled: true },
	{ key: 'index', type: 'index', label: '序号', width: 60, align: 'center', disabled: true },
	{ key: 'connName', label: '连接名称', align: 'center', showOverflowTooltip: true },
	{ key: 'dbType', label: '数据库类型', align: 'center', formatter: getLabel(DB_TYPES) },
	{ key: 'connUrl', label: 'URL', align: 'center', showOverflowTooltip: true },
	{ key: 'datasourceDesc', label: '描述', align: 'center', showOverflowTooltip: true },
	{ key: 'createTime', label: '创建时间', minWidth: 120, sortable: 'custom' as const, align: 'center' },
	{ key: 'updateTime', label: '修改时间', minWidth: 120, sortable: 'custom' as const, align: 'center' },
	{ key: 'operation', label: '操作', fixed: 'right' as const, width: 150, align: 'center', disabled: true }
]

// 表格配置
const tableConfig = reactive({
	initQueryForm,
	searchSchema,
	tableColumns,
	dataListApi: genDatasourceApi.entityPage,
	deleteApi: genDatasourceApi.deleteList,
	deleteNameKey: 'connName'
} as UseCrudTableOptions)

// 统一 hook
const { tableHook, formHook } = useCrud<GenDatasourceEntity, GenDatasourceQuery, GenDatasourceEntity>({
	primaryKey: 'id',
	subject: '数据源',
	tableConfig,
	formConfig
})

// 复制模式：连接名称追加"_复制"，清除主键
formConfig.afterOpen = () => {
	if (formHook.dialog.mode === 'copy') {
		formHook.form.data.connName += '_复制'
		formHook.form.data.id = null
	}
}

// 测试数据源连接
const datasourceTestHandle = (id: number) => {
	genDatasourceApi.test(id).then(data => {
		const { result, errorMessage, databaseName } = data
		if (result) {
			ElMessage.success(`测试成功，数据库为：${databaseName}`)
		} else {
			ElMessage.error(`测试失败，异常信息：${errorMessage}`)
		}
	})
}
</script>
