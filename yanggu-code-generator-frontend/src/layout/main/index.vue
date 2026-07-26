<template>
	<el-scrollbar ref="layoutScrollbarRef" class="layout-scrollbar">
		<div class="layout-card">
			<!-- 开启了全局缓存 -->
			<template v-if="systemSettingStore.other.isOpenPageCache">
				<router-view v-slot="{ Component }">
					<transition name="slide" mode="out-in">
						<!-- 内置|业务菜单和非缓存的iframe页面 -->
						<keep-alive :include="cacheStore.cacheList" :exclude="['RouterRedirect']">
							<component
								:is="Component"
								v-if="route.meta.type === 1 || (route.meta.type === 3 && !route.meta.cache)"
								:key="route.fullPath"
							></component>
						</keep-alive>
					</transition>
				</router-view>
				<!-- 缓存的iframe页面 -->
				<iframe-container></iframe-container>
			</template>
			<!-- 关闭全局缓存，只用component -->
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
import { useRoute } from 'vue-router'
import { useAppStore, useCacheStore, useSystemSettingStore } from '@/store'
import { watch } from 'vue'
import { storeToRefs } from 'pinia'
import SvgIcon from '@/components/svg-icon/index.vue'

defineOptions({
	name: 'LayoutMain'
})

const route = useRoute()
const appStore = useAppStore()
const cacheStore = useCacheStore()
const systemSettingStore = useSystemSettingStore()
const { layoutScrollbarRef } = storeToRefs(appStore)

// 路由切换时滚动到顶部
watch(
	() => route.fullPath,
	() => {
		// 如果全局页面缓存关闭 或者 页面本身不缓存，则滚动到顶部
		if (!systemSettingStore.other.isOpenPageCache || !route.meta.cache) {
			layoutScrollbarRef.value?.scrollTo({ top: 0, left: 0 })
		}
	}
)
</script>
