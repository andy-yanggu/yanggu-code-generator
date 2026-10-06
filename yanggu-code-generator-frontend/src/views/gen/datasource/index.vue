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
import type { CrudField } from '@/types/schema'
import type { GenDatasourceEntity, GenDatasourceQuery, UseCrudFormOptions, UseCrudTableOptions } from '@/types'
import { useCrud } from '@/hooks/use-crud'
import CrudPage from '@/components/crud/CrudPage.vue'
import { Connection, CopyDocument, Delete, Edit } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenDatasource'
})

// 初始化表单数据
const initFormData = (): GenDatasourceEntity => ({ id: null, dbType: '', connName: '', connUrl: '', username: '', password: '', datasourceDesc: '' })

// 统一字段声明
const fields: CrudField[] = [
	{
		key: 'connName',
		label: '连接名称',
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			tooltip: '连接名称具有唯一性',
			ruleList: [{ required: true, message: '连接名称不能为空', trigger: 'blur' }]
		}
	},
	{
		key: 'dbType',
		label: '数据库类型',
		component: 'select',
		options: DB_TYPES.items,
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			ruleList: [{ required: true, message: '数据库类型不能为空', trigger: 'blur' }]
		},
		table: { formatter: getLabel(DB_TYPES) }
	},
	{
		key: 'connUrl',
		label: 'URL',
		inForm: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: 'URL不能为空', trigger: 'blur' }] }
	},
	{
		key: 'username',
		label: '用户名',
		inForm: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: '用户名不能为空', trigger: 'blur' }] }
	},
	{ key: 'password', label: '密码', inForm: true, form: { ruleList: [{ required: true, message: '密码不能为空', trigger: 'blur' }] } },
	{ key: 'datasourceDesc', label: '描述', inForm: true, inTable: true }
]

// 表单配置
const formConfig = reactive({
	initFormData,
	afterDataAssign: () => {
		if (formHook.dialog.mode === 'copy') {
			formHook.form.data.connName += '_复制'
		}
	},
	submitApi: genDatasourceApi.submit,
	detailApi: genDatasourceApi.detail
} as UseCrudFormOptions)

// 初始化查询表单数据
const initQueryForm = (): GenDatasourceQuery => ({ dbType: '', connName: '' })

// 表格配置
const tableConfig = reactive({
	initQueryForm,
	dataListApi: genDatasourceApi.entityPage,
	deleteApi: genDatasourceApi.deleteList,
	deleteNameKey: 'connName'
} as UseCrudTableOptions)

// 统一 hook
const { tableHook, formHook } = useCrud<GenDatasourceEntity, GenDatasourceQuery, GenDatasourceEntity>({
	subject: '数据源',
	fields,
	formConfig,
	tableConfig
})

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
