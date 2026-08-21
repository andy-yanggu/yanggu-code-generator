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
				<icon-button v-if="useSearch" size="16px" :el-icon="Search" :tooltip="showSearch ? '隐藏搜索' : '显示搜索'" @click="toggleSearch()">
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
			</el-space>
		</div>
	</div>
</template>

<script setup lang="ts">
import { Refresh, Search } from '@element-plus/icons-vue'
import IconButton from '@/components/icon-button/index.vue'
import { ElDivider } from 'element-plus'

defineOptions({
	name: 'TableToolBar'
})

const showSearch = defineModel('showSearch', {
	type: Boolean,
	default: true
})

const queryLoading = defineModel('queryLoading', {
	type: Boolean,
	default: false
})

defineProps({
	useSearch: {
		type: Boolean,
		default: true
	},
	useRefresh: {
		type: Boolean,
		default: true
	}
})

const spacer = h(ElDivider, { direction: 'vertical' })

const emit = defineEmits(['getDataList'])

const toggleSearch = useToggle(showSearch)
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
