<template>
	<!-- 缓存池化（v-show控制展示和隐藏） -->
	<transition-group name="slide" tag="div">
		<div v-for="item in cacheStore.iframeCacheList" v-show="route.fullPath === item.fullPath" :key="`iframe-${item.fullPath}`">
			<router-view v-slot="{ Component }">
				<component :is="Component" :iframe-src="item.src" :cache="true"></component>
			</router-view>
		</div>
	</transition-group>
</template>

<script setup lang="ts">
import { useCacheStore, useMenuPreferenceStore } from '@/store'

const cacheStore = useCacheStore()
const menuPreferenceStore = useMenuPreferenceStore()
const route = useRoute()

defineOptions({
	name: 'IframeContainer'
})

// 监听路由切换
watch(
	() => [route.fullPath, route.meta.type, route.meta.cache],
	() => {
		// 只处理 iframe 类型的路由 (type === 3)
		const externalUrl = route.meta.externalUrl as string
		const serverCache = (route.meta.cache as boolean) || false
		// 使用用户偏好覆盖的 cache 值
		const effectiveCache = menuPreferenceStore.getEffectiveCache(route.path, serverCache)
		if (route.meta.type !== 3 || !effectiveCache || !externalUrl) {
			return
		}

		const newRouteName = route.name as string

		// 缓存模式：加入池子
		cacheStore.addIframeCache({ name: newRouteName, src: externalUrl, fullPath: route.fullPath })
	},
	{ immediate: true }
)
</script>
