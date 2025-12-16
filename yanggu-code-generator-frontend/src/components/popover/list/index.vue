<template>
	<el-popover ref="popoverRef" :disabled="list.length <= previewCount" :teleported="!isFullscreen" trigger="hover" placement="top" :width="width">
		<template #default>
			<el-space :size="5" wrap spacer="、">
				<template v-for="item in list" :key="item">
					<el-tag size="small">{{ item }}</el-tag>
				</template>
			</el-space>
		</template>
		<template #reference>
			<div :style="{ cursor: list.length > previewCount ? 'pointer' : 'default' }">
				<el-tag v-for="item in list.slice(0, previewCount)" :key="item" size="small" style="margin-right: 5px"> {{ item }}</el-tag>
				<el-tag v-if="list.length > previewCount" size="small">+{{ list.length - previewCount }}</el-tag>
			</div>
		</template>
	</el-popover>
</template>
<script setup lang="ts">
import { PropType } from 'vue'
import { ElPopover } from 'element-plus'
import { useFullscreen } from '@vueuse/core'

defineOptions({
	name: 'PopoverList'
})

defineProps({
	list: {
		type: Array as PropType<string[]>,
		default: [] as string[]
	},
	width: {
		type: Number,
		default: 240
	},
	previewCount: {
		type: Number,
		default: 2
	}
})

const { isFullscreen } = useFullscreen()
</script>
