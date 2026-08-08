<template>
	<el-result v-if="status === 'success'" icon="success" title="生成成功" :sub-title="successSubTitle">
		<template #extra>
			<el-text type="info">{{ remainSeconds }}秒后自动关闭</el-text>
		</template>
	</el-result>

	<el-result v-else-if="status === 'error'" icon="error" title="生成失败" :sub-title="errorMessage"></el-result>

	<el-result v-else-if="status === 'loading'" icon="primary" title="生成代码生成中" sub-title="请稍等"></el-result>
</template>
<script lang="ts" setup>
import { genGeneratorApi } from '@/api'

const dialogVisible = defineModel('dialogVisible', {
	type: Boolean,
	default: false
})

const finish = defineModel('finish', {
	type: Boolean,
	default: false
})

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

const AUTO_CLOSE_SECONDS = 3
const remainSeconds = ref(AUTO_CLOSE_SECONDS)

// 进行自动关闭
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

// 初始化方法
const init = (dataForm: any, generatorType: number) => {
	generatorTypeRef.value = generatorType
	status.value = INIT_STATUS
	remainSeconds.value = AUTO_CLOSE_SECONDS
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
