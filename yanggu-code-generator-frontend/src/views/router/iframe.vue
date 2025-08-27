<template>
	<div v-loading="loading" class="iframe-container">
		<iframe :src="iframeSrc" width="100%" height="100%" style="min-height: calc(100vh - 84px)" allow="accelerometer" @load="handleLoad"></iframe>
	</div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const iframeSrc = ref('')
const loading = ref(true) // 初始 loading 状态

defineOptions({
	name: 'IframeComponent'
})

watch(
	() => route.meta.externalUrl,
	val => {
		iframeSrc.value = (val as string) || ''
		loading.value = true // 每次切换 URL 时重新显示 loading
	},
	{ immediate: true }
)

const handleLoad = () => {
	loading.value = false // iframe 加载完成后关闭 loading
}
</script>

<style scoped>
.iframe-container {
	height: 100%;
	overflow: hidden;
}
</style>
