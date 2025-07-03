<template>
	<div class="logo-container">
		<el-avatar src="./favicon.ico" size="small" class="logo-avatar"></el-avatar>
		<el-text class="logo-title">
			{{ appTitle }}
		</el-text>
	</div>
</template>
<script setup lang="ts">
const appTitle = import.meta.env.VITE_APP_TITLE
</script>

<style scoped>
.logo-container {
	display: flex;
	align-items: center;
	padding: 8px;
}

.logo-avatar {
	margin-right: 12px;
}

.logo-title {
	color: #2c3e50;
	font-size: 16px;
	font-weight: 550;
}
</style>
