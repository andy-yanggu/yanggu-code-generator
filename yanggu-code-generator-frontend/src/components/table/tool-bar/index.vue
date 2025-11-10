<template>
	<div class="table-tool-bar">
		<!-- 左插槽：父组件传入 -->
		<div class="table-tool-bar-left">
			<slot name="left"></slot>
		</div>

		<!-- 右侧：通用工具 -->
		<div class="table-tool-bar-right">
			<el-space>
				<!-- 搜索 -->
				<el-tooltip :content="showSearch ? '隐藏搜索' : '显示搜索'" placement="top">
					<el-button circle :icon="Search" @click="toggleSearch()" />
				</el-tooltip>

				<!-- 刷新 -->
				<el-tooltip content="刷新表格" placement="top">
					<el-button circle :icon="Refresh" @click="emit('getDataList')" />
				</el-tooltip>

				<!-- 全屏 -->
				<el-tooltip :content="isFullscreen ? '退出全屏' : '开启全屏'" placement="top">
					<el-button circle @click="toggle()">
						<svg-icon :icon="isFullscreen ? 'icon-fullscreen-exit' : 'icon-fullscreen'" size="18px" is-pointer></svg-icon>
					</el-button>
				</el-tooltip>
			</el-space>
		</div>
	</div>
</template>

<script setup lang="ts">
import { Refresh, Search } from '@element-plus/icons-vue'
import { useFullscreen, useToggle } from '@vueuse/core'
import SvgIcon from '@/components/svg-icon'
import { PropType, Ref } from 'vue'

defineOptions({
	name: 'TableToolBar'
})

const showSearch = defineModel('showSearch', {
	type: Boolean,
	default: true
})

const props = defineProps({
	tableCardRef: {
		type: Object as PropType<Ref<HTMLElement>>,
		required: true
	}
})

const emit = defineEmits(['getDataList'])

const toggleSearch = useToggle(showSearch)

const { isFullscreen, toggle } = useFullscreen(props.tableCardRef)
</script>

<style scoped>
.table-tool-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
}
.table-tool-bar-left,
.table-tool-bar-right {
	display: flex;
	align-items: center;
}
</style>
