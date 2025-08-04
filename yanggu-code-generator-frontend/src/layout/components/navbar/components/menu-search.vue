<template>
	<!-- 搜索框 -->
	<div class="search-container">
		<el-tooltip :content="'菜单搜索'" effect="dark" placement="bottom" @click="openSearch">
			<el-icon :size="18" class="search-icon" @click="openSearch">
				<Search></Search>
			</el-icon>
		</el-tooltip>
		<el-dialog v-model="searchState.visible" placement="bottom" trigger="click" title="菜单搜索" class="menu-search-dialog" @opened="onDialogOpened">
			<el-input
				ref="searchInputRef"
				v-model="searchState.keyword"
				placeholder="请输入菜单名称"
				clearable
				:prefix-icon="Search"
				class="search-input"
				@input="querySearch"
				@keydown.up.prevent="handleKeyUp"
				@keydown.down.prevent="handleKeyDown"
				@keydown.enter="handleKeyEnter"
			></el-input>

			<!-- 包裹层用于键盘模式下控制 hover 样式 -->
			<el-scrollbar max-height="300px" class="search-results" :class="{ 'using-keyboard': searchState.usingKeyboard }">
				<el-row
					v-for="(item, index) in searchState.matchItemList"
					:key="item.path"
					class="menu-item"
					:class="{ active: index === searchState.activeIndex }"
					@click="handleSelect(item)"
					@mouseenter="handleMouseEnter(index)"
				>
					<!-- 面包屑风格展示路径 -->
					<span v-for="(breadcrumbItem, breadcrumbIndex) in item.breadcrumbItemList" :key="breadcrumbItem.title" class="breadcrumb-item">
						<svg-icon :icon="breadcrumbItem.icon"></svg-icon>
						<el-text>{{ breadcrumbItem.title }}</el-text>
						<el-text v-if="breadcrumbIndex < item.breadcrumbItemList.length - 1">/</el-text>
					</span>
				</el-row>
				<div v-if="searchState.matchItemList.length === 0" class="no-results">
					<el-text>无匹配结果</el-text>
				</div>
			</el-scrollbar>

			<!-- 底部快捷提示栏 -->
			<div class="shortcut-bar">
				<div class="icon-text">
					<svg-icon icon="icon-enter" class="icon-button"></svg-icon>
					<el-text class="hint-text">确认</el-text>
				</div>
				<div class="icon-text">
					<svg-icon icon="icon-arrowup" class="icon-button small-arrow"></svg-icon>
					<svg-icon icon="icon-arrowdown" class="icon-button small-arrow"></svg-icon>
					<el-text class="hint-text">切换</el-text>
				</div>
				<div class="icon-text">
					<el-text class="esc-box">ESC</el-text>
					<el-text class="hint-text">关闭</el-text>
				</div>
			</div>
		</el-dialog>
	</div>
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'
import SvgIcon from '@/components/svg-icon/src/svg-icon.vue'
import { nextTick, reactive, ref } from 'vue'
import { MenuInfo, useUserStore } from '@/store/user-store'
import { useRouter } from 'vue-router'

defineOptions({
	name: 'MenuSearch'
})

interface BreadcrumbItem {
	title: string
	icon: string
}

interface SearchItem extends MenuInfo {
	breadcrumbItemList: BreadcrumbItem[]
}

const searchState = reactive({
	visible: false,
	keyword: '',
	searchItemList: [] as SearchItem[],
	matchItemList: [] as SearchItem[],
	activeIndex: -1,
	usingKeyboard: false // 标记是否正在使用键盘控制
})

const searchInputRef = ref()
const userStore = useUserStore()
const router = useRouter()

// 打开搜索框
const openSearch = () => {
	searchState.visible = true
	searchState.keyword = ''
	searchState.activeIndex = -1
	searchState.usingKeyboard = false

	// 将 userStore.menuList 进行平铺展开
	searchState.searchItemList = []
	userStore.menuList.forEach(menuItem => {
		buildMenuInfo([], menuItem)
	})

	querySearch('')
}

const onDialogOpened = () => {
	nextTick(() => {
		searchInputRef.value?.focus()
	})
}

const buildMenuInfo = (parentBreadcrumb: BreadcrumbItem[], menuItem: MenuInfo) => {
	if (menuItem.meta.type === 2) {
		return
	}
	if (menuItem.meta.hidden) {
		return
	}

	const currentBreadcrumb = [
		...parentBreadcrumb,
		{
			title: menuItem.meta.title,
			icon: menuItem.meta.icon
		} as BreadcrumbItem
	]

	if (menuItem.meta.type === 1) {
		const newMenuItem = {
			...menuItem,
			meta: { ...menuItem.meta },
			breadcrumbItemList: currentBreadcrumb
		}
		searchState.searchItemList.push(newMenuItem)
		return
	}

	if (menuItem.children && menuItem.children.length > 0) {
		menuItem.children.forEach(child => {
			buildMenuInfo(currentBreadcrumb, child)
		})
	}
}

// 处理搜索建议
const querySearch = (queryString?: string) => {
	const raw = queryString !== undefined ? queryString : searchState.keyword
	if (!raw || !raw.trim()) {
		searchState.matchItemList = searchState.searchItemList
		searchState.activeIndex = -1
		searchState.usingKeyboard = false
		return
	}

	const keyword = raw.toLowerCase()
	const results: SearchItem[] = []

	searchState.searchItemList.forEach(menu => {
		const breadcrumbText = menu.breadcrumbItemList
			.flatMap(item => item.title)
			.join('/')
			.toLowerCase()
		if (breadcrumbText.includes(keyword)) {
			results.push(menu)
		}
	})
	searchState.matchItemList = results
	searchState.activeIndex = results.length > 0 ? 0 : -1
	searchState.usingKeyboard = false // 重新搜索时重置键盘状态
}

// 处理鼠标进入事件
const handleMouseEnter = (index: number) => {
	searchState.activeIndex = index
	searchState.usingKeyboard = false
}

// 处理向上键
const handleKeyUp = () => {
	if (searchState.matchItemList.length === 0) {
		return
	}
	searchState.usingKeyboard = true

	if (searchState.activeIndex === -1) {
		searchState.activeIndex = searchState.matchItemList.length - 1
	} else if (searchState.activeIndex === 0) {
		searchState.activeIndex = searchState.matchItemList.length - 1
	} else {
		searchState.activeIndex--
	}
}

// 处理向下键
const handleKeyDown = () => {
	if (searchState.matchItemList.length === 0) {
		return
	}
	searchState.usingKeyboard = true

	if (searchState.activeIndex === -1) {
		searchState.activeIndex = 0
	} else if (searchState.activeIndex === searchState.matchItemList.length - 1) {
		searchState.activeIndex = 0
	} else {
		searchState.activeIndex++
	}
}

// 处理回车键
const handleKeyEnter = () => {
	if (searchState.activeIndex >= 0 && searchState.matchItemList.length > 0) {
		handleSelect(searchState.matchItemList[searchState.activeIndex])
	}
}

// 选择搜索结果
const handleSelect = (item: any) => {
	searchState.visible = false
	searchState.keyword = ''
	searchState.activeIndex = -1
	searchState.usingKeyboard = false
	router.push(item.path)
}
</script>

<style scoped>
.search-container {
	display: flex;
	align-items: center;
}

.search-icon {
	cursor: pointer;
}

.search-results {
	margin-top: 8px;
	overflow-y: auto;
}

/* 禁用键盘模式下非激活项的 hover 高亮 */
.search-results.using-keyboard .menu-item:not(.active):hover {
	background-color: transparent;
	color: inherit;
	transform: none;
	box-shadow: none;
}
.search-results.using-keyboard .menu-item:not(.active)::before {
	left: -100%;
	transition: none;
}

.menu-item {
	display: flex;
	align-items: center;
	gap: 10px;
	margin: 3px 0;
	padding: 8px 12px;
	cursor: pointer;
	border-radius: 4px;
	transition: all 0.2s ease;
	position: relative;
	overflow: hidden;
	font-size: 13px;
}

/* 激活 / hover 的样式保持清爽不过于夸张 */
.menu-item:hover,
.menu-item.active {
	background-color: #409eff;
	color: white;
	transform: none;
	box-shadow: none;
}

/* 高亮滑动效果保持轻微 */
.menu-item::before {
	content: '';
	position: absolute;
	top: 0;
	left: -100%;
	width: 100%;
	height: 100%;
	background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
	transition: left 0.4s;
}

.menu-item:hover::before,
.menu-item.active::before {
	left: 100%;
}

.breadcrumb-item {
	display: inline-flex;
	align-items: center;
	gap: 4px;
	font-size: 13px;
}

.icon-text {
	display: flex;
	align-items: center;
	gap: 6px;
	font-size: 12px;
}

.icon-button {
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	padding: 4px 6px;
	box-shadow: none;
	background: #f5f7fa;
	display: inline-flex;
	align-items: center;
	font-size: 12px;
	line-height: 1;
}

/* 箭头用稍小一点以避免过重 */
.small-arrow {
	padding: 2px 5px;
}

/* ESC 框样式更接近文字按钮 */
.esc-box {
	display: inline-block;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	padding: 4px 8px;
	background: #f5f7fa;
	font-size: 12px;
	line-height: 1;
}

/* 底部快捷提示栏样式，贴合截图的白底、无阴影、紧凑布局 */
.shortcut-bar {
	display: flex;
	gap: 20px;
	margin: 10px 0 0;
	padding: 8px 12px;
	background: #ffffff;
	border-top: 1px solid #e6e6e6;
	font-size: 12px;
	align-items: center;
}

/* 搜索输入稍微压紧 */
.search-input {
	margin-bottom: 6px;
}

/* 无结果文案居中 */
.no-results {
	padding: 16px 0;
	text-align: center;
	color: #8c8c8c;
}
</style>
