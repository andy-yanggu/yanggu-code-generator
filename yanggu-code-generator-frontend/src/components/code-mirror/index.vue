<template>
	<codemirror
		v-model="codes"
		placeholder="Code goes here..."
		:autofocus="true"
		:indent-with-tab="true"
		:tab-size="2"
		:extensions="extensions"
		@ready="handleReady"
		@change="change($event)"
	></codemirror>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Codemirror } from 'vue-codemirror'
import { javascript } from '@codemirror/lang-javascript'
import { EditorView } from '@codemirror/view'

const props = defineProps({
	readOnly: {
		type: Boolean,
		default: false
	}
})

const codes = ref('')
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

const extensions = computed(() => {
	const ext = [javascript()]
	if (props.readOnly) {
		ext.push(EditorView.editable.of(false))
	}
	return [ext, customStyle]
})

// Codemirror EditorView instance ref
const view = ref()
const handleReady = payload => {
	view.value = payload.view
}
const emit = defineEmits(['update:modelValue'])
const change = event => {
	emit('update:modelValue', event)
}
</script>
