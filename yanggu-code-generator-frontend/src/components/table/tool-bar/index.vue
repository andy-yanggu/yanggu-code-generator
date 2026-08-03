<template>
	<div class="table-tool-bar">
		<!-- 左插槽：父组件传入 -->
		<div class="table-tool-bar-left">
			<slot name="left"></slot>
		</div>

		<!-- 右侧：通用工具 -->
		<div class="table-tool-bar-right">
			<el-space size="default">
				<!-- 搜索 -->
				<el-tooltip v-if="useSearch" :content="showSearch ? '隐藏搜索' : '显示搜索'" placement="top">
					<el-button circle :icon="Search" @click="toggleSearch()"></el-button>
				</el-tooltip>

				<!-- 刷新 -->
				<el-tooltip v-if="useRefresh" content="刷新表格" placement="top">
					<el-button circle :icon="Refresh" :loading-icon="Refresh" :loading="queryLoading" @click="emit('getDataList')"></el-button>
				</el-tooltip>
			</el-space>
		</div>
	</div>
</template>

<script setup lang="ts">
import { Refresh, Search } from '@element-plus/icons-vue'
import { useToggle } from '@vueuse/core'

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
</style>
