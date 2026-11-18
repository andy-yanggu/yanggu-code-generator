<template>
	<!-- 成功 -->
	<div v-if="status === 'success'" class="result-container">
		<el-result icon="success" title="生成成功" :sub-title="successSubTitle">
			<template #extra>
				<el-button type="primary" size="small" @click="dialogVisible = false">立即关闭</el-button>
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

	<!-- 加载中：el-result 的 icon 属性只认 5 种语义值，没有 loading 档，转圈图形只能走 #icon 插槽；
	     图形/旋转/尺寸封装在 LoadingIcon，icon-primary 取语义色（与相邻两个分支的取色路径一致） -->
	<div v-else-if="status === 'loading'" class="result-container">
		<el-result title="代码生成中" :sub-title="loadingSubTitle">
			<template #icon>
				<loading-icon class="icon-primary"></loading-icon>
			</template>
		</el-result>
	</div>
</template>

<script lang="ts" setup>
import { Back } from '@element-plus/icons-vue'
import LoadingIcon from '@/components/loading-icon/index.vue'
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
	let resultText = '代码生成成功'
	if (generatorTypeRef.value === 0) {
		resultText = '代码已经下载到浏览器'
	} else if (generatorTypeRef.value === 1) {
		resultText = '代码已经生成到本地'
	}
	return `${resultText}，${remainSeconds.value}秒后自动关闭`
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
