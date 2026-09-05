<template>
	<div class="search-container">
		<icon-button :el-icon="Search" tooltip="菜单搜索" size="18px" @click="openSearch()"></icon-button>

		<!-- 搜索框 -->
		<el-dialog v-model="searchState.visible" title="菜单搜索" width="45%" destroy-on-close @opened="onDialogOpened">
			<el-input
				ref="searchInputRef"
				v-model="searchState.keyword"
				placeholder="请输入菜单名称"
				clearable
				:prefix-icon="Search"
				class="search-input"
				@input="querySearch"></el-input>

			<!-- 树形结构展示 -->
			<el-scrollbar ref="scrollbarRef" max-height="400px" class="search-results">
				<MenuTreeNode v-for="node in searchState.matchItemList" :key="node.title" :node="node" :level="0" :on-select="handleSelect"></MenuTreeNode>
				<div v-if="isEmpty(searchState.matchItemList)" class="no-results">
					<el-text>无匹配结果</el-text>
				</div>
			</el-scrollbar>
		</el-dialog>
	</div>
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { MenuInfo } from '@/types'
import MenuTreeNode, { TreeNode } from '@/layout/navbar/components/menu-tree-node.vue'
import IconButton from '@/components/icon-button/index.vue'
import { isEmpty } from '@/utils/tool'

defineOptions({
	name: 'MenuSearch'
})

const initState = () => ({
	visible: false,
	keyword: '',
	searchItemList: [] as TreeNode[],
	matchItemList: [] as TreeNode[]
})

const searchState = reactive(initState())

const searchInputRef = ref()
const scrollbarRef = ref()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

// 构建树结构
const buildMenuTree = (menuList: MenuInfo[]): TreeNode[] => {
	const build = (items: MenuInfo[], parentPath: string = ''): TreeNode[] => {
		return items
			.filter(item => !item.meta.hideMenu && item.meta.type !== 2)
			.map(item => {
				// 处理路径拼接逻辑
				let fullPath: string
				if (item.path?.startsWith('/')) {
					// 绝对路径
					fullPath = item.path
				} else {
					// 相对路径，需要拼接父路径
					const cleanParentPath = parentPath.replace(/\/$/, '') // 移除父路径末尾的斜杠
					fullPath = cleanParentPath + '/' + (item.path || '')
				}

				const node: TreeNode = {
					title: item.meta.title,
					icon: item.meta.icon,
					type: item.meta.type,
					path: fullPath,
					externalUrl: item.meta.externalUrl
				}
				if (item.children?.length) {
					node.children = build(item.children, fullPath)
				}
				return node
			})
	}
	return build(menuList)
}

// 打开搜索框
const openSearch = () => {
	Object.assign(searchState, initState())
	searchState.visible = true
	searchState.searchItemList = buildMenuTree(userStore.menuList)
	searchState.matchItemList = searchState.searchItemList
}

// 搜索逻辑（防抖）
const debouncedQuerySearch = useDebounceFn((queryString?: string) => {
	const raw = queryString !== undefined ? queryString : searchState.keyword
	if (!raw || !raw.trim()) {
		searchState.matchItemList = searchState.searchItemList
		return
	}

	const keyword = raw.toLowerCase()

	const filterTree = (nodes: TreeNode[]): TreeNode[] => {
		return nodes
			.map(node => {
				const children = node.children ? filterTree(node.children) : []
				if (node.title.toLowerCase().includes(keyword) || children.length > 0) {
					return { ...node, children }
				}
				return null
			})
			.filter(Boolean) as TreeNode[]
	}

	searchState.matchItemList = filterTree(searchState.searchItemList)
}, 150)

const querySearch = (queryString?: string) => debouncedQuerySearch(queryString)

// 事件
const onDialogOpened = async () => {
	await nextTick()
	searchInputRef.value?.focus()
	scrollbarRef.value?.setScrollTop(0)
}

// 选择树节点
const handleSelect = (node: TreeNode) => {
	searchState.visible = false
	// 如果是菜单或者iframe，则路由跳转
	if ([1, 3].includes(node.type)) {
		if (node.path != route.path) {
			router.push(node.path!)
		}
	} else {
		// 如果是外链
		window.open(node.externalUrl!)
	}
	if (node.path != userStore.activeMenuPath) {
		userStore.setActiveMenuPath(node.path!)
	}
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
	padding-left: 8px;
}
.search-input {
	margin-bottom: 6px;
}
.no-results {
	padding: 16px 0;
	text-align: center;
	color: #8c8c8c;
}
</style>
