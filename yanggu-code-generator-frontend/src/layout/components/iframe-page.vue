<template>
	<div v-loading="loading" class="iframe-container">
		<iframe :src="iframeSrc" width="100%" height="100%" style="min-height: calc(100vh - 84px)" allow="accelerometer" @load="handleLoad"></iframe>
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const loading = ref(true) // 初始 loading 状态

defineProps({
	iframeSrc: {
		type: String,
		required: true
	}
})

defineOptions({
	name: 'IframeComponent'
})

import { getCurrentInstance, onMounted, onActivated, onDeactivated, onUnmounted } from 'vue'

const inst = getCurrentInstance()
console.log('uid:', inst?.uid) // 不同 key 会看到不同 uid

onMounted(() => console.log('mounted uid=', inst?.uid))
onActivated(() => {
	console.log('activated uid=', inst?.uid)
	const currentUrl = route.meta.externalUrl as string
	console.log('currentUrl:', currentUrl)
})
onDeactivated(() => console.log('deactivated uid=', inst?.uid))
onUnmounted(() => console.log('unmounted uid=', inst?.uid))

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
