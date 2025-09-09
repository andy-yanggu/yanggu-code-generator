<template>
	<el-tooltip :content="!isDark ? '日间模式' : '夜间模式'" effect="dark" placement="bottom">
		<svg-icon :icon="isDark ? 'icon-sun' : 'icon-moon'" size="18px" class="theme-switch-icon" @click="toggleDark()"> </svg-icon>
	</el-tooltip>
</template>

<script setup lang="ts">
import { useDark, useToggle } from '@vueuse/core'
import SvgIcon from '@/components/svg-icon/index.vue'

// 使用 useDark 管理暗黑模式状态
const isDark = useDark({
	selector: 'html',
	attribute: 'class',
	valueDark: 'dark',
	valueLight: 'light'
})

// 切换暗黑模式
const toggleDark = useToggle(isDark)
</script>

<style scoped>
.theme-switch-icon {
	cursor: pointer;
}
</style>
