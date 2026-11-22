<template>
	<Codemirror
		v-model="model"
		placeholder="Code goes here..."
		:autofocus="true"
		:indent-with-tab="true"
		:tab-size="2"
		:extensions="extensions"
		@ready="handleReady"></Codemirror>
</template>

<script setup lang="ts">
import { Codemirror } from 'vue-codemirror'
import { javascript } from '@codemirror/lang-javascript'
import { EditorView } from '@codemirror/view'
import { oneDarkTheme } from '@codemirror/theme-one-dark'
import { useAppStore } from '@/store'

defineOptions({
	name: 'CodeMirror'
})

const model = defineModel({
	type: String,
	required: true,
	default: ''
})

const props = defineProps({
	readOnly: {
		type: Boolean,
		default: false
	}
})

const appStore = useAppStore()

// codemirror 的只读样式
const customStyle = EditorView.theme({
	'&': {
		height: 'auto',
		maxHeight: '100%',
		overflow: 'visible'
	},
	'.cm-scroller': {
		overflow: 'visible'
	}
})

// 组合扩展
const extensions = computed(() => {
	const ext: any[] = [javascript()]
	if (props.readOnly) {
		ext.push(EditorView.editable.of(false))
	}
	// 如果是暗色模式，添加暗夜主题
	if (appStore.isDark) {
		ext.push(oneDarkTheme)
	}
	// 返回平铺的数组
	return [...ext, customStyle]
})

// Codemirror EditorView 实例
const view = ref()
const handleReady = (payload: any) => {
	view.value = payload.view
}

/**
 * 在当前光标处插入文本，并把光标落到 caret 偏移位（模板变量插入用）
 *
 * @returns 是否插入成功；编辑器尚未 ready 或处于只读时返回 false，由调用方提示而不是静默失败
 */
const insertAtCursor = (text: string, caret: number = text.length): boolean => {
	const editorView = view.value as EditorView | undefined
	if (!editorView || props.readOnly) {
		return false
	}
	const from = editorView.state.selection.main.from
	// selection 与 changes 同处一个事务，EditorState 会直接应用，不需要 @codemirror/commands 的 dispatchWithSelection
	editorView.dispatch({
		changes: { from, insert: text },
		selection: { anchor: from + caret }
	})
	// dispatch 触发 vue-codemirror 的 update 监听 -> v-model 回写 -> 宿主的既有 watch 自动置 isEdited
	editorView.focus()
	return true
}

defineExpose({
	insertAtCursor
})
</script>
