<template>
	<div class="table-tool-bar">
		<!-- 左插槽：父组件传入 -->
		<div class="table-tool-bar-left">
			<slot name="left"></slot>
		</div>

		<!-- 右侧：通用工具 -->
		<div class="table-tool-bar-right">
			<el-space size="small" :spacer="spacer">
				<!-- 搜索 -->
				<icon-button
					v-if="useSearch" size="16px"
					:el-icon="Search"
					:tooltip="showSearch ? '隐藏搜索' : '显示搜索'"
					@click="toggleSearch()">
				</icon-button>

				<!-- 刷新 -->
				<icon-button
					v-if="useRefresh"
					size="16px"
					:el-icon="Refresh"
					tooltip="刷新表格"
					:loading="queryLoading"
					:loading-icon="Refresh"
					@click="emit('getDataList')">
				</icon-button>

				<!-- 最大化 -->
				<icon-button
					v-if="useMaximize"
					size="16px"
					:el-icon="FullScreen"
					:tooltip="maximized ? '还原表格' : '最大化表格'"
					@click="handleToggleMaximized()"></icon-button>
			</el-space>
		</div>
	</div>
</template>

<script setup lang="ts">
import { FullScreen, Refresh, Search } from '@element-plus/icons-vue'
import IconButton from '@/components/icon-button/index.vue'
import { ElDivider } from 'element-plus'

defineOptions({
	name: 'TableToolBar'
})

// 查询区域是否显示
const showSearch = defineModel('showSearch', {
	type: Boolean,
	default: true
})

// 查询加载中状态
const queryLoading = defineModel('queryLoading', {
	type: Boolean,
	default: false
})

// 表格卡片是否最大化
const maximized = defineModel('maximized', {
	type: Boolean,
	default: false
})

defineProps({
	// 是否显示搜索按钮
	useSearch: {
		type: Boolean,
		default: true
	},
	// 是否显示刷新按钮
	useRefresh: {
		type: Boolean,
		default: true
	},
	// 是否显示最大化按钮
	useMaximize: {
		type: Boolean,
		default: true
	}
})

const spacer = h(ElDivider, { direction: 'vertical' })

const emit = defineEmits(['getDataList'])

const toggleSearch = useToggle(showSearch)
const toggleMaximized = useToggle(maximized)

// 最大化切换，进入最大化时提示 ESC 退出
const handleToggleMaximized = () => {
	toggleMaximized()
	if (!maximized.value) {
		ElMessage.info('按 ESC 可退出最大化')
	}
}

// ESC 退出最大化
onKeyStroke('Escape', () => {
	if (maximized.value) {
		maximized.value = false
	}
})
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
:deep(.el-divider--vertical) {
	margin: 0 0;
}
</style>
