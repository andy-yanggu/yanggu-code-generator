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
						<router-view v-slot="{ Component, route }">
							<transition name="slide" mode="out-in">
								<keep-alive v-if="route.meta.type === 1" :include="appStore.cacheList" :exclude="['Redirect']">
									<component :is="Component" :key="route.fullPath"></component>
								</keep-alive>
							</transition>
						</router-view>
						<iframe-container></iframe-container>
					</div>
				</el-scrollbar>
			</el-main>
		</el-container>
	</el-container>
</template>

<script setup lang="ts">
import sidebar from '@/layout/sidebar/index.vue'
import navbar from '@/layout/navbar/index.vue'
import { useAppStore } from '@/store/app-store'
import IframeContainer from '@/layout/components/iframe-container.vue'

const appStore = useAppStore()
</script>

<style scoped></style>
