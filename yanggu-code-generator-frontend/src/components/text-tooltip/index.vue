<template>
	<el-tooltip :content="content" :disabled="!isOverflow" :placement="placement">
		<el-text ref="textRef" :class="['text-tooltip-title', textClass]" :style="{ maxWidth }">
			{{ title }}
		</el-text>
	</el-tooltip>
</template>

<script setup lang="ts">
import type { Placement } from 'element-plus'
import { useTextOverflow } from '@/hooks/use-text-overflow'

defineOptions({
	name: 'TextTooltip'
})

/**
 * placement 用 el-tooltip 自己的 Placement 类型：runtime 声明（type: String）只能表达 string，
 * 传给 :placement 会报 TS2322。Placement 由 element-plus 根包导出，用 import type 引入
 * 只在编译期生效、产物里被完全擦除，不会给运行时增加 @popperjs/core 依赖。
 */
const props = withDefaults(
	defineProps<{
		title: string
		tooltipContent?: string
		maxWidth?: string
		placement?: Placement
		textClass?: string
	}>(),
	{
		tooltipContent: '',
		maxWidth: '100px', // 如果是el-row和el-col的title，则maxWidth为100%
		placement: 'top',
		textClass: ''
	}
)
// 变成响应式数据
const { title: titleRef, maxWidth: maxWidthRef } = toRefs(props)

const content = computed(() => {
	return props.tooltipContent || props.title
})

const { textRef, isOverflow } = useTextOverflow(titleRef, maxWidthRef)
</script>
<style scoped lang="scss">
.text-tooltip-title {
	display: inline-block;
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
	vertical-align: middle;
	color: inherit;
}
</style>
