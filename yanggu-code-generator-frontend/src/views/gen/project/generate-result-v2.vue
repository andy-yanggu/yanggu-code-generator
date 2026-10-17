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

	<!-- 加载中 -->
	<div v-else-if="status === 'loading'" class="result-container">
		<div class="loading-content">
			<el-icon class="loading-icon is-loading" :size="48" color="var(--el-color-primary)"><Loading></Loading></el-icon>
			<h3>代码生成中</h3>
			<p>请稍等，正在为您生成代码...</p>
		</div>
	</div>
</template>

<script lang="ts" setup>
import { Loading, Back } from '@element-plus/icons-vue'
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

// 返回修改
const emitBack = () => {
	emit('back')
}

// 初始化方法
const init = (dataForm: any, generatorType: number) => {
	generatorTypeRef.value = generatorType
	status.value = INIT_STATUS
	remainSeconds.value = AUTO_CLOSE_SECONDS
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
	submitHandler!(dataForm)
		.then(() => {
			status.value = 'success'
			startCountDown()
		})
		.catch(() => (status.value = 'error'))
		.finally(() => (finish.value = true))
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

.loading-content {
	text-align: center;

	.loading-icon {
		margin-bottom: 20px;
	}

	h3 {
		margin: 0 0 8px;
		font-size: 18px;
		font-weight: 500;
		color: var(--el-text-color-primary);
	}

	p {
		margin: 0;
		font-size: 14px;
		color: var(--el-text-color-secondary);
	}
}
</style>
