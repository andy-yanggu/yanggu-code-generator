<template>
	<el-tooltip :content="!appStore.isDark ? '日间模式' : '夜间模式'" effect="dark" placement="bottom">
		<svg-icon :icon="appStore.isDark ? 'icon-sun' : 'icon-moon'" size="18px" is-pointer @click="appStore.toggleDark()"></svg-icon>
	</el-tooltip>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store/app-store'
import SvgIcon from '@/components/svg-icon/index.vue'

const appStore = useAppStore()
</script>
