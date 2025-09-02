<template>
	<!-- 缓存池化（v-show控制展示和隐藏） -->
	<transition-group name="slide" tag="div">
		<div v-for="item in appStore.iframeCacheList" v-show="route.fullPath === item.fullPath" :key="item.fullPath">
			<iframe-page v-model:loading="item.loading" :iframe-src="item.src" :cache="true"></iframe-page>
		</div>
	</transition-group>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store/app-store'
import { useRoute } from 'vue-router'
import iframePage from '@/layout/components/iframe-page.vue'
import { watch } from 'vue'

const appStore = useAppStore()
const route = useRoute()

defineOptions({
	name: 'IframeContainer'
})

// 监听路由切换
watch(
	() => [route.fullPath, route.meta.type, route.meta.cache],
	([newFullPath, newType, newCache]) => {
		// 只处理 iframe 类型的路由 (type === 3)
		if (newType !== 3 || newCache === false) {
			return
		}

		const newRouteName = route.name as string
		const externalUrl = route.meta.externalUrl as string

		// 缓存模式：加入池子
		appStore.addIframeCache({ name: newRouteName, src: externalUrl, fullPath: newFullPath as string, loading: true })
	},
	{ immediate: true }
)
</script>

<style scoped></style>
