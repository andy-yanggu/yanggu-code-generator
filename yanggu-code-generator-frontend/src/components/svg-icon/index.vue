<template>
	<svg class="svg-icon" :style="svgStyle" aria-hidden="true">
		<use :xlink:href="`#${icon}`"></use>
	</svg>
</template>
<script setup lang="ts">
defineOptions({
	name: 'SvgIcon'
})

/**
 * isPointer 是三态语义，不是普通的布尔开关：
 * - null / 不传：完全不设置 cursor，由外层样式继承
 * - true：cursor: pointer
 * - false：cursor: default
 *
 * runtime 声明（type: Boolean）只能表达 boolean | undefined，表达不了 null，
 * 导致 icon-text-tooltip 透传 null 时报 TS2322；改用类型声明式把三态写进签名。
 */
const props = withDefaults(
	defineProps<{
		/** 图标名称（传入时需要带上前缀 icon-） */
		icon: string
		/** 图标颜色 */
		color?: string
		/** 图标大小，支持 px / em / rem */
		size?: string
		/** 是否为 pointer */
		isPointer?: boolean | null
	}>(),
	{
		color: '',
		size: '1em',
		isPointer: null
	}
)

const svgStyle = computed(() => {
	const style = {
		color: props.color,
		width: props.size,
		height: props.size,
		cursor: ''
	}
	// 三态：null / undefined（未指定）时不设 cursor，交给外层样式继承
	if (props.isPointer === null || props.isPointer === undefined) {
		return style
	} else if (props.isPointer) {
		style.cursor = 'pointer'
		return style
	} else {
		style.cursor = 'default'
		return style
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
