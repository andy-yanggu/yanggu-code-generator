<template>
	<el-container class="layout-container">
		<el-aside class="layout-sidebar" :style="{ width: appStore.isCollapseRef ? '64px' : '200px' }">
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
							<keep-alive :include="appStore.cacheList" :exclude="['Redirect']">
								<component :is="Component" :key="route.fullPath"></component>
							</keep-alive>
						</router-view>
					</div>
				</el-scrollbar>
			</el-main>
		</el-container>
	</el-container>
</template>

<script setup lang="ts">
import sidebar from '@/layout/components/sidebar/index.vue'
import navbar from '@/layout/components/navbar/index.vue'
import { useAppStore } from '@/store/app-store'

const appStore = useAppStore()
</script>

<style scoped></style>
