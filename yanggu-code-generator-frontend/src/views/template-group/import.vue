<template>
	<el-upload class="upload-demo" :limit="1" :show-file-list="false" :http-request="handleManualUpload">
		<el-button type="success">导入</el-button>
	</el-upload>
</template>

<script lang="ts" setup>
import { importTemplateGroupApi } from '@/api/template-group'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['refreshDataList'])

// 自定义上传处理
const handleManualUpload = (options: any) => {
	const { file } = options
	const formData = new FormData()
	formData.append('file', file)

	importTemplateGroupApi(formData).then(() => {
		ElMessage.success('模板组导入成功')
		emit('refreshDataList')
	})
}
</script>
