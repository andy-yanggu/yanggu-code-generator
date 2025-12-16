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
				<el-tooltip v-if="useSearch && searchInner" :content="showSearch ? '隐藏搜索' : '显示搜索'" placement="top" :teleported="teleported">
					<el-button circle :icon="Search" @click="toggleSearch()"></el-button>
				</el-tooltip>

				<!-- 刷新 -->
				<el-tooltip v-if="useRefresh" content="刷新表格" placement="top" :teleported="teleported">
					<el-button circle :icon="Refresh" :loading-icon="Refresh" :loading="queryLoading" @click="emit('getDataList')"></el-button>
				</el-tooltip>

				<!-- 全屏 -->
				<el-tooltip
					v-if="useFullscreenButton && tableCardRef"
					:content="isFullscreen ? '退出全屏' : '开启全屏'"
					placement="top"
					:teleported="teleported"
				>
					<el-button circle @click="toggle">
						<svg-icon :icon="isFullscreen ? 'icon-fullscreen-exit' : 'icon-fullscreen'" is-pointer></svg-icon>
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
// 在Vue3组件中
import { computed, PropType, Ref } from 'vue'
import { useAppStore } from '@/store'

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

const props = defineProps({
	tableCardRef: {
		type: Object as PropType<Ref<HTMLElement>>,
		required: true
	},
	useSearch: {
		type: Boolean,
		default: true
	},
	useRefresh: {
		type: Boolean,
		default: true
	},
	useFullscreenButton: {
		type: Boolean,
		default: true
	}
})

const emit = defineEmits(['getDataList'])

const teleported = computed(() => !isFullscreen.value)

const toggleSearch = useToggle(showSearch)

const searchInner = computed(() => {
	return appStore.currentFullscreenElement !== props.tableCardRef.$el
})

const appStore = useAppStore()

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
