<template>
	<el-container class="layout-container">
		<el-aside class="layout-sidebar" :style="{ width: appStore.isCollapse ? '60px' : '210px' }">
			<sidebar></sidebar>
		</el-aside>
		<el-container direction="vertical">
			<el-header class="layout-header">
				<navbar></navbar>
			</el-header>
			<el-main class="layout-main">
				<el-scrollbar class="layout-scrollbar">
					<div class="layout-card">
						<!-- 开启了全局缓存 -->
						<template v-if="systemSettingStore.isOpenPageCache">
							<router-view v-slot="{ Component }">
								<transition name="slide" mode="out-in">
									<!-- 内置|业务菜单和非缓存的iframe页面	-->
									<keep-alive :include="appStore.cacheList" :exclude="['Redirect']">
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
			</el-main>
		</el-container>
	</el-container>
</template>

<script setup lang="ts">
import sidebar from '@/layout/sidebar/index.vue'
import navbar from '@/layout/navbar/index.vue'
import IframeContainer from '@/layout/components/iframe-container.vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/app-store'
import { useSystemSettingStore } from '@/store/system-setting-store'

const route = useRoute()
const appStore = useAppStore()
const systemSettingStore = useSystemSettingStore()
</script>
