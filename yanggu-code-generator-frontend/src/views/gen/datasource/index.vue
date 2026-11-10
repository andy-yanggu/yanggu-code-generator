<template>
	<crud-page :table-hook="tableHook" :form-hook="formHook">
		<!-- 操作列：测试 + 修改 + 复制 + 删除（2×2 网格） -->
		<template #table-action="{ row, tableHook: t, dialog }">
			<el-row>
				<el-col :span="12">
					<!-- 单并发：压屏显形前的 300ms 内遮罩还不在，靠 disabled 兼平这段窗口 -->
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

	<!-- 测试结果卡片：成功与失败共用同一套版式，只换皮肤与字段显隐。
	     状态区交给 el-result（图标色即语义色），事实项交给 el-descriptions。
	     必须放在 crud-page 外面：CrudPage 没有渲染默认插槽，写在里面会被静默丢掉。 -->
	<el-dialog v-model="testResultVisible" title="数据源连接测试" width="480">
		<el-result
			v-if="testResult"
			class="test-result"
			:icon="TEST_KIND_ICON[testResult.kind]"
			:title="TEST_KIND_TITLE[testResult.kind]"
			:sub-title="testSubTitle"
		>
			<template #extra>
				<el-descriptions :column="1" border>
					<el-descriptions-item label="连接名称">{{ testResult.connName }}</el-descriptions-item>
					<el-descriptions-item label="数据库类型">{{ testResult.dbTypeLabel }}</el-descriptions-item>
					<!-- 数据库名只有连上才有，失败时不渲染而不留空行 -->
					<el-descriptions-item v-if="testResult.kind === 'success'" label="数据库">{{ testResult.databaseName }}</el-descriptions-item>
					<el-descriptions-item label="耗时">{{ formattedElapsed }} ms</el-descriptions-item>
					<el-descriptions-item label="测试时间">{{ testResult.testedAt }}</el-descriptions-item>
					<!-- 详细信息：仅失败渲染，异常原文要能滚动能复制 -->
					<el-descriptions-item v-if="testResult.kind !== 'success'" label="详细信息">
						<div class="test-result__detail-head">
							<el-button link type="primary" :icon="copied ? Check : DocumentCopy" @click="copyErrorMessageHandle">
								{{ copied ? '已复制' : '复制' }}
							</el-button>
						</div>
						<pre class="test-result__detail">{{ testResult.message }}</pre>
					</el-descriptions-item>
				</el-descriptions>
			</template>
		</el-result>
		<template #footer>
			<el-button :icon="Close" @click="testResultVisible = false">关闭</el-button>
			<el-button type="primary" :icon="RefreshRight" :disabled="testingId !== null" @click="retryTestHandle">重新测试</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { genDatasourceApi } from '@/api'
import { DB_TYPES } from '@/constant/enum'
import { getDictTag } from '@/utils/enum'
import { copyToClipboard } from '@/utils/tool'
import type { CrudField } from '@/types/schema'
import type { GenDatasourceEntity, GenDatasourceQuery, Key, UseCrudFormOptions, UseCrudTableOptions } from '@/types'
import { useCrud } from '@/hooks/use-crud'
import CrudPage from '@/components/crud/CrudPage.vue'
import dayjs from 'dayjs'
import { Check, Close, Connection, CopyDocument, Delete, DocumentCopy, Edit, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

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

// ====== 数据源连接测试 ======
//
// 交互：单并发锁 + 全屏压屏 + 结果卡片。
// 压屏延迟 300ms 才显形（快结果不闪），8s 兜底超时（绝不永久压屏），
// 结果一律落在卡片上、不再走顶部 ElMessage：异常原文的长度超出 message 的单行承载力。

// 压屏延迟显形时长
const SHOW_LOADING_DELAY_MS = 300
// 前端兜底超时：后端 DriverManager.setLoginTimeout 为 6s，留 2s 余量
const TEST_TIMEOUT_MS = 8000

// 测试结果种类：成功 / 连不上（后端 result=false）/ 等待超时 / 请求异常
type TestKind = 'success' | 'fail' | 'timeout' | 'error'

interface DatasourceTestResult {
	kind: TestKind
	connName: string
	dbTypeLabel: string
	databaseName?: string
	message?: string
	elapsed: number
	testedAt: string
}

// el-result 的图标类型（与组件内部字面量一致）：success 绿 / warning 橙 / error 红 / info 灰
type ResultIconType = 'success' | 'warning' | 'error' | 'info'

// 颜色、图标、文字三重编码，不单靠颜色区分
// “不通”而不写“失败”：失败暗示系统坏了，不通才准确指向网络与参数
const TEST_KIND_TITLE: Record<TestKind, string> = {
	success: '连接成功',
	fail: '连接不通',
	timeout: '等待超时',
	error: '测试请求失败'
}

const TEST_KIND_ICON: Record<TestKind, ResultIconType> = {
	success: 'success',
	fail: 'error',
	timeout: 'warning',
	error: 'warning'
}

// sub-title 只放行动建议；异常原文走详细信息区，两边不重复
const TEST_KIND_HINT: Record<TestKind, string> = {
	success: '',
	fail: '请检查连接地址与端口、账号密码，以及数据库服务是否已启动。',
	timeout: '请检查网络连通性与端口是否放开，可重新测试确认是否为临时抖动。',
	error: '请确认后端服务是否正常运行，或当前登录状态是否仍然有效。'
}

// testingId 一个变量兼两职：非空即单并发锁，同时标记哪一行的按钮在转
const testingId = ref<Key | null>(null)
// 暂存发起测试的整行：结果回来后才有连接名与数据库类型可展示
const testRow = ref<GenDatasourceEntity | null>(null)
const testResultVisible = ref(false)
const testResult = ref<DatasourceTestResult | null>(null)
const copied = ref(false)

// 当前这组定时器与遮罩的收口函数：组件卸载时调一次，防止全屏遮罩泄漏在页面上
let teardownTest: (() => void) | null = null

const formattedElapsed = computed(() => (testResult.value?.elapsed ?? 0).toLocaleString())

// sub-title：成功播报连到了哪个库，失败给下一步动作
const testSubTitle = computed(() => {
	const result = testResult.value
	if (!result) {
		return ''
	}
	return result.kind === 'success' ? `已连接到数据库 ${result.databaseName || '-'}` : TEST_KIND_HINT[result.kind]
})

// 拦截器 reject 出来的可能是字符串（业务错误）、Error（网络错误）或 axios error
const extractErrorMessage = (error: any): string => {
	if (!error) {
		return ''
	}
	if (typeof error === 'string') {
		return error
	}
	// api 里关了全局错误提示，拿不到拦截器的中文映射，这里补最常见的一个
	if (error.message === 'Network Error') {
		return '网络连接失败，请检查网络设置'
	}
	return error.message || error.msg || ''
}

const datasourceTestHandle = (row: GenDatasourceEntity) => {
	// 单并发：同一时刻只允许一个数据源在测
	if (testingId.value !== null) {
		return
	}

	testingId.value = row.id
	testRow.value = row

	const startTime = Date.now()
	let loadingInstance: { close: () => void } | null = null
	let showTimer = 0
	let timeoutTimer = 0
	// 超时、响应、卸载三方共用一个收口，先到者生效，迟到者丢弃
	let settled = false

	const release = () => {
		window.clearTimeout(showTimer)
		window.clearTimeout(timeoutTimer)
		loadingInstance?.close()
		loadingInstance = null
		testingId.value = null
	}
	teardownTest = release

	const finish = (partial: Pick<DatasourceTestResult, 'kind'> & Partial<DatasourceTestResult>) => {
		if (settled) {
			return
		}
		settled = true
		release()
		testResult.value = {
			connName: row.connName,
			// 后端存的是 dbType 字面量，回显成人读标签
			dbTypeLabel: DB_TYPES.label(row.dbType) ?? row.dbType,
			// 耗时与时间戳后端不返，前端自己计
			elapsed: Date.now() - startTime,
			testedAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
			...partial
		}
		copied.value = false
		// 先让压屏收起，下一帧再开卡片，避免遮罩与弹窗的层级、过渡打架
		nextTick(() => {
			testResultVisible.value = true
		})
	}

	showTimer = window.setTimeout(() => {
		loadingInstance = ElLoading.service({
			fullscreen: true,
			text: `正在测试数据源【${row.connName}】…`,
			background: 'rgba(255, 255, 255, 0.8)'
		})
	}, SHOW_LOADING_DELAY_MS)

	timeoutTimer = window.setTimeout(() => {
		finish({
			kind: 'timeout',
			// 只记技术事实，排查建议由 sub-title 给出，两边不重复
			message: `本次测试已等待超过 ${TEST_TIMEOUT_MS / 1000} 秒，前端主动结束等待；后端连接可能仍在进行中。`
		})
	}, TEST_TIMEOUT_MS)

	genDatasourceApi
		.test(row.id as number)
		.then(data => {
			if (data.result) {
				finish({ kind: 'success', databaseName: data.databaseName })
			} else {
				// 后端兼容了异常，但 errorMessage 仍可能为空，不留空白卡片
				finish({ kind: 'fail', message: data.errorMessage?.trim() || '连接失败，未返回具体原因，请检查连接参数。' })
			}
		})
		.catch(error => {
			// 原实现的 catch 只静默清状态，用户什么都看不到；noErrorMessage 后更要靠卡片播报
			// 拿不到具体原因时也要给结论，不留空白详细信息区
			finish({
				kind: 'error',
				message: extractErrorMessage(error) || '接口未返回错误详情，请结合后端日志定位。'
			})
		})
}

// 重新测试：先收起卡片再走一遍完整流程，避免两个弹层叠在一起
const retryTestHandle = () => {
	const row = testRow.value
	if (!row) {
		return
	}
	testResultVisible.value = false
	nextTick(() => datasourceTestHandle(row))
}

const copyErrorMessageHandle = async () => {
	const message = testResult.value?.message
	if (!message) {
		return
	}
	try {
		await copyToClipboard(message)
		copied.value = true
		window.setTimeout(() => (copied.value = false), 2000)
	} catch {
		// copyToClipboard 内部已提示（含不支持时的降级提醒），不再重复播报
	}
}

onBeforeUnmount(() => teardownTest?.())
</script>

<style lang="scss" scoped>
.test-result {
	// el-result 的留白与 64px 图标是给整页空状态用的，塞进 480 弹窗要收紧
	padding: 8px 0 0;
	--el-result-icon-font-size: 48px;
	--el-result-title-margin-top: 12px;
	--el-result-subtitle-margin-top: 6px;
	--el-result-extra-margin-top: 18px;

	// 提示语偏长，限定行宽让它折行而不是顶到弹窗两边
	:deep(.el-result__subtitle) {
		max-width: 360px;
	}

	// el-result 是 align-items: center 的纵列，插槽内容被包在 __extra 里会按内容收缩；
	// 宽度要给到 __extra，给到内层 descriptions 只是“收缩后宽度的 100%”，表格依旧胖不起来
	:deep(.el-result__extra) {
		width: 100%;
	}

	// 标签列定宽，值列吃满剩余宽度，长异常原文才不会把左列顶得忽宽忽窄
	:deep(.el-descriptions__label) {
		width: 96px;
	}

	&__detail-head {
		display: flex;
		justify-content: flex-end;
		margin-bottom: 4px;
	}

	// 异常原文：等宽字体 + 可滚动 + 可换行，保证长堆栈不撑破卡片也不被截断
	&__detail {
		max-height: 168px;
		margin: 0;
		padding: 10px 12px;
		overflow: auto;
		font-family: Consolas, Monaco, monospace;
		font-size: 12px;
		line-height: 1.6;
		color: var(--el-text-color-regular);
		text-align: left;
		white-space: pre-wrap;
		word-break: break-all;
		background: var(--el-fill-color-light);
		border-radius: 4px;
	}
}
</style>
