<template>
	<el-tooltip :content="appStore.isDark ? '切换日间' : '切换夜间'" effect="dark" placement="bottom">
		<svg-icon :icon="appStore.isDark ? 'icon-sun' : 'icon-moon'" size="18px" is-pointer @click="appStore.toggleDark()"></svg-icon>
	</el-tooltip>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store/app-store'
import SvgIcon from '@/components/svg-icon/index.vue'

defineOptions({
	name: 'ThemeSwitch'
})

const appStore = useAppStore()
</script>
