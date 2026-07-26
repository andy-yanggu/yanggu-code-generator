<template>
	<el-row class="setting-row">
		<div class="setting-label">
			<el-text style="margin-right: 5px">{{ label }}</el-text>
			<el-tooltip v-if="tooltip" :content="tooltip" effect="dark" placement="top">
				<el-icon class="setting-icon"><QuestionFilled></QuestionFilled></el-icon>
			</el-tooltip>
		</div>
		<el-switch v-model="localValue" inline-prompt active-text="开" inactive-text="关"></el-switch>
	</el-row>
</template>

<script setup lang="ts">
import { QuestionFilled } from '@element-plus/icons-vue'

defineOptions({
	name: 'SystemSettingItem'
})

const localValue = defineModel({
	type: Boolean,
	required: true,
	default: false
})

defineProps({
	label: {
		type: String,
		required: true
	},
	tooltip: {
		type: String,
		default: ''
	}
})
</script>

<style scoped>
.setting-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 10px;
}
.setting-label {
	display: flex;
	align-items: center;
}
.setting-icon {
	cursor: pointer;
}
:deep(.el-switch) {
	--el-switch-on-color: var(--el-color-primary);
	--el-switch-off-color: var(--el-color-danger);
}
</style>
