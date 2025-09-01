<template>
	<!-- 缓存池化（v-show控制展示和隐藏） -->
	<template v-if="route.meta.cache !== false">
		<div v-for="item in appStore.iframeCacheList" v-show="item.name === appStore.activeIframe" :key="item.name">
			<iframe-page :iframe-src="item.src"></iframe-page>
		</div>
	</template>
	<!-- （不缓存） -->
	<iframe-page v-else :key="route.fullPath" :iframe-src="route.meta.externalUrl as string"></iframe-page>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store/app-store'
import { watch } from 'vue'
import { useRoute } from 'vue-router'
import iframePage from '@/layout/components/iframe-page.vue'

const appStore = useAppStore()
const route = useRoute()

defineOptions({
	name: 'IframeContainer'
})

// 监听路由切换
watch(
	() => [route.name, route.meta.type, route.meta.cache],
	([newName, newType, newCache]) => {
		// 只处理 iframe 类型的路由 (type !== 1)
		if (newType === 1 || !newName || newCache === false) {
			return
		}

		const newRouteName = newName as string
		const externalUrl = route.meta.externalUrl as string
		appStore.setActiveIframe(newRouteName)
		// 缓存模式：加入池子
		appStore.addIframeCache({ name: newRouteName, src: externalUrl, path: route.path })
	},
	{ immediate: true }
)
</script>

<style scoped></style>
