<template>
	<el-upload class="upload-demo" :action="serverUrl" :limit="1">
		<el-button type="primary">导入</el-button>
	</el-upload>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

const serverUrl = ref('')
const apiUrl = import.meta.env.VITE_API_URL
serverUrl.value = `${apiUrl}/templateGroup/import`
</script>
