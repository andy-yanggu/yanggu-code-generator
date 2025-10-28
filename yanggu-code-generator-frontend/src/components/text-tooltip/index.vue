<template>
	<el-tooltip :content="title" :disabled="!isTitleOverflow" :placement="placement">
		<el-text ref="textRef" class="text-tooltip-title" :style="{ maxWidth }">
			{{ title }}
		</el-text>
	</el-tooltip>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

defineProps({
	title: {
		type: String,
		required: true
	},
	maxWidth: {
		type: String,
		default: '100px'
	},
	placement: {
		type: String,
		default: 'top'
	}
})

const textRef = ref()
const isTitleOverflow = ref(false)

// 检查文本是否溢出
onMounted(() => {
	if (textRef.value && textRef.value.$el) {
		const element = textRef.value.$el
		console.log(element)
		isTitleOverflow.value = element.scrollWidth > element.offsetWidth
		console.log(isTitleOverflow.value)
	}
})
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
