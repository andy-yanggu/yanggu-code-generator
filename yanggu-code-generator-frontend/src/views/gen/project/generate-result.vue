<template>
	<el-result v-if="status === 'success'" icon="success" title="生成成功" :sub-title="successSubTitle"></el-result>

	<el-result v-else-if="status === 'error'" icon="error" title="生成失败" sub-title="请检查相关配置"></el-result>

	<el-result v-else icon="primary" title="正在生成代码" sub-title="请稍等"></el-result>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue'
import { genGeneratorApi } from '@/api'
import { ElMessage } from 'element-plus'

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

const status = ref('')
const generatorTypeRef = ref(0)

const init = (dataForm: any, generatorType: number) => {
	generatorTypeRef.value = generatorType
	let submitHandler: (dataForm: any) => Promise<any> = _ => Promise.resolve()
	if (generatorType === 0) {
		submitHandler = genGeneratorApi.projectDownloadZip
	} else if (generatorType === 1) {
		submitHandler = genGeneratorApi.projectDownloadLocal
	} else if (generatorType === 2) {
		status.value = 'error'
		ElMessage.warning(`生成方式异常：${generatorType}`)
		return
	}
	submitHandler!(dataForm)
		.then(() => (status.value = 'success'))
		.catch(() => (status.value = 'error'))
		.finally(() => (finish.value = true))
}

defineExpose({
	init
})
</script>
