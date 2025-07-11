<template>
	<div class="navbar-container">
		<!-- 展开和折叠按钮 -->
		<el-icon :size="22" @click="store.toggleCollapse()">
			<Expand v-if="store.isCollapseRef"></Expand>
			<Fold v-else></Fold>
		</el-icon>
		<!-- 面包屑 -->
		<el-breadcrumb separator="/">
			<el-breadcrumb-item v-for="item in store.breadcrumbListRef" :key="item.path">
				{{ item.name }}
			</el-breadcrumb-item>
		</el-breadcrumb>
	</div>
	<!-- 标签栏 -->
	<tag></tag>
</template>

<script setup lang="ts">
import { Expand, Fold } from '@element-plus/icons-vue'
import Tag from '@/layout/components/navbar/components/tag.vue'
import { appStore } from '@/store'

const store = appStore()
</script>

<style scoped>
.navbar-container {
	margin-top: 10px;
	display: flex;
	align-items: center; /* 垂直居中 */
	gap: 16px; /* 按钮与面包屑之间的间距 */
}
</style>
