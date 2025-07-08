<template>
	<el-container class="layout-container">
		<el-aside class="layout-sidebar">
			<sidebar></sidebar>
		</el-aside>
		<el-container>
			<el-header class="layout-header">
				<navbar></navbar>
			</el-header>
			<el-main class="layout-main">
				<el-scrollbar class="layout-scrollbar">
					<div class="layout-card">
						<router-view v-slot="{ Component, route }">
							<component :is="Component" :key="route.fullPath"></component>
						</router-view>
					</div>
				</el-scrollbar>
			</el-main>
		</el-container>
	</el-container>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import sidebar from '@/layout/components/sidebar/index.vue'
import navbar from '@/layout/components/navbar/index.vue'
</script>
