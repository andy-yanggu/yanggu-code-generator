<template>
	<el-popover
		:disabled="list.length <= previewCount"
		teleported
		:append-to="appStore.currentFullscreenElement || body"
		trigger="hover"
		placement="top"
		:width="width"
		:popper-options="{
			modifiers: [
				{
					name: 'flip',
					options: {
						fallbackPlacements: [] // 禁止自动翻转
					}
				}
			]
		}">
		<template #default>
			<el-space :size="5" wrap spacer="、">
				<template v-for="item in list" :key="item">
					<el-tag :type="tagType" :size="tagSize">{{ item }}</el-tag>
				</template>
			</el-space>
		</template>
		<template #reference>
			<div :style="{ cursor: list.length > previewCount ? 'pointer' : 'default' }">
				<el-tag v-for="item in list.slice(0, previewCount)" :key="item" :type="tagType" :size="tagSize" style="margin-right: 5px"> {{ item }}</el-tag>
				<el-tag v-if="list.length > previewCount" :type="tagType" :size="tagSize">+{{ list.length - previewCount }}</el-tag>
			</div>
		</template>
	</el-popover>
</template>
<script setup lang="ts">
import { useAppStore } from '@/store'

defineOptions({
	name: 'PopoverList'
})

defineProps({
	list: {
		type: Array as PropType<string[]>,
		default: [] as string[]
	},
	tagType: {
		type: String,
		default: 'primary'
	},
	tagSize: {
		type: String,
		default: 'small'
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

const body = document.body

const appStore = useAppStore()
</script>
