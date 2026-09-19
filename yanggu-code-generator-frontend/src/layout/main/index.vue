<template>
	<el-scrollbar ref="layoutScrollbarRef" class="layout-scrollbar">
		<div class="layout-card">
			<!-- 开启了全局缓存：keep-alive 缓存组件 + iframe 池化缓存 -->
			<template v-if="systemSettingStore.other.isOpenPageCache">
				<router-view v-slot="{ Component }">
					<transition name="slide" mode="out-in">
						<!--
							keep-alive 负责缓存业务菜单组件
							非缓存的 iframe 也经过此处渲染，但不在 cacheList 中，不会被缓存
							缓存的 iframe 由下方 iframe-container 池化管理
						-->
						<keep-alive :include="cacheStore.cacheList" :exclude="['RouterRedirect']">
							<component :is="Component" v-if="shouldCachePage" :key="route.fullPath"></component>
						</keep-alive>
					</transition>
				</router-view>
				<iframe-container></iframe-container>
			</template>
			<!-- 关闭全局缓存：纯 component 渲染，无缓存 -->
			<template v-else>
				<router-view v-slot="{ Component }">
					<transition name="slide" mode="out-in">
						<component :is="Component" :key="route.fullPath"></component>
					</transition>
				</router-view>
			</template>
		</div>
		<!-- 返回顶部组件 -->
		<el-backtop target=".layout-scrollbar .el-scrollbar__wrap" :bottom="50" :right="50">
			<el-tooltip content="回到顶部" placement="top">
				<el-link underline="never">
					<svg-icon icon="icon-vertical-align-top" size="20px"></svg-icon>
				</el-link>
			</el-tooltip>
		</el-backtop>
	</el-scrollbar>
</template>

<script setup lang="ts">
import IframeContainer from '@/layout/main/components/iframe-container.vue'
import { useCacheStore, useMenuPreferenceStore, useSystemSettingStore } from '@/store'
import { useLayout } from '@/hooks/use-layout'
import SvgIcon from '@/components/svg-icon/index.vue'
import { ROUTE_META_DEFAULTS } from '@/config/router'

defineOptions({
	name: 'LayoutMain'
})

const route = useRoute()
const cacheStore = useCacheStore()
const systemSettingStore = useSystemSettingStore()
const menuPreferenceStore = useMenuPreferenceStore()
const { layoutScrollbarRef } = useLayout()

// 判断 iframe 是否开启缓存（考虑用户偏好）
const isIframeCached = (path: string, serverCache: boolean): boolean => {
	const { cache } = menuPreferenceStore.getEffective(path, { cache: serverCache })
	return cache
}

/**
 * 当前路由页面是否应该被 keep-alive 缓存
 * - 业务菜单(type=1)：始终走 keep-alive（是否真正缓存由 cacheList 决定）
 * - 非缓存 iframe(type=3)：经过 keep-alive 但不在 cacheList 中，不会被缓存
 * - 缓存 iframe(type=3)：不走 keep-alive，由 iframe-container 池化管理
 */
const shouldCachePage = computed(() => {
	const { type, cache } = route.meta
	// 业务菜单
	if (type === 1) {
		return true
	}
	// 非缓存的 iframe（缓存的 iframe 交给 iframe-container 处理）
	if (type === 3) {
		return !isIframeCached(route.path, (cache as boolean) || ROUTE_META_DEFAULTS.cache)
	}
	return false
})

// 路由切换时滚动到顶部
watch(
	() => route.fullPath,
	() => {
		const { cache: effectiveCache } = menuPreferenceStore.getEffective(route.path, route.meta)
		// 如果全局页面缓存关闭 或者 页面本身不缓存，则滚动到顶部
		if (!systemSettingStore.other.isOpenPageCache || !effectiveCache) {
			layoutScrollbarRef.value?.scrollTo({ top: 0, left: 0 })
		}
	}
)
</script>
