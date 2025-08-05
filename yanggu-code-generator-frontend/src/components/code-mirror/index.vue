<template>
	<el-scrollbar>
		<codemirror
			v-model="codes"
			placeholder="Code goes here..."
			:style="{ height: height + 'px' }"
			:autofocus="true"
			:indent-with-tab="true"
			:tab-size="2"
			:extensions="extensions"
			@ready="handleReady"
			@change="change($event)"
			@focus="log('focus', $event)"
			@blur="log('blur', $event)"
		></codemirror>
	</el-scrollbar>
</template>

<script setup>
import { ref, shallowRef, watch } from 'vue'
import { Codemirror } from 'vue-codemirror'
import { javascript } from '@codemirror/lang-javascript'
import { EditorView } from '@codemirror/view'

const props = defineProps({
	modelValue: {
		type: String
	},
	height: {
		type: Number,
		default: 400
	}
})
const codes = ref('')
watch(
	() => props.modelValue,
	val => {
		codes.value = val || ''
	},
	{
		immediate: true
		// deep: true
	}
)
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

const extensions = [javascript(), customStyle]
const log = console.log

// Codemirror EditorView instance ref
const view = shallowRef()
const handleReady = payload => {
	view.value = payload.view
}
const emit = defineEmits(['update:modelValue'])
const change = event => {
	emit('update:modelValue', event)
}
</script>
