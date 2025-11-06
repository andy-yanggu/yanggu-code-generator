<template>
	<div class="form-divider-content">
		<div class="form-blue-indicator"></div>
		<el-text class="form-section-title">{{ title }}</el-text>
	</div>
</template>
<script setup lang="ts">
defineProps({
	title: {
		type: String,
		required: true
	}
})
</script>

<style scoped>
.form-divider-content {
	display: flex;
	align-items: center;
	gap: 10px; /* 蓝条与文字的间距，可选 */
	margin: 16px 0 20px;
}
/* 当上一个元素是 el-row 时，减少顶边距 */
.el-row + .form-divider-content {
	margin-top: 8px !important;
}
.form-divider-content:first-child {
	margin-top: 0;
}
.form-blue-indicator {
	display: flex;
	width: 4px;
	height: 1.2em;
	background-color: var(--el-color-primary);
	border-radius: 2px;
}
.form-section-title {
	color: var(--el-text-color-primary);
	font-size: 15px;
	font-weight: 600;
}
</style>
