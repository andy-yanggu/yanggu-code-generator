<template>
	<div class="icon-list">
		<div class="icon-item" @click="emits('refreshCurrentTag')">
			<el-icon size="10"><Refresh></Refresh></el-icon>
			<span>刷新页面</span>
		</div>
		<div class="icon-item" @click="emits('openNewWindow')">
			<svg-icon icon="icon-new-window" size="10px"></svg-icon>
			<span>打开窗口</span>
		</div>
		<div class="icon-item" @click="emits('fullScreen')">
			<svg-icon icon="icon-fullscreen" size="10px"></svg-icon>
			<span>页面全屏</span>
		</div>
		<div class="icon-item" @click="emits('togglePin')">
			<svg-icon :icon="props.currentMenuTag.pinned ? 'icon-cancel-pin' : 'icon-pin'" size="10px"></svg-icon>
			<span>{{ props.currentMenuTag.pinned ? '取消固定' : '固定页面' }}</span>
		</div>
	</div>
	<template v-if="!props.currentMenuTag.pinned">
		<el-divider></el-divider>
		<div class="icon-list">
			<div
				v-if="!props.currentMenuTag.pinned && (props.currentMenuTag.fullPath !== defaultMenu || unpinnedLength > 1)"
				class="icon-item"
				@click="emits('closeCurrentTag')"
			>
				<el-icon size="10"><CloseBold></CloseBold></el-icon>
				<span>关闭当前</span>
			</div>
			<div v-if="currentUnpinnedIndex > 0" class="icon-item" @click="emits('closeLeftTag')">
				<el-icon size="10"><Back></Back></el-icon>
				<span>关闭左侧</span>
			</div>
			<div v-if="currentUnpinnedIndex < unpinnedLength - 1" class="icon-item" @click="emits('closeRightTag')">
				<el-icon size="10"><Right></Right></el-icon>
				<span>关闭右侧</span>
			</div>
			<div v-if="unpinnedLength > 1" class="icon-item" @click="emits('closeOtherTags')">
				<svg-icon icon="icon-close-others" size="10px"></svg-icon>
				<span>关闭其他</span>
			</div>
			<div v-if="unpinnedLength > 1" class="icon-item" @click="emits('closeAllTags')">
				<svg-icon icon="icon-close-all" size="10px"></svg-icon>
				<span>关闭全部</span>
			</div>
		</div>
	</template>
</template>

<script setup lang="ts">
import { Back, CloseBold, Refresh, Right } from '@element-plus/icons-vue'
import { useAppStore } from '@/store'
import { NavbarTag } from '@/types'
import { computed, defineEmits, defineProps, PropType } from 'vue'
import SvgIcon from '@/components/svg-icon/index.vue'

defineOptions({
	name: 'TagMenu'
})

const appStore = useAppStore()

const props = defineProps({
	defaultMenu: {
		type: String,
		required: true
	},
	currentMenuTag: {
		type: Object as PropType<NavbarTag>,
		required: true
	},
	currentMenuTagIndex: {
		type: Number,
		required: true
	}
})

const unpinnedTags = computed(() => appStore.tagList.filter(tag => !tag.pinned))

const unpinnedLength = computed(() => unpinnedTags.value.length)

const currentUnpinnedIndex = computed(() => unpinnedTags.value.findIndex(tag => tag.fullPath === props.currentMenuTag.fullPath))

const emits = defineEmits([
	'refreshCurrentTag',
	'closeCurrentTag',
	'closeAllTags',
	'closeOtherTags',
	'closeLeftTag',
	'closeRightTag',
	'openNewWindow',
	'fullScreen',
	'togglePin'
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
.el-divider--horizontal {
	margin: 0 0;
}
</style>
