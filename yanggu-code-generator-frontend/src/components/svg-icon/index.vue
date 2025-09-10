<template>
	<svg
		class="svg-icon"
		:style="{
			color,
			width: size,
			height: size,
			...(isPointer !== undefined ? { cursor: isPointer ? 'pointer' : 'default' } : {})
		}"
		aria-hidden="true"
	>
		<use :xlink:href="`#${icon}`"></use>
	</svg>
</template>
<script setup lang="ts">
defineProps({
	// 图标名称（传入时需要带上前缀 icon-）
	icon: {
		type: String,
		required: true
	},
	// 图标颜色
	color: {
		type: String,
		default: ''
	},
	// 图标大小，支持 px / em / rem
	size: {
		type: String,
		default: '1em'
	},
	// 是否为pointer
	isPointer: {
		type: Boolean,
		required: false
	}
})
</script>

<style scoped>
.svg-icon {
	display: inline-block;
	vertical-align: middle;
	fill: currentColor; /* 保持与文字颜色同步 */
}
</style>
