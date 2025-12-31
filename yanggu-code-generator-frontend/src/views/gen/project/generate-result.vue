<template>
	<el-result v-if="status === 'success'" icon="success" title="生成成功" :sub-title="successSubTitle">
		<template #extra>
			<el-button type="primary" :icon="Close" @click="dialogVisible = false">关闭</el-button>
		</template>
	</el-result>

	<el-result v-else-if="status === 'error'" icon="error" title="生成失败" sub-title="请检查相关配置"></el-result>

	<el-result v-else icon="primary" title="正在生成代码" sub-title="请稍等"></el-result>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue'
import { genGeneratorApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'

const dialogVisible = defineModel('dialogVisible', {
	type: Boolean,
	default: true
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
	if (generatorType === 0) {
		genGeneratorApi
			.projectDownloadZip(dataForm)
			.then(() => (status.value = 'success'))
			.catch(() => (status.value = 'error'))
	} else if (generatorType === 1) {
		genGeneratorApi
			.projectDownloadLocal(dataForm)
			.then(() => (status.value = 'success'))
			.catch(() => (status.value = 'error'))
	} else if (generatorType === 2) {
		status.value = 'error'
		ElMessage.warning(`生成方式异常：${generatorType}`)
	}
}

defineExpose({
	init
})
</script>
