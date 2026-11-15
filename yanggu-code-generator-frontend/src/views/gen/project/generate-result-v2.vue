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
	     图形取自有图标库的 icon-loading：12 根辐条带透明度梯度，配 steps(12) 步进旋转亮头暗尾逐格追逐；
	     不用 icons-vue 的 Loading：它是 8 根等长放射短棒、45° 旋转对称，配匀速动画几乎看不出在转 -->
	<div v-else-if="status === 'loading'" class="result-container">
		<el-result title="代码生成中" :sub-title="loadingSubTitle">
			<template #icon>
				<!-- icon-loading-spin 全局类提供步进旋转，icon-primary 命中 el-result 的语义色规则（与相邻两个分支的取色路径一致）；
				     尺寸显式传 el-result 的图标变量，与相邻分支内置图标同源；symbol 的 viewBox 已按 EP 语义图标的图形占比收边，同框即同视觉大小 -->
				<svg-icon icon="icon-loading" size="var(--el-result-icon-font-size)" class="icon-loading-spin icon-primary"></svg-icon>
			</template>
		</el-result>
	</div>
</template>

<script lang="ts" setup>
import { Back } from '@element-plus/icons-vue'
import SvgIcon from '@/components/svg-icon/index.vue'
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
</style>
