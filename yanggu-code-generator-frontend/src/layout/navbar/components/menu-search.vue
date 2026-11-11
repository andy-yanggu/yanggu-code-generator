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

			<!-- 树形结构展示：默认全部展开，命中关键词的片段由高亮组件分段渲染 -->
			<el-scrollbar ref="scrollbarRef" max-height="400px" class="search-results">
				<el-tree
					:data="searchState.matchItemList"
					node-key="path"
					:props="{ label: 'title', children: 'children' }"
					:indent="16"
					default-expand-all
					empty-text="无匹配结果"
					class="menu-search-tree"
					@node-click="handleNodeClick">
					<template #default="{ data }">
						<div class="menu-tree-node">
							<svg-icon v-if="data.icon" :icon="data.icon"></svg-icon>
							<menu-tree-label :title="data.title" :keyword="searchState.keyword"></menu-tree-label>
						</div>
					</template>
				</el-tree>
			</el-scrollbar>
		</el-dialog>
	</div>
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'
import { useMenuPreferenceStore, useUserStore } from '@/store'
import { MenuInfo, RouteType } from '@/types'
import MenuTreeLabel from '@/layout/navbar/components/menu-tree-label.vue'
import IconButton from '@/components/icon-button/index.vue'
import SvgIcon from '@/components/svg-icon/index.vue'

defineOptions({
	name: 'MenuSearch'
})

/** 菜单搜索树节点，由 userStore.menuList 递归构建，仅本组件消费 */
interface TreeNode {
	title: string
	icon?: string
	path?: string
	type: RouteType
	externalUrl?: string
	children?: TreeNode[]
}

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
const menuPreferenceStore = useMenuPreferenceStore()
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

				const { title } = menuPreferenceStore.getEffective(fullPath, item.meta)
				const node: TreeNode = {
					title,
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

	// trim 与高亮分段保持一致，否则尾部空格会出现过滤有结果、高亮不命中
	const keyword = raw.trim().toLowerCase()

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

// 目录行交给 el-tree 自身切换展开/收起，只有可跳转的节点才执行 handleSelect。
// 这里按 type 而不按 children 判定：filterTree 会把命中目录的 children 置为空数组，
// 此时目录被当成叶子就会落到 window.open(undefined) 上。
const handleNodeClick = (data: TreeNode) => {
	if (data.type === RouteType.DIRECTORY) {
		return
	}
	handleSelect(data)
}

// 选择树节点
const handleSelect = (node: TreeNode) => {
	searchState.visible = false
	// 如果是菜单或者iframe，则路由跳转
	if ([RouteType.MENU, RouteType.IFRAME].includes(node.type)) {
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
}
.search-input {
	margin-bottom: 6px;
}
.menu-search-tree {
	display: block;
	/* 行高与 hover 底色走 EP 自己的变量，不直接覆盖 .el-tree-node__content 的 height */
	--el-tree-node-content-height: 32px;
	--el-tree-node-hover-bg-color: var(--el-color-primary-light-9);
}
.menu-tree-node {
	display: flex;
	align-items: center;
	gap: 5px;
	flex: 1;
	min-width: 0;
}
:deep(.el-tree-node__content) {
	border-radius: 4px;
}
</style>
