<template>
	<div style="display: inline-flex; align-items: center; gap: 5px">
		<svg-icon v-if="enableIcon && icon" :icon="icon" :size="iconSize" :is-pointer="isPointer"></svg-icon>
		<text-tooltip :title="title" :max-width="maxWidth" :placement="placement"></text-tooltip>
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
	iconSize: {
		type: String,
		required: false,
		default: '1em'
	},
	isPointer: {
		type: Boolean,
		default: false
	},
	title: {
		type: String,
		required: true
	},
	maxWidth: {
		type: String,
		required: false,
		default: '100px'
	},
	placement: {
		type: String,
		required: false,
		default: 'top'
	}
})
</script>
