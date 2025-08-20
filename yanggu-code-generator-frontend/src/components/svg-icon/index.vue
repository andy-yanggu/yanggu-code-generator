<template>
	<div class="svg-icon">
		<svg
			:style="{
				color: color,
				width: size,
				height: size
			}"
			aria-hidden="true"
		>
			<use :xlink:href="`#${props.icon}`"></use>
		</svg>
	</div>
</template>

<script setup lang="ts">
const props = defineProps({
	// https://www.iconfont.cn 图标库需使用前缀 icon- 才能匹配
	icon: {
		type: String,
		required: true
	},
	color: {
		type: String,
		default: ''
	},
	size: {
		type: String,
		default: '1em' // 支持传入 px、em、rem
	}
})
</script>

<style scoped>
.svg-icon {
	display: inline-block;
}
.svg-icon svg {
	display: block;
	fill: currentColor;
}
</style>
