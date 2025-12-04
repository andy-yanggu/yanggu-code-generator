<template>
	<div class="option-label">
		<span class="label">{{ label }}</span>
		<span v-if="desc && desc.trim()" class="desc">（{{ desc }}）</span>
	</div>
</template>

<script setup>
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
