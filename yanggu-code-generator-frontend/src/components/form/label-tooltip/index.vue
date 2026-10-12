<template>
	<div :style="{ display: 'flex', alignItems: 'center', justifyContent: alignMap[align] }">
		<span>{{ label }}</span>
		<el-tooltip v-if="tooltip" :content="tooltip" effect="dark" placement="top">
			<el-icon style="margin-left: 5px; cursor: pointer">
				<template v-if="icon">
					<component :is="icon"></component>
				</template>
				<template v-else>
					<QuestionFilled></QuestionFilled>
				</template>
			</el-icon>
		</el-tooltip>
	</div>
</template>

<script setup lang="ts">
import { QuestionFilled } from '@element-plus/icons-vue'

defineOptions({
	name: 'FormLabelTooltip'
})

defineProps({
	label: {
		type: String,
		required: true
	},
	tooltip: {
		type: String,
		default: ''
	},
	icon: {
		type: Object,
		default: QuestionFilled,
		required: false
	},
	align: {
		type: String as () => 'left' | 'center' | 'right',
		default: 'left'
	}
})

const alignMap: Record<string, string> = {
	left: 'flex-start',
	center: 'center',
	right: 'flex-end'
}
</script>
