<template>
	<div class="menu-item">
		<svg-icon v-if="icon" :icon="icon" is-pointer></svg-icon>
		<el-tooltip :content="title" :disabled="!isOverflow" placement="top">
			<el-text ref="textRef" class="menu-title" :style="{ maxWidth: menuTitleMaxWidth }">
				{{ title }}
			</el-text>
		</el-tooltip>
	</div>
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import { useSystemSettingStore } from '@/store'
import { useTextOverflow } from '@/hooks'

defineOptions({
	name: 'MenuItemContent'
})

const props = defineProps({
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

// 变成响应式数据
const { title: titleRef } = toRefs(props)

const { textRef, isOverflow } = useTextOverflow(titleRef, menuTitleMaxWidth)
</script>

<style scoped>
.menu-item {
	display: flex;
	align-items: center; /* 图标+文字垂直居中 */
}
.menu-title {
	display: inline-block;
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
	vertical-align: middle;
}
</style>
