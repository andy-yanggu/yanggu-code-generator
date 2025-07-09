<template>
	<div class="icon-list">
		<div class="icon-item" @click="emits('refreshTag')">
			<el-icon size="10"><Refresh /></el-icon>
			<span>刷新页面</span>
		</div>
		<div v-if="props.currentTag.fullPath != '/index'" class="icon-item" @click="emits('closeTag')">
			<el-icon size="10"><CloseBold /></el-icon>
			<span>关闭当前</span>
		</div>
		<div v-if="store.tagLength > 1" class="icon-item" @click="emits('closeOtherTags')">
			<el-icon size="10"><CircleClose /></el-icon>
			<span>关闭其他</span>
		</div>
		<div v-if="props.currentTagIndex > 0" class="icon-item" @click="emits('closeLeftTag')">
			<el-icon size="10"><Back /></el-icon>
			<span>关闭左侧</span>
		</div>
		<div v-if="props.currentTagIndex < store.tagLength - 1" class="icon-item" @click="emits('closeRightTag')">
			<el-icon size="10"><Right /></el-icon>
			<span>关闭右侧</span>
		</div>
		<div v-if="store.tagLength > 1" class="icon-item" @click="emits('closeAllTags')">
			<el-icon size="10"><Close /></el-icon>
			<span>关闭全部</span>
		</div>
	</div>
</template>

<script setup lang="ts">
import { Back, CircleClose, Close, CloseBold, Refresh, Right } from '@element-plus/icons-vue'
import { appStore } from '@/store'
import { defineEmits, defineProps } from 'vue'

const store: any = appStore()

const props = defineProps({
	currentTag: {
		type: Object,
		required: true
	},
	currentTagIndex: {
		type: Number,
		required: true
	}
})

const emits = defineEmits(['refreshTag', 'closeTag', 'closeAllTags', 'closeOtherTags', 'closeLeftTag', 'closeRightTag'])
</script>

<style scoped>
.icon-list {
	display: flex;
	flex-direction: column;
	gap: 8px;
	padding: 6px;
	width: 70px; /* 设置列表容器的宽度 */
}

.icon-item {
	display: flex;
	align-items: center;
	gap: 3px;
	font-size: 12px;
	width: 100%; /* 让项目占满容器宽度 */
	cursor: pointer; /* 鼠标变成小手 */
	padding: 6px 8px;
	border-radius: 4px;
	transition:
		background-color 0.2s ease,
		color 0.2s ease; /* 动画过渡 */
}

/* 悬停效果 */
.icon-item:hover {
	background-color: #f0f0f0; /* 浅灰色高亮，可自定义颜色 */
	color: #333; /* 可选：加深文字颜色 */
}
</style>
