<template>
	<div class="icon-text-tooltip">
		<svg-icon v-if="enableIcon && icon" :icon="icon" :size="iconSize" :is-pointer="isPointer"></svg-icon>
		<text-tooltip :title="title" :max-width="maxWidth" :placement="placement" :text-class="textClass"></text-tooltip>
	</div>
</template>
<script setup lang="ts">
import TextTooltip from '@/components/text-tooltip/index.vue'
import SvgIcon from '@/components/svg-icon/index.vue'

defineOptions({
	name: 'IconTextTooltip'
})

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
		required: false,
		default: null
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
	},
	textClass: {
		type: String,
		required: false,
		default: ''
	}
})
</script>
<style scoped>
.icon-text-tooltip {
	display: flex;
	align-items: center;
	gap: 5px;
}
</style>
