<template>
	<Codemirror
		v-model="innerValue"
		placeholder="Code goes here..."
		:autofocus="true"
		:indent-with-tab="true"
		:tab-size="2"
		:extensions="extensions"
		@ready="handleReady"
	></Codemirror>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Codemirror } from 'vue-codemirror'
import { javascript } from '@codemirror/lang-javascript'
import { EditorView } from '@codemirror/view'
import { oneDarkTheme } from '@codemirror/theme-one-dark'
import { useAppStore } from '@/store/app-store'

const props = defineProps({
	modelValue: {
		type: String,
		default: ''
	},
	readOnly: {
		type: Boolean,
		default: false
	}
})

const emit = defineEmits(['update:modelValue'])

const appStore = useAppStore()

// 内部值，和外部 v-model 双向绑定
const innerValue = ref(props.modelValue)

// 监听外部传入的值，保持同步
watch(
	() => props.modelValue,
	val => {
		if (val !== innerValue.value) {
			innerValue.value = val
		}
	}
)

// 监听内部编辑器内容变化，通知父组件更新
watch(innerValue, val => {
	if (val !== props.modelValue) {
		emit('update:modelValue', val)
	}
})

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
</script>
