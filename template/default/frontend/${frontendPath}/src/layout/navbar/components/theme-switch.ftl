<template>
	<el-tooltip :content="appStore.isDark ? '日间模式' : '夜间模式'" placement="bottom">
		<el-switch
			v-model="appStore.isDark"
			size="default"
			:active-action-icon="Moon"
			:inactive-action-icon="Sunny"
			inline-prompt
			@change="appStore.toggleDark"
		></el-switch>
	</el-tooltip>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store'
import { Moon, Sunny } from '@element-plus/icons-vue'

defineOptions({
	name: 'ThemeSwitch'
})

const appStore = useAppStore()
</script>
