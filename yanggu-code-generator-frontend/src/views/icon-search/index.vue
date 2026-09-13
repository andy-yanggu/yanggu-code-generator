<template>
	<el-container class="icon-search" direction="vertical">
		<!-- 顶部搜索栏 -->
		<div class="search-bar">
			<el-input v-model="searchText" placeholder="请输入图标名称或分类名称" :prefix-icon="Search" clearable size="large" />
			<span class="search-stats">{{ filteredCount }} / {{ totalCount }}</span>
		</div>

		<!-- 主体：左侧分类 + 右侧网格 -->
		<el-container class="main-layout">
			<!-- 左侧分类导航 -->
			<el-aside width="200px" class="category-sidebar">
				<div class="sidebar-item" :class="{ active: selectedKey === 'all' }" @click="selectedKey = 'all'">
					<span class="sidebar-label">全部图标</span>
					<span class="sidebar-count">{{ totalCount }}</span>
				</div>
				<div
					v-for="cat in sidebarCategories"
					:key="cat.key"
					class="sidebar-item"
					:class="{ active: selectedKey === cat.key, 'name-match': keyword && cat.label.toLowerCase().includes(keyword) }"
					@click="selectedKey = cat.key">
					<text-tooltip :title="cat.label" max-width="120px" />
					<span class="sidebar-count">{{ cat.icons.length }}</span>
				</div>
			</el-aside>

			<!-- 右侧图标网格 -->
			<el-main class="icon-grid">
				<div v-if="currentCategory && currentIcons.length > 0" class="section-header">
					<span class="section-title">{{ currentCategory.label }}</span>
					<el-tag size="small" round type="info">{{ currentCategory.icons.length }}</el-tag>
				</div>

				<div v-if="pagedIcons.length > 0" class="icon-list">
					<div v-for="(iconName, index) in pagedIcons" :key="iconName" class="icon-item" @click="selectIcon(iconName)">
						<span class="item-index">{{ (currentPage - 1) * pageSize + index + 1 }}</span>
						<svg-icon :icon="iconName" class="item-icon" />
						<text-tooltip :title="iconName" />
					</div>
				</div>

				<el-empty v-if="currentIcons.length === 0" description="未找到匹配的图标" />

				<div v-if="totalPages > 1" class="pagination-bar">
					<el-pagination
						v-model:current-page="currentPage"
						:page-size="pageSize"
						:total="currentIcons.length"
						layout="total, prev, pager, next"
						background
						size="default" />
				</div>
			</el-main>
		</el-container>
	</el-container>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/svg-icon/index.vue'
import { copyToClipboard } from '@/utils/tool'
import { Search } from '@element-plus/icons-vue'
import TextTooltip from '@/components/text-tooltip/index.vue'
import { iconCategories as dataIconCategories } from '@/icons/iconfont/modules'

/** 图标分类 */
interface IconCategory {
	key: string
	label: string
	icons: string[]
}

defineOptions({
	name: 'IconSearch'
})

const searchText = ref('')
const selectedKey = ref('all')
const currentPage = ref(1)
const pageSize = 48 // 8 列 × 6 行

const iconCategories = computed(() => {
	return dataIconCategories
})

/** 图标总数 */
const totalCount = computed(() => iconCategories.value.reduce((sum, c) => sum + c.icons.length, 0))

/** 搜索关键词 */
const keyword = computed(() => searchText.value.trim().toLowerCase())

/** 搜索时自动筛选有结果的分类，无搜索时返回全部分类。支持按分类名称匹配 */
const displayedCategories = computed<IconCategory[]>(() => {
	if (!keyword.value) return iconCategories.value
	return iconCategories.value
		.map(cat => {
			/** 分类名称匹配 → 保留该分类下所有图标 */
			if (cat.label.toLowerCase().includes(keyword.value)) {
				return { ...cat, icons: [...cat.icons].sort() }
			}
			/** 否则只保留图标名称匹配的图标 */
			return { ...cat, icons: cat.icons.filter(icon => icon.toLowerCase().includes(keyword.value)).sort() }
		})
		.filter(cat => cat.icons.length > 0)
})

/** 左侧导航栏的分类（复用 displayedCategories 的过滤结果） */
const sidebarCategories = computed(() => displayedCategories.value)

/** 当前选中分类的数据 */
const currentCategory = computed<IconCategory | null>(() => {
	if (selectedKey.value === 'all') return null
	return displayedCategories.value.find(c => c.key === selectedKey.value) ?? null
})

/** 当前要展示的图标列表 */
const currentIcons = computed<string[]>(() => {
	// 搜索模式或选中“全部”：展示所有匹配分类的图标
	if (selectedKey.value === 'all') {
		return displayedCategories.value.flatMap(c => c.icons)
	}
	return currentCategory.value?.icons ?? []
})

/** 筛选后的图标总数 */
const filteredCount = computed(() => currentIcons.value.length)

/** 总页数 */
const totalPages = computed(() => Math.ceil(currentIcons.value.length / pageSize))

/** 当前页的图标 */
const pagedIcons = computed(() => {
	const start = (currentPage.value - 1) * pageSize
	return currentIcons.value.slice(start, start + pageSize)
})

/** 切换分类或搜索时重置到第 1 页 */
watch([selectedKey, keyword], () => {
	currentPage.value = 1
})

/** 搜索词变化时：分类名匹配则自动选中第一个，清空则回退到全部 */
watch(keyword, () => {
	if (keyword.value) {
		const firstMatch = displayedCategories.value.find(cat => cat.label.toLowerCase().includes(keyword.value))
		if (firstMatch) {
			selectedKey.value = firstMatch.key
		}
	} else {
		selectedKey.value = 'all'
	}
})

/** 选中分类变化时，如果该分类无结果则回退到“全部” */
watch(selectedKey, key => {
	if (key !== 'all' && !sidebarCategories.value.find(c => c.key === key)) {
		selectedKey.value = 'all'
	}
})

/** 选择图标时复制到剪贴板 */
const selectIcon = (iconName: string) => {
	copyToClipboard(iconName).then(() => {
		ElMessage.success(`${iconName} 已复制到剪贴板`)
	})
}
</script>
<style scoped>
/* ─── 根容器 ─── */
.icon-search {
	height: 100%;
	gap: 12px;
	--el-aside-width: 200px;
}

/* ─── 搜索栏 ─── */
.search-bar {
	display: flex;
	align-items: center;
	gap: 16px;
	flex-shrink: 0;
	position: sticky;
	top: 0;
	z-index: 10;
	background: var(--el-bg-color-page);
	padding: 8px 0;
}

.search-stats {
	white-space: nowrap;
	color: var(--el-text-color-secondary);
	font-size: 13px;
	font-variant-numeric: tabular-nums;
}

/* ─── 主体布局 ─── */
.main-layout {
	flex: 1;
	min-height: 0;
	gap: 12px;
}

/* ─── 左侧分类导航 ─── */
.category-sidebar {
	overflow-y: auto;
	border-right: 1px solid var(--el-border-color-lighter);
	background: var(--el-bg-color-overlay);
	padding: 6px 6px 16px;
}

.sidebar-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 8px 12px;
	border-radius: 6px;
	cursor: pointer;
	transition: all 0.2s;
	font-size: 13px;
	color: var(--el-text-color-regular);
}

.sidebar-item:hover {
	background: var(--el-fill-color-light);
}

.sidebar-item.active {
	background: var(--el-color-primary-light-9);
	color: var(--el-color-primary);
	font-weight: 500;
}

/** 搜索时分类名称匹配关键词 → 高亮 */
.sidebar-item.name-match {
	border-left: 2px solid var(--el-color-primary);
}

.sidebar-count {
	flex-shrink: 0;
	margin-left: 8px;
	font-size: 12px;
	color: var(--el-text-color-placeholder);
	font-variant-numeric: tabular-nums;
}

.sidebar-item.active .sidebar-count {
	color: var(--el-color-primary-light-3);
}

/* ─── 右侧图标网格 ─── */
.icon-grid {
	min-width: 0;
	min-height: 0;
	overflow-y: auto;
	background: var(--el-bg-color-overlay);
	padding: 16px;
	--el-main-padding: 16px;
}

.section-header {
	display: flex;
	align-items: center;
	gap: 8px;
	margin-bottom: 12px;
	padding-bottom: 8px;
	border-bottom: 1px solid var(--el-border-color-extra-light);
}

.section-header ~ .icon-list {
	margin-bottom: 20px;
}

.section-title {
	font-size: 14px;
	font-weight: 600;
	color: var(--el-text-color-primary);
}

.icon-list {
	display: grid;
	grid-template-columns: repeat(8, 1fr);
	gap: 12px;
}

.icon-item {
	position: relative;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8px;
	padding: 16px 4px 12px;
	border: 1px solid var(--el-border-color-light);
	border-radius: 8px;
	cursor: pointer;
	transition: all 0.25s ease;
	background: var(--el-bg-color);
}

.item-index {
	position: absolute;
	top: 4px;
	left: 6px;
	display: flex;
	align-items: center;
	justify-content: center;
	width: 18px;
	height: 18px;
	border-radius: 50%;
	border: 1px solid var(--el-border-color);
	font-size: 10px;
	font-weight: 600;
	line-height: 1;
	color: var(--el-text-color);
	font-variant-numeric: tabular-nums;
	user-select: none;
}

.icon-item:hover {
	border-color: var(--el-color-primary);
	box-shadow: 0 4px 12px var(--el-border-color-lighter);
	transform: translateY(-2px);
}

:deep(.icon-item:hover .text-tooltip-title) {
	color: var(--el-color-primary);
}

.item-icon {
	font-size: 36px;
	color: var(--el-text-color-primary);
	transition: all 0.25s ease;
}

/* ─── 分页栏 ─── */
.pagination-bar {
	display: flex;
	justify-content: center;
	padding-bottom: 16px;
	margin-top: 8px;
	border-top: 1px solid var(--el-border-color-extra-light);
}

.icon-item:hover .item-icon {
	color: var(--el-color-primary);
	transform: scale(1.15);
}
</style>
