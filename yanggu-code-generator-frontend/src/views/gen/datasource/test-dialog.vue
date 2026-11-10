<template>
	<!-- 数据源连接测试面板：pending 与四种结果态共用同一套骨架，只换 el-result 的图标位与文案，详情区按状态增减行。
	     点击「测试」即开弹窗并进入 pending，不再走全屏压屏：等待感由图标位旋转 + 「已等待」实时秒承载。
	     pending 期间不拦关闭路径：遮罩 / ESC / ✕ / footer 随时可关，弹窗关不掉请求；请求回来后只写状态不再弹开，
	     单并发锁由 finish 收口释放，所以提前关窗不会把表格按钮卡在「测试中」。 -->
	<el-dialog v-model="visible" title="数据源连接测试" width="600">
		<el-result
			v-if="testResult"
			class="test-result"
			:icon="TEST_KIND_ICON[testResult.kind]"
			:title="TEST_KIND_TITLE[testResult.kind]"
			:sub-title="testSubTitle"
		>
			<!-- pending 顶掉内置图标：is-loading 命中 EP 内置的 rotating 动画，icon-primary 命中 el-result 的语义色规则，两处都不写死样式。
			     条件具名插槽在 v-if 为假时不会注册，结果态才走 :icon 属性的内置图标
			     图形复用 el-loading 的开口圆弧几何（r=20 + dasharray 90,150），stroke 走 currentColor 才吃得到 icon-primary 的语义色。
			     不用 EP 的 Loading 图标：它是 8 根等长辐条、严格 8 重对称，转一圈视觉重复 8 次且没有方向感，撑不起 48px 的大图标位 -->
			<template v-if="isPending" #icon>
				<el-icon class="test-result__icon is-loading icon-primary">
					<svg viewBox="25 25 50 50" aria-hidden="true">
						<circle
							cx="50"
							cy="50"
							r="20"
							fill="none"
							stroke="currentColor"
							stroke-width="4"
							stroke-linecap="round"
							stroke-dasharray="90,150"
						></circle>
					</svg>
				</el-icon>
			</template>

			<template #extra>
				<el-descriptions :column="1" border>
					<!-- 静态事实：发起瞬间即已知，三态恒在 -->
					<el-descriptions-item label="连接名称">{{ testResult.connName }}</el-descriptions-item>
					<el-descriptions-item label="数据库类型">{{ testResult.dbTypeLabel }}</el-descriptions-item>
					<el-descriptions-item label="描述">{{ formatDesc(testResult.datasourceDesc) }}</el-descriptions-item>

					<!-- 本次测试产生的事实：库名只有连上才有，耗时与完成时间只有落定才有 -->
					<el-descriptions-item v-if="isSuccess" label="数据库">{{ testResult.databaseName }}</el-descriptions-item>
					<el-descriptions-item label="发起时间">{{ testResult.startedAt }}</el-descriptions-item>
					<el-descriptions-item v-if="isPending" label="已等待">{{ waitingText }}</el-descriptions-item>
					<el-descriptions-item v-if="isSettled" label="耗时">{{ formattedElapsed }} ms</el-descriptions-item>
					<el-descriptions-item v-if="isSettled" label="完成时间">{{ testResult.testedAt }}</el-descriptions-item>
					<!-- 详细信息：仅未连上渲染，异常原文要能滚动能复制 -->
					<el-descriptions-item v-if="isFailed" label="详细信息">
						<div class="test-result__detail-head">
							<el-button link type="primary" :disabled="copied" :icon="copied ? Check : DocumentCopy" @click="copyErrorMessageHandle">
								{{ copied ? '已复制' : '复制' }}
							</el-button>
						</div>
						<pre class="test-result__detail">{{ testResult.message }}</pre>
					</el-descriptions-item>
				</el-descriptions>
			</template>
		</el-result>

		<template #footer>
			<el-button :icon="Close" @click="visible = false">关闭</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { genDatasourceApi } from '@/api'
import { DB_TYPES } from '@/constant/enum'
import { copyToClipboard, isNotBlank } from '@/utils/tool'
import type { GenDatasourceEntity, Key } from '@/types'
import dayjs from 'dayjs'
import { Check, Close, DocumentCopy } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenDatasourceTestDialog'
})

// 前端兜底超时：后端 DriverManager.setLoginTimeout 为 6s，留 2s 余量
const TEST_TIMEOUT_MS = 8000
// 「已等待」的刷新节奏：200ms 足够看出计数活着，不必跟到帧级
const WAITING_TICK_MS = 200
// 「已复制」态的回退时长
const COPY_RESET_DELAY_MS = 2000
// 空值占位符
const PLACEHOLDER = '-'

// 面板状态：pending 为加载中，其余为四种结果态
type TestKind = 'pending' | 'success' | 'fail' | 'timeout' | 'error'
// 结果态：已落定，才有耗时与原因可填
type ResultKind = Exclude<TestKind, 'pending'>

interface DatasourceTestResult {
	kind: TestKind
	connName: string
	dbTypeLabel: string
	datasourceDesc: string
	startedAt: string
	// 以下字段仅落定后存在
	databaseName?: string
	message?: string
	elapsed?: number
	testedAt?: string
}

// 落定时在发起快照上补齐的字段
type TestPatch = Pick<DatasourceTestResult, 'kind'> & Partial<Pick<DatasourceTestResult, 'databaseName' | 'message' | 'elapsed' | 'testedAt'>>

// el-result 的图标类型（与组件内部字面量一致）：primary 主色 / success 绿 / warning 橙 / error 红 / info 灰
type ResultIconType = 'primary' | 'success' | 'warning' | 'error' | 'info'

// 颜色、图标、文字三重编码，不单靠颜色区分
// 「不通」而不写「失败」：失败暗示系统坏了，不通才准确指向网络与参数
const TEST_KIND_TITLE: Record<TestKind, string> = {
	pending: '正在测试连接',
	success: '连接成功',
	fail: '连接不通',
	timeout: '等待超时',
	error: '测试请求失败'
}

const TEST_KIND_ICON: Record<TestKind, ResultIconType> = {
	// pending 走 #icon 插槽自绘旋转图标，此项只为类型完备
	pending: 'primary',
	success: 'success',
	fail: 'error',
	timeout: 'warning',
	error: 'warning'
}

// sub-title 只放行动建议；异常原文走详细信息区，两边不重复
const TEST_KIND_HINT: Record<ResultKind, string> = {
	success: '',
	fail: '请检查连接地址与端口、账号密码，以及数据库服务是否已启动。',
	timeout: '请检查网络连通性与端口是否放开，可重新测试确认是否为临时抖动。',
	error: '请确认后端服务是否正常运行，或当前登录状态是否仍然有效。'
}

// 弹窗显隐由组件自持：父层只负责发起测试与读单并发锁
const visible = ref(false)
// 单并发锁交给父层：表格行的「测试」按钮要靠它做 loading 与 disabled
const testingId = defineModel<Key>('testingId', { default: null })
// 暂存发起测试的整行：重新测试才有原行可复用
const testRow = ref<GenDatasourceEntity | null>(null)
const testResult = ref<DatasourceTestResult | null>(null)
const copied = ref(false)
// pending 期间的已等待毫秒数
const waitingMs = ref(0)

// 三个定时器全部走 VueUse：内部用 onScopeDispose 注册清理，组件销毁自动摘掉，不必再手写 onBeforeUnmount
// useIntervalFn 返回 Pausable（pause/resume），useTimeoutFn 返回 Stoppable（start/stop），两者控制方法名不同，别名按用途取
// 已等待按 Date.now() 差值取，不累加 tick，避免后台标签页节流把计时拖慢
const { pause: pauseWaiting, resume: resumeWaiting } = useIntervalFn(() => (waitingMs.value = Date.now() - startTime), WAITING_TICK_MS, {
	immediate: false
})

// 兜底超时与后端响应抢跑，谁先到谁生效，闸门是下面的 settled
// 回调前向引用 finish：只在 setup 跑完后的运行期才会被执行，不存在暂时性死区
const { start: startTimeout, stop: stopTimeout } = useTimeoutFn(
	() => finish({ kind: 'timeout', message: `本次测试已等待超过 ${TEST_TIMEOUT_MS / 1000} 秒，前端主动结束等待；后端连接可能仍在进行中。` }),
	TEST_TIMEOUT_MS,
	{ immediate: false }
)

// 「已复制」态自动回退；start 内部会先清掉上一笔，重复点击是重新计时而不是留下一个到点就把状态打回去的旧定时器
const { start: startCopyReset, stop: stopCopyReset } = useTimeoutFn(() => (copied.value = false), COPY_RESET_DELAY_MS, { immediate: false })

// 超时与响应两方共用一个收口，先到者生效，迟到者丢弃
let settled = false
// 发起时刻：耗时按它算，不用完成时刻反推
let startTime = 0

const isTesting = computed(() => testingId.value !== null)
const kind = computed(() => testResult.value?.kind)
const isPending = computed(() => kind.value === 'pending')
const isSuccess = computed(() => kind.value === 'success')
const isSettled = computed(() => !!kind.value && kind.value !== 'pending')
const isFailed = computed(() => kind.value === 'fail' || kind.value === 'timeout' || kind.value === 'error')

const formattedElapsed = computed(() => (testResult.value?.elapsed ?? 0).toLocaleString())

// 已等待只报到十分位：要的是「请求还活着」的观感，精确值由「耗时」承载
const waitingText = computed(() => `${(waitingMs.value / 1000).toFixed(1)} 秒`)

// 描述为空（含纯空格）属于「该填没填」，留占位符不隐行；不适用才隐行（如未连上时的数据库）
const formatDesc = (desc: string): string => (isNotBlank(desc) ? desc : PLACEHOLDER)

// sub-title：pending 播报正在拨哪个数据源，成功播报连到了哪个库，落定失败给下一步动作
const testSubTitle = computed(() => {
	const result = testResult.value
	if (!result) {
		return ''
	}
	if (result.kind === 'pending') {
		return `正在连接数据源【${result.connName}】，最长等待 ${TEST_TIMEOUT_MS / 1000} 秒`
	}
	if (result.kind === 'success') {
		return `已连接到数据库 ${result.databaseName || PLACEHOLDER}`
	}
	return TEST_KIND_HINT[result.kind]
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

const stopTimers = () => {
	pauseWaiting()
	stopTimeout()
	stopCopyReset()
}

// 收口：清定时器 + 放开单并发锁
const release = () => {
	stopTimers()
	testingId.value = null
}

// 落定：在发起快照上补齐结果字段，静态行不重建、弹窗不重开
const finish = (patch: TestPatch) => {
	if (settled || !testResult.value) {
		return
	}
	settled = true
	release()
	testResult.value = {
		...testResult.value,
		...patch,
		// 耗时与完成时间戳后端不返，前端自己记
		elapsed: Date.now() - startTime,
		testedAt: dayjs().format('YYYY-MM-DD HH:mm:ss')
	}
	copied.value = false
}

// 供父层通过 ref 调用：开弹窗、写 pending 快照、发起请求
const start = (row: GenDatasourceEntity) => {
	// 单并发：同一时刻只允许一个数据源在测；父层按钮的 disabled 已挡住误点，这里兜底
	if (isTesting.value) {
		return
	}

	testingId.value = row.id
	testRow.value = row
	startTime = Date.now()
	settled = false
	copied.value = false
	waitingMs.value = 0

	// 发起即写快照：详情区的静态行在 pending 就有内容可渲染，不等结果回来才建结构
	testResult.value = {
		kind: 'pending',
		connName: row.connName,
		// 后端存的是 dbType 字面量，回显成人读标签
		dbTypeLabel: DB_TYPES.label(row.dbType) ?? row.dbType,
		datasourceDesc: row.datasourceDesc,
		startedAt: dayjs(startTime).format('YYYY-MM-DD HH:mm:ss')
	}
	visible.value = true

	resumeWaiting()
	startTimeout()

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
			// api 配了 noErrorMessage，拦截器不播报；拿不到具体原因时也要给结论，不留空白详细信息区
			finish({
				kind: 'error',
				message: extractErrorMessage(error) || '接口未返回错误详情，请结合后端日志定位。'
			})
		})
}

const copyErrorMessageHandle = async () => {
	const message = testResult.value?.message
	if (!message) {
		return
	}
	try {
		await copyToClipboard(message)
		copied.value = true
		startCopyReset()
	} catch {
		// copyToClipboard 内部已提示（含不支持时的降级提醒），不再重复播报
	}
}

// 卸载时的定时器清理由 VueUse 的 onScopeDispose 负责；锁与结果都随组件一起消失，不必再写状态
defineExpose({
	start
})
</script>
<style lang="scss" scoped>
// 落定才出现的异常原文（v-if isFailed）是新挂载元素，同一帧详情表会长高约 156px；
// 给它一次淡入加轻微上浮，让这段内容看起来是被“填上”的而不是砰地出现（只缓解观感，不消除高差）
@keyframes test-detail-in {
	from {
		opacity: 0;
		transform: translateY(4px);
	}

	to {
		opacity: 1;
		transform: none;
	}
}

@keyframes test-icon-in {
	from {
		opacity: 0;
		transform: scale(0.55);
	}

	to {
		opacity: 1;
		transform: scale(1);
	}
}

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

	// 值列里的长 token（长描述、长地址）必须能折断，否则会把表格顶出弹窗
	:deep(.el-descriptions__content) {
		word-break: break-word;
	}

	// 结果态图标是 EP 内置图标组件渲染的裸 svg，直接挂在 __icon 下；pending 的圆弧包在 i.el-icon 里、svg 是孙元素
	// 所以这个 > 天然只命中落定那一态，不需再加状态类；el-result 是本组件模板里的元素，带 data-v，:deep 能递到它的内部
	// 两态是两个不同 DOM 元素，Vue 卸旧挂新、中间没有任何过渡；给新图标一次淡入放大，把“形状硬切”的注意力接住
	// transform-origin 实测为 24px 24px（48×48 的正中），缩放不会跑偏；缓动末值略大于 1 做一点回弹
	:deep(.el-result__icon > svg) {
		animation: test-icon-in 240ms cubic-bezier(0.2, 0.9, 0.32, 1.18) both;
	}

	// pending 图标：盒子尺寸跟着 el-result 自己的图标变量走，避免 48px 写两处
	// el-icon 默认 1em 的盒子装不下被 __icon svg 强制到 48px 的图形，会把标题顶上去
	// 转速：EP 把周期写死在 .el-icon.is-loading 的 animation 简写里（2s），无 prop 与变量可调，只能覆盖
	// 只改 animation-duration，name/缓动/次数仍由 EP 提供；选择器带上 is-loading 是为了高一级权重，避免与 EP 同权重靠样式注入顺序决胜
	// 1s/圈是开口圆弧的合适区间（0.8~1.2s）：再快显得焦躁，再慢在 6 秒的等待里像卡住
	&__icon.is-loading {
		font-size: var(--el-result-icon-font-size);
		animation-duration: 1s;
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
		animation: test-detail-in 200ms ease-out both;
	}
}
</style>
