<template>
	<icon-button
		:svg-icon="isFullscreen ? 'icon-fullscreen-exit' : 'icon-fullscreen'"
		:content="isFullscreen ? '退出全屏' : '开启全屏'"
		size="18px"
		@click="toggle()"
	></icon-button>
</template>
<script setup lang="ts">
import IconButton from '@/components/icon-button/index.vue'
import { useFullscreen } from '@vueuse/core'

defineOptions({
	name: 'FullScreen'
})

const { isFullscreen, toggle } = useFullscreen()
</script>
