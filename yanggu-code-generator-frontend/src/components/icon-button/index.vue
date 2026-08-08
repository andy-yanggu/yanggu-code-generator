<template>
	<el-tooltip :content="tooltip" placement="top" :disabled="disabled || isBlank(tooltip)">
		<!-- 1️⃣ icon-only：完全没有 slot，DOM 最干净 -->
		<el-button
			v-if="elIcon && !svgIcon && !slots.default"
			link
			class="icon-button"
			:class="className"
			:style="computedStyle"
			:icon="elIcon"
			:disabled="disabled"
			v-bind="$attrs"
			@click="handleClick"
		></el-button>

		<!-- 2️⃣ 有 slot / svg-icon：允许 span -->
		<el-button v-else link class="icon-button" :class="className" :style="computedStyle" :disabled="disabled" v-bind="$attrs" @click="handleClick">
			<slot v-if="slots.default"></slot>
			<svg-icon v-else-if="svgIcon" :icon="svgIcon"></svg-icon>
		</el-button>
	</el-tooltip>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/svg-icon/index.vue'
import { ElButton } from 'element-plus'
import { isBlank } from '@/utils/tool'

defineOptions({
	name: 'IconButton'
})

const props = defineProps({
	// class名称
	className: {
		type: String,
		default: ''
	},
	// 图标大小
	size: {
		type: String,
		default: '20px'
	},
	// svg-icon 的 icon 名称
	svgIcon: {
		type: String,
		default: ''
	},
	// element-plus icon 组件，如 Edit / Delete
	elIcon: {
		type: Object as PropType<Component>,
		default: undefined
	},
	// 是否禁用
	disabled: {
		type: Boolean,
		default: false
	},
	// 提示信息
	tooltip: {
		type: String,
		default: ''
	}
})

const emit = defineEmits<{
	(e: 'click', evt: MouseEvent): void
}>()

const slots = useSlots()

const computedStyle = computed(() => {
	const style: Record<string, any> = {
		color: 'inherit'
	}
	if (props.size) {
		style.fontSize = props.size
	}
	return style
})

const handleClick = (e: MouseEvent) => {
	if (props.disabled) {
		return
	}
	emit('click', e)
}
</script>
<style scoped lang="scss">
.icon-button:not(.is-disabled):hover {
	color: var(--el-color-primary) !important;
	transform: scale(var(--icon-hover-transform-scale)) translateY(var(--icon-hover-transform-translate-y));
}
</style>
