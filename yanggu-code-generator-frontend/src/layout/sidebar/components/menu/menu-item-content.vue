<template>
	<div class="menu-item">
		<svg-icon v-if="icon" :icon="icon" is-pointer></svg-icon>
		<el-tooltip :content="title" :disabled="!isTitleOverflow" placement="top">
			<el-text ref="titleRef" class="menu-title" :style="{ maxWidth: systemSettingStore.menuExpandWidth - 100 + 'px' }">
				{{ title }}
			</el-text>
		</el-tooltip>
	</div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import { useSystemSettingStore } from '@/store/system-setting-store'

defineProps({
	icon: {
		type: String,
		default: ''
	},
	title: {
		type: String,
		required: true
	}
})

const titleRef = ref()
const isTitleOverflow = ref(false)
const systemSettingStore = useSystemSettingStore()

// 检查文本是否溢出
onMounted(() => {
	nextTick(() => {
		const el = titleRef.value?.$el
		if (!el) {
			return
		}

		const check = () => {
			isTitleOverflow.value = el.scrollWidth > el.offsetWidth
		}
		check()

		const observer = new ResizeObserver(check)
		observer.observe(el)
	})
})
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
