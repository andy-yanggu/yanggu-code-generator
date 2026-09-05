<template>
	<div class="preference-section">
		<el-text size="small" type="info">基于服务端默认值进行个性化覆盖</el-text>

		<!-- 搜索过滤 -->
		<el-input v-model="keyword" placeholder="请输入菜单名称" clearable :prefix-icon="Search" class="search-input" size="small"></el-input>

		<!-- 菜单树 -->
		<el-scrollbar max-height="500px">
			<template v-if="filteredTree.length > 0">
				<template v-for="node in filteredTree" :key="node.path">
					<!-- 目录：可折叠 -->
					<el-collapse v-if="node.children.length > 0" v-model="expandedDirs" expand-icon-position="left" class="dir-collapse">
						<el-collapse-item :title="node.title" :name="node.path">
							<template #title>
								<div class="dir-title">
									<icon-text-tooltip :icon="node.icon ?? ''" :title="node.title" :max-width="'300px'"></icon-text-tooltip>
								</div>
							</template>
							<!-- 渲染子项 -->
							<template v-for="child in node.children" :key="child.path">
								<!-- 子目录：嵌套折叠 -->
								<el-collapse v-if="child.children.length > 0" v-model="expandedDirs" class="dir-collapse sub-dir">
									<el-collapse-item :title="child.title" :name="child.path">
										<template #title>
											<div class="dir-title">
												<svg-icon v-if="child.icon" :icon="child.icon"></svg-icon>
												<el-text>{{ child.title }}</el-text>
											</div>
										</template>
										<menu-preference-item
											v-for="grandChild in child.children"
											:key="grandChild.path"
											:path="grandChild.path"
											:title="grandChild.title"
											:icon="grandChild.icon"
											:server-cache="grandChild.serverCache"
											:server-hide-menu="grandChild.serverHideMenu"
											:server-hide-tab="grandChild.serverHideTab"></menu-preference-item>
									</el-collapse-item>
								</el-collapse>
								<!-- 叶子菜单 -->
								<menu-preference-item
									v-else
									:path="child.path"
									:title="child.title"
									:icon="child.icon"
									:server-cache="child.serverCache"
									:server-hide-menu="child.serverHideMenu"
									:server-hide-tab="child.serverHideTab"></menu-preference-item>
							</template>
						</el-collapse-item>
					</el-collapse>
					<!-- 叶子菜单：直接展示 -->
					<menu-preference-item
						v-else
						:path="node.path"
						:title="node.title"
						:icon="node.icon"
						:server-cache="node.serverCache"
						:server-hide-menu="node.serverHideMenu"
						:server-hide-tab="node.serverHideTab"></menu-preference-item>
				</template>
			</template>
			<div v-else class="no-results">
				<el-text type="info" size="small">无匹配菜单</el-text>
			</div>
		</el-scrollbar>

		<!-- 重置按钮 -->
		<div class="reset-area">
			<el-button :icon="RefreshLeft" size="small" @click="handleReset">恢复所有默认</el-button>
		</div>
	</div>
</template>

<script setup lang="ts">
import { RefreshLeft, Search } from '@element-plus/icons-vue'
import { useMenuPreferenceStore, useUserStore } from '@/store'
import { MenuInfo } from '@/types'
import MenuPreferenceItem from '@/layout/navbar/components/menu-preference-item.vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import IconTextTooltip from '@/components/icon-text-tooltip/index.vue'

defineOptions({
	name: 'MenuPreferenceSection'
})

// 偏好树节点
interface PreferenceTreeNode {
	path: string
	title: string
	icon: string
	type: number
	serverCache: boolean
	serverHideMenu: boolean
	serverHideTab: boolean
	children: PreferenceTreeNode[]
}

const keyword = ref('')
const expandedDirs = ref<string[]>([])
const userStore = useUserStore()
const menuPreferenceStore = useMenuPreferenceStore()

// 构建偏好树（不过滤 hideMenu，只过滤 type === 2）
const buildPreferenceTree = (menuList: MenuInfo[], parentPath: string = ''): PreferenceTreeNode[] => {
	return menuList
		.filter(item => item.meta.type !== 2)
		.map(item => {
			// 处理路径
			let fullPath: string
			if (item.path?.startsWith('/')) {
				fullPath = item.path
			} else {
				const cleanParentPath = parentPath.replace(/\/$/, '')
				fullPath = cleanParentPath + '/' + (item.path || '')
			}

			const node: PreferenceTreeNode = {
				path: fullPath,
				title: item.meta.title,
				icon: item.meta.icon || '',
				type: item.meta.type,
				serverCache: item.meta.cache ?? false,
				serverHideMenu: item.meta.hideMenu ?? false,
				serverHideTab: item.meta.hideTab ?? false,
				children: []
			}

			if (item.children?.length) {
				node.children = buildPreferenceTree(item.children, fullPath)
			}

			return node
		})
}

// 完整树
const fullTree = computed(() => buildPreferenceTree(userStore.menuList))

// 过滤后的树（按关键词匹配）
const filteredTree = computed(() => {
	const raw = keyword.value.trim()
	if (!raw) {
		return fullTree.value
	}

	const kw = raw.toLowerCase()

	const filterNode = (node: PreferenceTreeNode): PreferenceTreeNode | null => {
		const filteredChildren = node.children.map(filterNode).filter(Boolean) as PreferenceTreeNode[]

		if (node.title.toLowerCase().includes(kw) || filteredChildren.length > 0) {
			return { ...node, children: filteredChildren }
		}
		return null
	}

	return fullTree.value.map(filterNode).filter(Boolean) as PreferenceTreeNode[]
})

// 搜索时自动展开包含匹配项的目录
watch(keyword, () => {
	if (keyword.value.trim()) {
		// 展开所有目录
		const collectDirPaths = (nodes: PreferenceTreeNode[]): string[] => {
			const paths: string[] = []
			for (const node of nodes) {
				if (node.children.length > 0) {
					paths.push(node.path)
					paths.push(...collectDirPaths(node.children))
				}
			}
			return paths
		}
		expandedDirs.value = collectDirPaths(filteredTree.value)
	} else {
		expandedDirs.value = []
	}
})

// 重置所有偏好
const handleReset = () => {
	menuPreferenceStore.resetAll()
	ElMessage.success('菜单偏好已恢复默认')
}
</script>

<style scoped>
.preference-section {
	padding: 0 4px;
}

.search-input {
	margin: 10px 0;
}

.dir-collapse {
	margin-bottom: 4px;
	border: 1px solid var(--el-border-color-lighter);
	border-radius: 6px;
}

:deep(.el-collapse-item__header) {
	padding-left: 12px;
	height: 40px;
	line-height: 40px;
	background-color: var(--el-fill-color-lighter);
	border-radius: 6px 6px 0 0;
}

:deep(.el-collapse-item__wrap) {
	padding: 8px;
}

:deep(.el-collapse-item__content) {
	display: flex;
	flex-direction: column;
	gap: 4px;
	padding-bottom: 0px;
}

.dir-title {
	display: flex;
	align-items: center;
	gap: 6px;
}

.sub-dir {
	margin: 4px 0 4px 8px;
}

.no-results {
	padding: 20px 0;
	text-align: center;
}

.reset-area {
	display: flex;
	justify-content: center;
	margin-top: 12px;
	padding-top: 12px;
	border-top: 1px solid var(--el-border-color-extra-light);
}
</style>
