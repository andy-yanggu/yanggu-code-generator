<template>
	<el-breadcrumb separator="/">
		<el-breadcrumb-item v-for="item in store.breadcrumbListRef" :key="item.title">
			<template #default>
				<span style="display: inline-flex; align-items: center; gap: 5px">
					<svg-icon :icon="item.icon"></svg-icon>
					{{ item.title }}
				</span>
			</template>
		</el-breadcrumb-item>
	</el-breadcrumb>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/svg-icon/src/svg-icon.vue'
import { useAppStore } from '@/store/app-store'

const store = useAppStore()
</script>
