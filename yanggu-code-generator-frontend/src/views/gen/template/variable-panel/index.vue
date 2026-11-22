<template>
	<div class="variable-panel">
		<el-empty v-if="broken" description="变量面板不可用，请刷新页面后重试" :image-size="60"></el-empty>
		<PanelBody v-else :catalog="catalog" :loading="loading" :error="error" :disabled="disabled" :insert="insert" @reload="emit('reload')"></PanelBody>
	</div>
</template>

<script setup lang="ts">
// 错误边界只能包外层：onErrorCaptured 捕获的是后代组件的错误，捕获不到本组件自己的渲染错误。
// 所以会把目录 computed 与 el-tree 都放进 PanelBody，面板自身渲染崩时才落回这里的局部降级（规格 6.2 ②）
import PanelBody from './panel-body.vue'
import type { VariableNode } from '@/types'

defineOptions({
	name: 'VariablePanel'
})

defineProps({
	// 当前 tab 不是模板文件时禁用插入
	disabled: {
		type: Boolean,
		default: false
	},
	// 由宿主提供：把文本写进编辑器光标处，返回是否成功
	insert: {
		type: Function as PropType<(text: string, caret: number) => boolean>,
		required: true
	},
	// 目录状态由宿主持有：面板只在展开时挂载，而收起态也需要用同一份数据算总项数（规格 5.2）
	catalog: {
		type: Array as PropType<VariableNode[]>,
		default: () => [] as VariableNode[]
	},
	loading: {
		type: Boolean,
		default: false
	},
	// 空串表示无异常
	error: {
		type: String,
		default: ''
	}
})

// 重试透传给宿主：加载逻辑在宿主，面板只上报意图
const emit = defineEmits<{
	(e: 'reload'): void
}>()

// 目录结构畸形导致渲染异常时局部降级，不阻断模板编辑
const broken = ref(false)

onErrorCaptured(() => {
	broken.value = true
	// 阻断冒泡，避免抽屉主体被连带卸载
	return false
})
</script>

<style scoped>
.variable-panel {
	display: flex;
	flex-direction: column;
	height: 100%;
	padding-left: 8px;
	border-left: 1px solid var(--el-border-color-lighter);
	overflow: hidden;
}
</style>
