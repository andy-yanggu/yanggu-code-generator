<template>
	<!-- 搜索框 -->
	<div class="search-container">
		<el-tooltip content="菜单搜索" effect="dark" placement="bottom">
			<el-icon :size="18" class="search-icon" @click="openSearch">
				<Search></Search>
			</el-icon>
		</el-tooltip>
		<el-dialog
			v-model="searchState.visible"
			placement="bottom"
			trigger="click"
			title="菜单搜索"
			width="50%"
			destroy-on-close
			@opened="onDialogOpened"
		>
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
			<el-scrollbar ref="scrollbarRef" max-height="300px" class="search-results" :class="{ 'using-keyboard': searchState.usingKeyboard }">
				<el-row
					v-for="(item, index) in searchState.matchItemList"
					:key="item.path"
					class="menu-item"
					:class="{ active: index === searchState.activeIndex }"
					@click="handleSelect(item)"
					@mouseenter="handleMouseEnter(index)"
				>
					<!-- 面包屑风格展示路径 -->
					<el-breadcrumb>
						<el-breadcrumb-item v-for="breadcrumbItem in item.breadcrumbItemList" :key="breadcrumbItem.title">
							<span class="breadcrumb-content">
								<svg-icon :icon="breadcrumbItem.icon" is-pointer class="breadcrumb-icon"></svg-icon>
								<el-text class="breadcrumb-title">{{ breadcrumbItem.title }}</el-text>
							</span>
						</el-breadcrumb-item>
					</el-breadcrumb>
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
import SvgIcon from '@/components/svg-icon/index.vue'
import { nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { MenuInfo, useUserStore } from '@/store/user-store'
import { useRoute, useRouter } from 'vue-router'
import { useDebounceFn } from '@vueuse/core'
import { cloneObject, resetReactiveObject } from '@/utils/tool'

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

const INIT_STATE = {
	visible: false,
	keyword: '',
	searchItemList: [] as SearchItem[],
	matchItemList: [] as SearchItem[],
	activeIndex: -1,
	usingKeyboard: false // 是否在键盘模式
}

const searchState = reactive(cloneObject(INIT_STATE))

const searchInputRef = ref()
const scrollbarRef = ref()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

// 打开搜索框
const openSearch = () => {
	resetReactiveObject(searchState, INIT_STATE)
	searchState.visible = true

	userStore.menuList.forEach(menuItem => {
		buildMenuInfo([], menuItem)
	})

	querySearch('')
}

watch(
	() => searchState.keyword,
	() => {
		nextTick(() => scrollbarRef.value?.setScrollTop(0))
	}
)

const onDialogOpened = async () => {
	await nextTick()
	searchInputRef.value?.focus()
	scrollbarRef.value?.setScrollTop(0)
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

	// 菜单、iframe和外链
	if ([1, 3, 4].includes(menuItem.meta.type)) {
		const newMenuItem = {
			...menuItem,
			meta: { ...menuItem.meta },
			breadcrumbItemList: currentBreadcrumb
		}
		searchState.searchItemList.push(newMenuItem)
		return
	} else {
		// 目录
		if (menuItem.children?.length) {
			menuItem.children.forEach(child => buildMenuInfo(currentBreadcrumb, child))
		}
	}
}

// 创建防抖版本的搜索函数
const debouncedQuerySearch = useDebounceFn((queryString?: string) => {
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
			.map(item => item.title)
			.join('/')
			.toLowerCase()
		if (breadcrumbText.includes(keyword)) {
			results.push(menu)
		}
	})

	if (!results || results.length === 0) {
		return
	}

	searchState.matchItemList = results
	searchState.activeIndex = 0
	searchState.usingKeyboard = false
}, 100) // 100ms 防抖延迟

// 修改原来的 querySearch 函数来使用防抖
const querySearch = (queryString?: string) => {
	debouncedQuerySearch(queryString)
}

// 滚动到当前激活项
const scrollToActiveItem = () => {
	nextTick(() => {
		const container = scrollbarRef.value?.wrapRef as HTMLElement
		if (!container) {
			return
		}

		const activeEl = container.querySelector('.menu-item.active') as HTMLElement
		if (!activeEl) {
			return
		}

		activeEl.scrollIntoView({
			behavior: 'smooth',
			block: 'nearest'
		})
	})
}

// 鼠标移入（仅在鼠标模式下生效）
const handleMouseEnter = (index: number) => {
	if (!searchState.usingKeyboard) {
		searchState.activeIndex = index
	}
}

// 向上键
const handleKeyUp = () => {
	if (!searchState.matchItemList.length) {
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
	scrollToActiveItem()
}

// 向下键
const handleKeyDown = () => {
	if (!searchState.matchItemList.length) {
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
	scrollToActiveItem()
}

// 回车
const handleKeyEnter = () => {
	if (searchState.activeIndex >= 0 && searchState.matchItemList.length > 0) {
		handleSelect(searchState.matchItemList[searchState.activeIndex])
	}
}

// 选择
const handleSelect = (item: SearchItem) => {
	resetReactiveObject(searchState, INIT_STATE)
	// 如果是菜单或者iframe，则路由跳转
	if ([1, 3].includes(item.meta.type)) {
		if (item.path != route.path) {
			router.push(item.path!)
		}
	} else {
		// 如果是外链
		window.open(item.meta.externalUrl!)
	}
	if (item.path != userStore.activeMenuPath) {
		userStore.setActiveMenuPath(item.path!)
	}
}

// 监听鼠标移动：切回鼠标模式
const handleGlobalMouseMove = () => {
	if (searchState.usingKeyboard) {
		searchState.usingKeyboard = false
	}
}

onMounted(() => {
	window.addEventListener('mousemove', handleGlobalMouseMove)
})

onUnmounted(() => {
	window.removeEventListener('mousemove', handleGlobalMouseMove)
})
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
	position: relative;
	overflow: hidden;
	font-size: 13px;
	transition:
		color 0.3s,
		background-color 0.3s;
}
.menu-item:hover,
.menu-item.active {
	box-shadow:
		inset 3px 0 0 var(--theme-menu-border-color),
		inset -3px 0 0 var(--theme-menu-border-color);
	background-color: var(--theme-menu-hover-bg-color);
	color: var(--theme-menu-hover-color);
}
.menu-item:hover,
.menu-item.active .breadcrumb-content .breadcrumb-title {
	color: var(--theme-menu-hover-color);
}
.menu-item:hover,
.menu-item.active .breadcrumb-content .breadcrumb-icon {
	color: var(--theme-menu-hover-color);
}
.menu-item::before {
	content: '';
	position: absolute;
	top: 0;
	left: -100%;
	width: 100%;
	height: 100%;
	transition: left 0.4s;
}
.menu-item:hover::before,
.menu-item.active::before {
	left: 100%;
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
	display: inline-flex;
	align-items: center;
	font-size: 12px;
	line-height: 1;
}
.small-arrow {
	padding: 2px 5px;
}
.esc-box {
	display: inline-block;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	padding: 4px 8px;
	font-size: 12px;
	line-height: 1;
}
.shortcut-bar {
	display: flex;
	gap: 20px;
	margin: 10px 0 0;
	padding: 8px 12px;
	border-top: 1px solid var(--el-border-color);
	font-size: 12px;
	align-items: center;
}
.search-input {
	margin-bottom: 6px;
}
.no-results {
	padding: 16px 0;
	text-align: center;
	color: #8c8c8c;
}
.breadcrumb-content {
	cursor: pointer;
	display: inline-flex;
	align-items: center; /* 垂直居中 */
	gap: 5px; /* 控制图标和文字间距 */
}
</style>
