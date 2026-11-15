<template>
	<!-- 图形取自有图标库的 icon-loading：symbol 的 viewBox 已按 EP 语义图标的图形占比收边，同框即同视觉大小。
	     颜色不封装：el-result 内由调用方挂 icon-primary 命中语义色规则，脱离 el-result 时 svg-icon 的 fill: currentColor 继承环境色 -->
	<svg-icon icon="icon-loading" :size="size" class="loading-icon"></svg-icon>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/svg-icon/index.vue'

defineOptions({
	name: 'LoadingIcon'
})

withDefaults(
	defineProps<{
		/** 图标尺寸：el-result 内吃它自己的图标变量（与内置图标同源），脱离 el-result 回退 1em 跟字号走 */
		size?: string
	}>(),
	{
		size: 'var(--el-result-icon-font-size, 1em)'
	}
)
</script>

<style lang="scss" scoped>
// icon-loading 专用步进旋转：12 根辐条带透明度梯度，steps(12) 每跳 30° 正好落到下一根辐条，
// 亮头在前暗尾在后形成追逐感；匀速动画会糊掉梯度方向感，故不用 EP 的 is-loading。
// 动画收口在本组件：调用方不再自挂全局旋转类；周期读全局 --icon-loading-duration 变量，
// keyframes 全局定义在 custom.scss（与 v-loading 蒙层覆盖规则共用，蒙层 CSS 够不到 scoped 哈希名）。
// 与 EP 裸 svg 的行盒对齐是 el-result 上下文规则（custom.scss 的 .el-result__icon svg.svg-icon），非本组件私有，不搬进来
.loading-icon {
	animation: icon-loading-rotate var(--icon-loading-duration) steps(12, end) infinite;
}
</style>