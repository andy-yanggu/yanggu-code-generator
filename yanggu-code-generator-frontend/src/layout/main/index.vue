<template>
	<el-scrollbar ref="layoutScrollbarRef" class="layout-scrollbar">
		<div class="layout-card">
			<!-- 开启了全局缓存 -->
			<template v-if="systemSettingStore.isOpenPageCache">
				<router-view v-slot="{ Component }">
					<transition name="slide" mode="out-in">
						<!-- 内置|业务菜单和非缓存的iframe页面 -->
						<keep-alive :include="appStore.cacheList" :exclude="['RouterRedirect']">
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
	</el-scrollbar>
</template>

<script setup lang="ts">
import IframeContainer from '@/layout/main/components/iframe-container.vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/app-store'
import { useSystemSettingStore } from '@/store/system-setting-store'
import { ref, watch } from 'vue'

defineOptions({
	name: 'LayoutMain'
})

const route = useRoute()
const appStore = useAppStore()
const systemSettingStore = useSystemSettingStore()
const layoutScrollbarRef = ref()

watch(
	() => route.fullPath,
	() => {
		// 滚动到顶部
		layoutScrollbarRef.value.scrollTo({ top: 0 })
	}
)
</script>
