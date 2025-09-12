<template>
	<el-switch
		v-model="appStore.isDark"
		size="default"
		:active-action-icon="Moon"
		:inactive-action-icon="Sunny"
		inline-prompt
		@change="appStore.toggleDark"
	/>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store/app-store'
import { Moon, Sunny } from '@element-plus/icons-vue'

defineOptions({
	name: 'ThemeSwitch'
})

const appStore = useAppStore()
</script>
