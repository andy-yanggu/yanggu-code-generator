<template>
	<!-- 成功 -->
	<div v-if="status === 'success'" class="result-container">
		<el-result icon="success" title="生成成功" :sub-title="successSubTitle">
			<template #extra>
				<el-text type="info">{{ remainSeconds }}秒后自动关闭</el-text>
				<el-button type="primary" size="small" style="margin-left: 12px" @click="dialogVisible = false">立即关闭</el-button>
			</template>
		</el-result>
	</div>

	<!-- 失败 -->
	<div v-else-if="status === 'error'" class="result-container">
		<el-result icon="error" title="生成失败" :sub-title="errorMessage">
			<template #extra>
				<el-button type="primary" :icon="Back" @click="emitBack()">返回修改</el-button>
			</template>
		</el-result>
	</div>

	<!-- 加载中：el-result 的 icon 属性只认 5 种语义值，没有 loading 档，转圈图形只能走 #icon 插槽。
	     图标取 RefreshRight 而不是 icons-vue 的 Loading：后者是 8 根等长放射短棒、45° 旋转对称，配匀速动画几乎看不出在转 -->
	<div v-else-if="status === 'loading'" class="result-container">
		<el-result title="代码生成中" :sub-title="loadingSubTitle">
			<template #icon>
				<!-- is-loading 命中 EP 内置的 rotating 动画，icon-primary 命中 el-result 的语义色规则（与相邻两个分支的取色路径一致），尺寸则交给下方 CSS 变量 -->
				<el-icon class="loading-icon is-loading icon-primary">
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
		</el-result>
	</div>
</template>

<script lang="ts" setup>
import { Back } from '@element-plus/icons-vue'
import { genGeneratorApi } from '@/api'

defineOptions({
	name: 'GenProjectGenerateResultV2'
})

const dialogVisible = defineModel('dialogVisible', {
	type: Boolean,
	default: false
})

const finish = defineModel('finish', {
	type: Boolean,
	default: false
})

const emit = defineEmits<{
	(e: 'back'): void
}>()

const successSubTitle = computed(() => {
	if (generatorTypeRef.value === 0) {
		return '代码已经下载到浏览器'
	} else if (generatorTypeRef.value === 1) {
		return '代码已经生成到本地'
	}
	return '代码生成成功'
})

// 「已等待」的刷新节奏：200ms 足够看出计数活着，不必跟到帧级
const WAITING_TICK_MS = 200
const waitingMs = ref(0)

// 已等待只报到十分位：要的是「请求还活着」的观感，精确耗时后端自留
const waitingText = computed(() => `${(waitingMs.value / 1000).toFixed(1)} 秒`)

const loadingSubTitle = computed(() => `已等待 ${waitingText.value}，正在为您生成代码...`)

const errorMessage = ref('请检查相关配置')
const generatorTypeRef = ref(0)

const INIT_STATUS = 'loading'
const status = ref<'loading' | 'success' | 'error'>(INIT_STATUS)

const AUTO_CLOSE_SECONDS = 5
const remainSeconds = ref(AUTO_CLOSE_SECONDS)

// 自动关闭
watch(
	() => remainSeconds.value,
	newValue => {
		if (newValue <= 0) {
			dialogVisible.value = false
		}
	}
)

/** 倒计时（每秒） */
const { resume: startCountDown } = useIntervalFn(
	() => {
		if (remainSeconds.value > 0) {
			remainSeconds.value--
		}
	},
	1000,
	{ immediate: false }
)

/** 已等待计时（每 200ms），请求收口即停 */
const { resume: startWaiting, pause: stopWaiting } = useIntervalFn(
	() => {
		waitingMs.value += WAITING_TICK_MS
	},
	WAITING_TICK_MS,
	{ immediate: false }
)

// 返回修改
const emitBack = () => {
	emit('back')
}

// 初始化方法
const init = (dataForm: any, generatorType: number) => {
	generatorTypeRef.value = generatorType
	status.value = INIT_STATUS
	remainSeconds.value = AUTO_CLOSE_SECONDS
	waitingMs.value = 0
	stopWaiting()
	finish.value = false
	let submitHandler: (dataForm: any) => Promise<any> = _ => Promise.resolve()
	if (generatorType === 0) {
		submitHandler = genGeneratorApi.projectDownloadZip
	} else if (generatorType === 1) {
		submitHandler = genGeneratorApi.projectDownloadLocal
	} else if (generatorType === 2) {
		status.value = 'error'
		errorMessage.value = `生成方式异常：${generatorType}`
		return
	}
	startWaiting()
	submitHandler!(dataForm)
		.then(() => {
			status.value = 'success'
			startCountDown()
		})
		.catch(() => (status.value = 'error'))
		.finally(() => {
			stopWaiting()
			finish.value = true
		})
}

defineExpose({
	init
})
</script>

<style lang="scss" scoped>
.result-container {
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 40vh;
}

// loading 图标尺寸不写死：读 el-result 自己的图标变量，与相邻两个分支的内置图标同源
// 这里补字号不只是为了大小：el-icon 的盒子默认只有 1em，不撑开就装不下被 __icon svg 强制到 64px 的图形，会把标题顶上去
// 转速：EP 将周期写死在 .el-icon.is-loading 的 animation 简写里（2s），无 prop 与变量可调，只能覆盖
// 只改 animation-duration，name/缓动/次数仍由 EP 提供；选择器带上 is-loading 是为了高一级权重，避免与 EP 同权重靠样式注入顺序决胜
.loading-icon.is-loading {
	font-size: var(--el-result-icon-font-size);
	animation-duration: 1s;
}
</style>
