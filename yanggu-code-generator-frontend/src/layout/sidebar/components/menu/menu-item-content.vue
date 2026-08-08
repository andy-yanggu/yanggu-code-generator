<template>
	<div class="menu-item">
		<svg-icon v-if="icon" :icon="icon" is-pointer></svg-icon>
		<text-tooltip :title="title" :max-width="menuTitleMaxWidth" text-class="menu-title"></text-tooltip>
	</div>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/svg-icon/index.vue'
import TextTooltip from '@/components/text-tooltip/index.vue'
import { useSystemSettingStore } from '@/store'

defineOptions({
	name: 'MenuItemContent'
})

defineProps({
	icon: {
		type: String,
		required: false,
		default: ''
	},
	title: {
		type: String,
		required: true
	}
})

const systemSettingStore = useSystemSettingStore()

const menuTitleMaxWidth = computed(() => {
	return systemSettingStore.menu.menuExpandWidth - 100 + 'px'
})
</script>

<style scoped>
.menu-item {
	display: flex;
	align-items: center; /* 图标+文字垂直居中 */
}
</style>
