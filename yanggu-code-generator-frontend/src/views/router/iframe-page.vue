<template>
	<div v-loading="currentLoading" class="iframe-container">
		<iframe :src="iframeSrc" width="100%" height="100%" style="min-height: calc(100vh - 84px)" allow="accelerometer" @load="handleLoad"></iframe>
	</div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import NProgress from 'nprogress'
import { useSystemSettingStore } from '@/store/system-setting-store'

defineOptions({
	name: 'RouterIframePage'
})

const props = defineProps({
	// iframe地址
	iframeSrc: {
		type: String,
		required: true
	},
	// 是否缓存
	cache: {
		type: Boolean,
		required: false,
		default: false
	}
})

const currentLoading = ref(true) // 是否在加载中
const systemSettingStore = useSystemSettingStore()

// 只有在缓存模式下进行监控
if (props.cache) {
	// 如果src路径发生变化，进行loading加载，表示刷新
	watch(
		() => props.iframeSrc,
		() => {
			if (systemSettingStore.isOpenProgress) {
				NProgress.start()
			}
			currentLoading.value = true
		}
	)
}

// iframe加载完成，关闭loading
const handleLoad = () => {
	// 只有在启用缓存模式下才处理 NProgress
	if (props.cache && NProgress.isStarted() && systemSettingStore.isOpenProgress) {
		NProgress.done()
	}
	currentLoading.value = false
}
</script>

<style scoped>
.iframe-container {
	height: 100%;
	overflow: hidden;
}
</style>
