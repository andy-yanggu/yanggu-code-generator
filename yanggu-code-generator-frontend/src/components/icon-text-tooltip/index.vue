<template>
	<div style="display: inline-flex; align-items: center; gap: 5px">
		<svg-icon v-if="enableIcon && icon" :icon="icon" :is-pointer="isPointer"></svg-icon>
		<text-tooltip :title="$attrs.title as string" :max-width="$attrs.maxWidth as string" :placement="$attrs.placement as string"></text-tooltip>
	</div>
</template>
<script setup lang="ts">
import TextTooltip from '@/components/text-tooltip/index.vue'
import SvgIcon from '@/components/svg-icon/index.vue'

defineProps({
	enableIcon: {
		type: Boolean,
		default: true
	},
	icon: {
		type: String,
		required: true
	},
	isPointer: {
		type: Boolean,
		default: false
	}
})
</script>
