<template>
	<div>
		<crud-page :table-hook="tableHook" :form-hook="formHook">
			<!-- 操作列：测试 + 修改 + 复制 + 删除（2×2 网格） -->
			<template #table-action="{ row, tableHook: t, dialog }">
				<el-row>
					<el-col :span="12">
						<!-- 单并发：锁由测试弹窗组件持有，父层读同一份 testingId 来转圈并挡住第二行发起 -->
						<el-button
							type="primary"
							link
							:icon="Connection"
							:loading="testingId === row.id"
							:disabled="testingId !== null && testingId !== row.id"
							@click="datasourceTestHandle(row)"
						>
							{{ testingId === row.id ? '测试中' : '测试' }}
						</el-button>
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
						<el-popconfirm
							:title="`确定删除${t.refs.subject}【${row[t.refs.deleteNameKey] || '该记录'}】吗？`"
							placement="top"
							@confirm="t.action.delete.row(row)"
							@cancel="ElMessage.info('删除已取消')"
						>
							<template #reference>
								<el-button type="danger" link :icon="Delete">删除</el-button>
							</template>
						</el-popconfirm>
					</el-col>
				</el-row>
			</template>

			<!-- 表单字段 password：密码输入框 -->
			<template #form-password="{ model }">
				<el-input v-model="model.password" type="password" show-password clearable placeholder="请输入密码"></el-input>
			</template>
		</crud-page>

		<!-- 连接测试弹窗：pending / 成功 / 未连上三态与全部计时都在组件内部，父层只发起测试并共享单并发锁 -->
		<datasource-test-dialog ref="testDialogRef" v-model:testing-id="testingId"></datasource-test-dialog>
	</div>
</template>

<script setup lang="ts">
import { genDatasourceApi } from '@/api'
import { DB_TYPES } from '@/constant/enum'
import { getDictTag } from '@/utils/enum'
import type { CrudField } from '@/types/schema'
import type { GenDatasourceEntity, GenDatasourceQuery, Key, UseCrudFormOptions, UseCrudTableOptions } from '@/types'
import { useCrud } from '@/hooks/use-crud'
import CrudPage from '@/components/crud/CrudPage.vue'
import DatasourceTestDialog from '@/views/gen/datasource/test-dialog.vue'
import { Connection, CopyDocument, Delete, Edit } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenDatasource'
})

// 初始化表单数据
const initFormData = (): GenDatasourceEntity => ({
	id: null,
	dbType: '',
	connName: '',
	connUrl: '',
	username: '',
	password: '',
	datasourceDesc: ''
})

// 统一字段声明
const fields: CrudField[] = [
	{
		key: 'connName',
		label: '连接名称',
		tooltip: '连接名称具有唯一性',
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
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
		table: { formatter: getDictTag(DB_TYPES) }
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
const initQueryData = (): GenDatasourceQuery => ({
	dbType: '',
	connName: ''
})

// 表格配置
const tableConfig = reactive({
	initQueryData,
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

// ====== 数据源连接测试 ======
//
// 交互：单并发锁 + 弹窗内三态。点击「测试」即开弹窗进入 pending，8s 兜底超时，
// 结果一律落在弹窗上、不走顶部 ElMessage：异常原文的长度超出 message 的单行承载力。
// 三态版式、计时与请求都收在 test-dialog.vue 里，本页只负责发起并共享那把锁。

// 测试弹窗实例：调 start 即开弹窗并发起测试
const testDialogRef = ref<InstanceType<typeof DatasourceTestDialog>>()
// 单并发锁：弹窗组件写入、本页读取，用来驱动行按钮的 loading 与 disabled
const testingId = ref<Key | null>(null)

const datasourceTestHandle = (row: GenDatasourceEntity) => testDialogRef.value?.start(row)
</script>
