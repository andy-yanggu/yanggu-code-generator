<template>
	<div class="svg-icon">
		<svg :class="`${className}`" :style="`color:${color};`" aria-hidden="true">
			<use :xlink:href="iconName"></use>
		</svg>
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
	icon: {
		type: String,
		required: true
	},
	color: {
		type: String,
		default: ''
	},
	className: {
		type: String,
		default: ''
	}
})

// https://www.iconfont.cn 图标库需使用前缀 icon- 才能匹配
const iconName = computed(() => `#${props.icon}`)
</script>

<style scoped>
.svg-icon {
	display: inline-block;
	width: 14px;
	height: 16px;
}

.svg-icon svg {
	display: block;
	width: 100%;
	height: 100%;
	fill: currentColor;
}
</style>
