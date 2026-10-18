<template>
	<div class="icon-text-tooltip">
		<svg-icon v-if="enableIcon && icon" :icon="icon" :size="iconSize" :is-pointer="isPointer"></svg-icon>
		<text-tooltip :title="title" :max-width="maxWidth" :placement="placement" :text-class="textClass"></text-tooltip>
	</div>
</template>
<script setup lang="ts">
import type { Placement } from 'element-plus'
import TextTooltip from '@/components/text-tooltip/index.vue'
import SvgIcon from '@/components/svg-icon/index.vue'

defineOptions({
	name: 'IconTextTooltip'
})

// placement 透传给 text-tooltip，必须跟着收窄成 Placement；
// 否则 string 赋给 Placement 会在本组件的模板里报 TS2322。
withDefaults(
	defineProps<{
		enableIcon?: boolean
		icon: string
		iconSize?: string
		isPointer?: boolean | null
		title: string
		maxWidth?: string
		placement?: Placement
		textClass?: string
	}>(),
	{
		enableIcon: true,
		iconSize: '1em',
		isPointer: null,
		maxWidth: '100px',
		placement: 'top',
		textClass: ''
	}
)
</script>
<style scoped>
.icon-text-tooltip {
	display: flex;
	align-items: center;
	gap: 5px;
}
</style>
