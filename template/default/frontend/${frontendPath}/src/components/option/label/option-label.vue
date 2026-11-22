<template>
	<div class="option-label">
		<span class="label">{{ label }}</span>
		<span v-if="isNotBlank(desc)" class="desc">（{{ desc }}）</span>
	</div>
</template>

<script setup>
import { isNotBlank } from '@/utils/tool.ts'

defineOptions({
	name: 'OptionLabel'
})

defineProps({
	label: {
		type: String,
		required: true
	},
	desc: {
		type: String,
		required: false,
		default: ''
	}
})
</script>

<style scoped>
.option-label {
	display: flex;
	align-items: center;
	gap: 5px;
}
.label {
	font-weight: bold;
}
.desc {
	color: #999;
	font-size: 12px;
}
</style>
