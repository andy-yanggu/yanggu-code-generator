<template>
	<el-popover
		:disabled="list.length <= previewCount"
		teleported
		:append-to="currentFullscreenElement || body"
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
import { useLayout } from '@/hooks/use-layout'

defineOptions({
	name: 'PopoverList'
})

/**
 * el-tag 的 type / size 只接受固定字面量，而 runtime 声明（type: String）只能表达 string，
 * 传给 :type / :size 时会报 TS2322。改用类型声明式 defineProps 把字面量联合直接写进签名。
 * 取值与 element-plus 2.11.3 的 el-tag props 保持一致（EP 未导出 TagProps，故此处内联）。
 */
withDefaults(
	defineProps<{
		list?: string[]
		tagType?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
		tagSize?: '' | 'small' | 'default' | 'large'
		width?: number
		previewCount?: number
	}>(),
	{
		// 数组默认值必须用工厂函数返回，否则所有组件实例会共享同一个数组引用
		list: () => [],
		tagType: 'primary',
		tagSize: 'small',
		width: 240,
		previewCount: 2
	}
)

const body = document.body

const { currentFullscreenElement } = useLayout()
</script>
