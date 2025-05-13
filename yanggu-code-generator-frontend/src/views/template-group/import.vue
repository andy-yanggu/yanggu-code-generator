<template>
	<el-button type="success" @click="handleImport">导入</el-button>
	<input ref="fileInput" type="file" style="display: none" @change="handleFileChange" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['refreshDataList'])
const fileInput = ref<HTMLInputElement | null>(null)
const apiUrl = import.meta.env.VITE_API_URL
const serverUrl = `${apiUrl}/gen/template-group/import`

// 触发文件选择
const handleImport = () => {
	if (fileInput.value) {
		fileInput.value.click()
	}
}

// 文件选择后的处理
const handleFileChange = (event: Event) => {
	const target = event.target as HTMLInputElement
	const file = target.files?.[0]
	if (!file) {
		return
	}

	// 校验文件类型和大小
	const isJson = file.type === 'application/json'
	const isLt500KB = file.size / 1024 < 500
	if (!isJson) {
		ElMessage.error('只能上传 JSON 文件!')
		return
	}
	if (!isLt500KB) {
		ElMessage.error('上传文件大小不能超过 500KB!')
		return
	}

	// 手动上传文件
	const formData = new FormData()
	formData.append('file', file)
	fetch(serverUrl, {
		method: 'POST',
		body: formData
	})
		.then(response => {
			if (response.ok && response.status === 200) {
				return response.json() // 解析响应体为 JSON
			} else {
				throw new Error('上传失败')
			}
		})
		.then(data => {
			// 判断 code 是否为 0
			if (data.code === 0) {
				ElMessage.success('模板组导入成功')
				emit('refreshDataList')
			} else {
				ElMessage.error('模板组导入失败: ' + data.msg || '未知错误')
			}
		})
		.catch(error => {
			ElMessage.error('模板组导入失败')
			console.error(error)
		})
}
</script>
