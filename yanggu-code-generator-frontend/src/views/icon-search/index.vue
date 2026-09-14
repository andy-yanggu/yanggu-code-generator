<template>
	<el-container class="main-layout">
		<!-- 左侧分类面板：分类搜索 + 分类列表 -->
		<el-aside width="200px" class="category-sidebar">
			<div class="sidebar-header">
				<el-input v-model="categorySearch" placeholder="请输入分类名称" :prefix-icon="Search" clearable></el-input>
				<el-divider style="margin: 10px 0 6px"></el-divider>
			</div>
			<el-scrollbar class="sidebar-scrollbar">
				<div v-show="!categoryKeyword" class="sidebar-item" :class="{ active: selectedKey === 'all' }" @click="selectedKey = 'all'">
					<span class="sidebar-label">全部图标</span>
					<span class="sidebar-count">{{ totalCount }}</span>
				</div>
				<div
					v-for="cat in filteredCategories"
					:key="cat.key"
					class="sidebar-item"
					:class="{ active: selectedKey === cat.key }"
					@click="selectedKey = cat.key">
					<text-tooltip :title="cat.label" max-width="120px" />
					<span class="sidebar-count">{{ cat.icons.length }}</span>
				</div>
				<el-empty v-if="categoryKeyword && filteredCategories.length === 0" description="未找到匹配的分类" :image-size="150"></el-empty>
			</el-scrollbar>
		</el-aside>

		<!-- 右侧图标面板：图标搜索 + 图标网格 -->
		<el-main class="icon-grid">
			<el-input v-model="iconSearch" placeholder="请输入图标名称" :prefix-icon="Search" clearable></el-input>
			<el-divider style="margin: 10px 0 6px"></el-divider>
			<div v-if="currentIcons.length > 0" class="section-header">
				<span class="section-title">{{ selectedKey === 'all' ? '全部图标' : currentCategory?.label }}</span>
				<el-tag size="small" round type="info">{{ selectedKey === 'all' ? totalCount : currentCategory?.icons.length }}</el-tag>
			</div>

			<el-row v-if="pagedIcons.length > 0" :gutter="12" class="icon-list">
				<el-col v-for="(iconName, index) in pagedIcons" :key="iconName" :span="3">
					<div class="icon-item" @click="selectIcon(iconName)">
						<span class="item-index">{{ (currentPage - 1) * pageSize + index + 1 }}</span>
						<svg-icon :icon="iconName" class="item-icon" />
						<text-tooltip :title="iconName" />
					</div>
				</el-col>
			</el-row>

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

const categorySearch = ref('')
const iconSearch = ref('')
const selectedKey = ref('all')
const currentPage = ref(1)
const pageSize = 40

const iconCategories = computed(() => dataIconCategories)

/** 图标总数 */
const totalCount = computed(() => iconCategories.value.reduce((sum, c) => sum + c.icons.length, 0))

/** 分类搜索关键词 */
const categoryKeyword = computed(() => categorySearch.value.trim().toLowerCase())

/** 图标搜索关键词 */
const iconKeyword = computed(() => iconSearch.value.trim().toLowerCase())

/** 按分类名称过滤后的分类列表 */
const filteredCategories = computed<IconCategory[]>(() => {
	if (!categoryKeyword.value) {
		return iconCategories.value
	} else {
		return iconCategories.value.filter(cat => cat.label.toLowerCase().includes(categoryKeyword.value))
	}
})

/** 当前选中分类的数据 */
const currentCategory = computed<IconCategory | null>(() => {
	if (selectedKey.value === 'all') {
		return null
	} else {
		return iconCategories.value.find(c => c.key === selectedKey.value) ?? null
	}
})

/** 当前要展示的图标列表：先按分类筛选，再按图标名称搜索 */
const currentIcons = computed<string[]>(() => {
	const icons = selectedKey.value === 'all' ? iconCategories.value.flatMap(c => c.icons) : (currentCategory.value?.icons ?? [])
	if (!iconKeyword.value) return icons
	return icons.filter(icon => icon.toLowerCase().includes(iconKeyword.value))
})

/** 总页数 */
const totalPages = computed(() => Math.ceil(currentIcons.value.length / pageSize))

/** 当前页的图标 */
const pagedIcons = computed(() => {
	const start = (currentPage.value - 1) * pageSize
	return currentIcons.value.slice(start, start + pageSize)
})

/** 切换分类或搜索时重置到第 1 页 */
watch([selectedKey, categoryKeyword, iconKeyword], () => {
	currentPage.value = 1
})

/** 分类过滤结果变化时，自动同步选中状态 */
watch(filteredCategories, cats => {
	if (!categoryKeyword.value) {
		return
	}
	if (cats.length > 0) {
		selectedKey.value = cats[0].key
	} else {
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
/* ─── 主体布局 ─── */
.main-layout {
	height: 100%;
	min-height: 0;
	gap: 12px;
}

/* ─── 左侧分类导航 ─── */
.category-sidebar {
	border-right: 1px solid var(--el-border-color-lighter);
	background: var(--el-bg-color-overlay);
	padding: 10px 10px 0;
	overflow: hidden;
	display: flex;
	flex-direction: column;
}

.sidebar-header {
	flex-shrink: 0;
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

.sidebar-scrollbar {
	flex: 1;
	min-height: 0;
}

/* ─── 右侧图标网格 ─── */
.icon-grid {
	min-width: 0;
	min-height: 0;
	overflow-y: auto;
	background: var(--el-bg-color-overlay);
	padding-top: 10px;
}

.section-header {
	display: flex;
	align-items: center;
	gap: 8px;
	margin-bottom: 12px;
	padding-bottom: 8px;
	border-bottom: 1px solid var(--el-border-color-extra-light);
}

.section-title {
	font-size: 14px;
	font-weight: 600;
	color: var(--el-text-color-primary);
}

.icon-list {
	row-gap: 12px;
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
