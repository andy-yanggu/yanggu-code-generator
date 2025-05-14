<template>
	<el-upload class="upload-demo" :action="serverUrl" :limit="1" :show-file-list="false" :on-success="handlerSuccess">
		<el-button type="primary">导入</el-button>
	</el-upload>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['refreshDataList'])
const serverUrl = ref('')
const apiUrl = import.meta.env.VITE_API_URL
serverUrl.value = `${apiUrl}/templateGroup/import`

const handlerSuccess = () => {
	ElMessage.success({
		message: '导入成功',
		duration: 1000,
		onClose: () => {
			emit('refreshDataList')
		}
	})
}
</script>
