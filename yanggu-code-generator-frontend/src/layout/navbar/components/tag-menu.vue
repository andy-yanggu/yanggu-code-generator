<template>
	<div class="icon-list">
		<div class="icon-item" @click="emits('refreshCurrentTag')">
			<el-icon size="10"><Refresh></Refresh></el-icon>
			<span>刷新当前</span>
		</div>
		<div v-if="props.currentMenuTag.fullPath != '/index' || appStore.tagLength > 1" class="icon-item" @click="emits('closeCurrentTag')">
			<el-icon size="10"><CloseBold></CloseBold></el-icon>
			<span>关闭当前</span>
		</div>
		<div v-if="appStore.tagLength > 1" class="icon-item" @click="emits('closeOtherTags')">
			<el-icon size="10"><CircleClose></CircleClose></el-icon>
			<span>关闭其他</span>
		</div>
		<div v-if="props.currentMenuTagIndex > 0" class="icon-item" @click="emits('closeLeftTag')">
			<el-icon size="10"><Back></Back></el-icon>
			<span>关闭左侧</span>
		</div>
		<div v-if="props.currentMenuTagIndex < appStore.tagLength - 1" class="icon-item" @click="emits('closeRightTag')">
			<el-icon size="10"><Right></Right></el-icon>
			<span>关闭右侧</span>
		</div>
		<div v-if="appStore.tagLength > 1" class="icon-item" @click="emits('closeAllTags')">
			<el-icon size="10"><Close></Close></el-icon>
			<span>关闭全部</span>
		</div>
		<div class="icon-item" @click="emits('openNewWindow')">
			<svg-icon icon="icon-new-window"></svg-icon>
			<span>打开窗口</span>
		</div>
	</div>
</template>

<script setup lang="ts">
import { Back, CircleClose, Close, CloseBold, Refresh, Right } from '@element-plus/icons-vue'
import { NavbarTag, useAppStore } from '@/store/app-store'
import { defineEmits, defineProps, PropType } from 'vue'
import SvgIcon from '@/components/svg-icon/index.vue'

defineOptions({
	name: 'TagMenu'
})

const appStore = useAppStore()

const props = defineProps({
	currentMenuTag: {
		type: Object as PropType<NavbarTag>,
		required: true
	},
	currentMenuTagIndex: {
		type: Number,
		required: true
	}
})

const emits = defineEmits([
	'refreshCurrentTag',
	'closeCurrentTag',
	'closeAllTags',
	'closeOtherTags',
	'closeLeftTag',
	'closeRightTag',
	'openNewWindow'
])
</script>

<style scoped>
.icon-list {
	display: flex;
	flex-direction: column;
	gap: 10px;
	padding: 5px;
	width: 70px;
}

.icon-item {
	display: flex;
	align-items: center;
	gap: 5px;
	font-size: 12px;
	width: 100%;
	cursor: pointer;
	padding: 5px 10px;
	text-align: left;
	transition:
		background-color 0.2s ease,
		color 0.2s ease;
}

/* 悬停效果 - 亮色模式 */
.icon-item:hover {
	background-color: #f0f0f0;
	color: #333;
}

/* 暗黑模式样式 */
html.dark .icon-item:hover {
	background-color: #3a3a3a;
	color: #e0e0e0;
}
</style>
