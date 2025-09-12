<template>
	<el-row class="setting-row">
		<div class="setting-label">
			<el-text style="margin-right: 5px">{{ label }}</el-text>
			<el-tooltip v-if="tooltip" :content="tooltip" effect="dark" placement="top">
				<el-icon class="setting-icon"><InfoFilled></InfoFilled></el-icon>
			</el-tooltip>
		</div>
		<el-switch v-model="localValue" inline-prompt active-text="开" inactive-text="关"></el-switch>
	</el-row>
</template>

<script setup lang="ts">
import { InfoFilled } from '@element-plus/icons-vue'
import { computed } from 'vue'

defineOptions({
	name: 'SystemSettingItem'
})

const props = defineProps<{
	label: string
	tooltip?: string
	modelValue: boolean
}>()

const emit = defineEmits<{
	(e: 'update:modelValue', value: boolean): void
}>()

// 通过 computed 做双向绑定
const localValue = computed({
	get: () => props.modelValue,
	set: (val: boolean) => emit('update:modelValue', val)
})
</script>

<style scoped>
.setting-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 10px;
}
.setting-icon {
	cursor: pointer;
}
.setting-label {
	display: flex;
	align-items: center;
}
</style>
