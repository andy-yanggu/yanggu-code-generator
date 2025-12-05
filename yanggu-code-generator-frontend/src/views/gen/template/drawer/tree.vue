<template>
	<div style="margin-bottom: 10px; gap: 20px; display: flex; justify-content: center; align-items: center">
		<el-input v-model="treeSearchText" placeholder="请输入目录/文件名称" size="small" clearable prefix-icon="Search" style="width: 200px"></el-input>
		<div style="display: flex; text-align: right">
			<el-button size="small" type="primary" :icon="Refresh" @click="refreshTree()">刷新</el-button>
			<el-button size="small" type="danger" :icon="Delete" :disabled="checkedNodeListLength === 0" @click="deleteCheckedNode()"> 删除 </el-button>
		</div>
	</div>
	<el-scrollbar style="height: calc(100% - 40px); overflow-x: auto" @contextmenu.prevent.stop="handleScrollWrapperRightClick">
		<div class="tree-scroll-wrapper">
			<el-tree
				ref="treeRef"
				:data="templateTreeData.treeList"
				node-key="id"
				:current-node-key="templateTreeData.activeItemId"
				highlight-current
				show-checkbox
				class="custom-tree"
				:props="{ label: 'fileName' }"
				:filter-node-method="filterNode"
				draggable
				:allow-drag="treeAllowDrag"
				:allow-drop="treeAllowDrop"
				:default-expanded-keys="expandedKeys"
				@node-click="handleTreeNodeClick"
				@node-contextmenu="handleNodeRightClick"
				@node-drop="handleNodeDrop"
				@node-expand="handleNodeExpand"
				@node-collapse="handleNodeCollapse"
			>
				<template #default="{ node, data }">
					<el-tooltip :content="data.templateDesc" placement="top" :disabled="!data.templateDesc">
						<div class="custom-tree-node">
							<svg-icon :icon="getIcon(node, data)"></svg-icon>
							<span class="tree-node-label">
								{{ node.label }}
								<span v-if="data.isEdited" class="edit-dot"></span>
							</span>
							<div class="tree-node-actions">
								<el-icon class="edit-icon" @click.stop="updateTemplate(data)">
									<Edit></Edit>
								</el-icon>
							</div>
						</div>
					</el-tooltip>
				</template>
				<template #empty>
					<el-empty>
						<template #description>
							<div style="display: flex; flex-direction: column; align-items: center; justify-content: center">
								<el-row>
									<el-text size="large" tag="b">暂无数据</el-text>
								</el-row>
								<el-row>
									<el-text size="large" tag="b">请在空白处点击右键新建目录、模板文件或者二进制文件</el-text>
								</el-row>
							</div>
						</template>
					</el-empty>
				</template>
			</el-tree>
		</div>
	</el-scrollbar>
	<!-- 自定义模板右键菜单 -->
	<div v-if="contextMenu.visible">
		<ul class="context-menu" :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }">
			<!-- 节点右键 -->
			<template v-if="contextMenu.isTree">
				<li v-if="contextMenu.nodeData.id" @click="updateTemplate(contextMenu.nodeData)"><svg-icon icon="icon-edit"></svg-icon>修改</li>
				<template v-if="contextMenu.nodeData.templateType === 0">
					<li @click="newDir(contextMenu.nodeData)"><svg-icon icon="icon-folder-add"></svg-icon>新建目录</li>
					<li @click="newTemplateFile(contextMenu.nodeData)"><svg-icon icon="icon-file-add"></svg-icon>新建模板文件</li>
					<li @click="newBinaryFile(contextMenu.nodeData)"><svg-icon icon="icon-file-unknown"></svg-icon>新建二进制文件</li>
				</template>
			</template>
			<!-- 空白右键 -->
			<template v-else>
				<li @click="newDir(contextMenu.nodeData)"><svg-icon icon="icon-folder-add"></svg-icon>新建目录</li>
				<li @click="newTemplateFile(contextMenu.nodeData)"><svg-icon icon="icon-file-add"></svg-icon>新建模板文件</li>
				<li @click="newBinaryFile(contextMenu.nodeData)"><svg-icon icon="icon-file-unknown"></svg-icon>新建二进制文件</li>
			</template>
		</ul>
	</div>
</template>
<script setup lang="ts">
import { Delete, Edit, Refresh } from '@element-plus/icons-vue'
import SvgIcon from '@/components/svg-icon'
import { computed, nextTick, reactive, ref, watch } from 'vue'
import Sortable from 'sortablejs'
import { useFullscreen } from '@vueuse/core'
import { Action, ElLoading, ElMessage, ElMessageBox } from 'element-plus'
import { genTemplateApi } from '@/api/gen/template'

const treeRef = ref()
const templateTestRef = ref()
const tabsRef = ref()
const codeScrollbarRef = ref()
const templateTreeData = reactive({
	visible: false,
	treeList: [] as Tree[],
	activeItemId: -1,
	tabList: [] as Tree[]
})

// 初始化tab拖拽效果
watch(
	() => templateTreeData.tabList.length > 0,
	_ => {
		nextTick(() => {
			initTabSortable()
		})
	}
)

// 监听树节点变化，实现节点自动滚动到可视区域内
watch(
	() => templateTreeData.activeItemId,
	id => {
		// 等待 el-tree 渲染完成
		nextTick(() => {
			const dom = treeRef.value.$el.querySelector(`[data-key="${id}"]`)
			if (dom) {
				dom.scrollIntoView({
					behavior: 'smooth',
					inline: 'center',
					block: 'nearest'
				})
			}

			codeScrollbarRef.value?.scrollTo({
				top: 0,
				behavior: 'smooth'
			})
		})
	}
)

const checkedNodeListLength = computed(() => treeRef.value?.getCheckedKeys().length)

// 实现tab的拖拽排序
const initTabSortable = () => {
	if (!tabsRef.value) {
		return
	}

	const tabsWrapper = tabsRef.value.$el.querySelector('.el-tabs__nav') as HTMLElement
	if (!tabsWrapper) {
		return
	}

	new Sortable(tabsWrapper, {
		animation: 200,
		handle: '.tab-label', // 拖拽手柄
		filter: '.el-icon', // 排除关闭按钮
		preventOnFilter: true,
		onEnd: evt => {
			// el-tab会有一个高亮的样式需要排除掉
			const oldIndex = evt.oldIndex! - 1
			const newIndex = evt.newIndex! - 1
			// 从原数组中取出移动的 tab
			const movedTab = templateTreeData.tabList.splice(oldIndex, 1)[0]
			// console.log('oldIndex:', oldIndex, 'newIndex:', newIndex, 'movedTab:', movedTab)
			// 插入到新位置
			templateTreeData.tabList.splice(newIndex, 0, movedTab)
			// 设置当前激活的tab
			if (movedTab.id != templateTreeData.activeItemId) {
				templateTreeData.activeItemId = movedTab.id
			}
		}
	})
}

const getElementFromTabList = (id: number): Tree | undefined => {
	if (templateTreeData.tabList.length === 0 || id === -1) {
		return { id: -1, templateContent: '', templateType: -1 } as Tree
	} else {
		return templateTreeData.tabList.find(item => item.id === id)
	}
}

const activeTabItem = computed(() => {
	const treeData = findByIdFromTree(templateTreeData.activeItemId, templateTreeData.treeList)
	if (!treeData) {
		return { id: -1, templateContent: '', templateType: -1 } as Tree
	} else {
		return treeData
	}
})

const treeSearchText = ref('')
const isCollapse = ref(false)
const { isFullscreen, toggle } = useFullscreen()
// 模板树右键菜单状态
const contextMenu = reactive({
	visible: false,
	x: 0,
	y: 0,
	isTree: true,
	templateType: -1,
	templatePath: '',
	parentId: 0,
	nodeData: {} as Tree
})

// tab右键菜单状态
const tabContextMenu = reactive({
	visible: false,
	x: 0,
	y: 0,
	index: -1,
	item: {} as Tree
})

const submitLoading = ref(false)

const imageTypeList = ref(['png', 'jpg', 'jpeg', 'gif', 'svg', 'bmp', 'git', 'ico'])

const fullFilePath = computed(() => {
	return getFullPathById(templateTreeData.activeItemId, templateTreeData.treeList)
})

const expandedKeys = ref<number[]>([])

// 节点展开时，把 id 加进 expandedKeys
const handleNodeExpand = (data: Tree) => {
	if (!expandedKeys.value.includes(data.id)) {
		expandedKeys.value.push(data.id)
	}
}

// 节点折叠时，递归移除自己和所有后代 id
const handleNodeCollapse = (data: Tree) => {
	const node = treeRef.value.getNode(data.id)
	node.expanded = false
	data.children?.forEach(child => {
		handleNodeCollapse(child)
	})
	const collectIds = (node: Tree): number[] => [node.id, ...(node.children?.flatMap(collectIds) || [])]
	const removeIds = collectIds(data)
	expandedKeys.value = expandedKeys.value.filter(id => !removeIds.includes(id))
}

// 初始化方法
const init = async () => {
	isCollapse.value = false
	const loadingInstance = ElLoading.service({ fullscreen: true })
	try {
		const data = await genTemplateApi.treeData(props.templateGroupId)
		templateTreeData.treeList = data
		buildTree(templateTreeData.treeList)
		const templateContentList = buildFileList(data)
		templateTreeData.visible = true
		await nextTick()

		// 恢复展开状态
		expandedKeys.value.forEach(key => {
			const node = treeRef.value.getNode(key)
			if (node) {
				node.expanded = true
			}
		})

		if (templateContentList.length > 0 && templateTreeData.activeItemId === -1) {
			templateTreeData.activeItemId = templateContentList[0].id
			expandedKeys.value.push(templateContentList[0].id)
			tabPush(templateContentList[0])
		}
		// 更新一下tab中的数据，已新加载数据为准
		refreshData(templateContentList)
	} finally {
		loadingInstance.close()
	}
}

const findByIdFromTree = (id: number, treeList: Tree[]): Tree | undefined => {
	for (const tree of treeList) {
		if (tree.id === id) {
			return tree
		}
		if (tree.children && tree.children.length > 0) {
			const tempTree = findByIdFromTree(id, tree.children!)
			if (tempTree) {
				return tempTree
			}
		}
	}
	return undefined
}

// 全量刷新树数据，回到初始状态
const refreshTree = () => {
	ElMessageBox.confirm('刷新将丢失所有未保存的编辑内容及界面状态（包括节点展开状态、搜索条件等），确定要刷新吗？', '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			// 清空搜索条件
			treeSearchText.value = ''

			templateTreeData.activeItemId = -1

			// 清空所有tab
			templateTreeData.tabList = []

			// 清空所有展开状态
			expandedKeys.value = []

			// 重新加载数据
			init()
		})
		.catch(() => {
			ElMessage.info('刷新已取消')
		})
}

// 监听：激活 id + 内容 + 类型
watch(
	() => [activeTabItem.value.id, activeTabItem.value.templateContent, activeTabItem.value.templateType],
	([id, content, type], [prevId, prevContent]) => {
		// 1) 只处理可编辑的文本模板
		if (type !== 1) {
			return
		}

		const tab = getElementFromTabList(id as number)
		if (!tab) {
			return
		}

		// 2) 如果当前内容 == 原始内容 → 取消编辑状态
		if (content === tab.originalTemplateContent) {
			tab.isEdited = false
			activeTabItem.value.isEdited = false
			return
		}

		// 3) 只有在同一个 id 下内容发生变化才认为是编辑
		if (id === prevId && content !== prevContent) {
			tab.isEdited = true
			activeTabItem.value.isEdited = true
		}
	}
)

// 关闭抽屉方法
const handleClose = (done: () => void) => {
	const editTabs: Tree[] = []
	// 递归遍历treeList，找出isEdited为true的节点
	const findAndClose = (nodes: Tree[]) => {
		for (const node of nodes) {
			if (node.isEdited) {
				editTabs.push(node)
			}
			if (node.children) {
				findAndClose(node.children)
			}
		}
	}
	findAndClose(templateTreeData.treeList)
	if (editTabs.length === 0) {
		done()
		return
	}
	const fileNameJoin = joinFileName(editTabs)
	const message = `${fileNameJoin}已修改未保存，是否保存后再关闭？`
	ElMessageBox.confirm(message, '提示', {
		distinguishCancelAndClose: true,
		confirmButtonText: '是(Y)',
		cancelButtonText: '否(N)',
		type: 'warning'
	})
		.then(() => {
			// 保存修改后关闭
			const dataFormList = editTabs.map(tab => {
				return {
					id: tab.id,
					templateContent: tab.templateContent
				}
			})
			genTemplateApi.updateContentList(dataFormList).then(() => {
				ElMessage.success({
					message: '保存成功',
					duration: 500,
					onClose: () => {
						done()
					}
				})
			})
		})
		.catch((action: Action) => {
			// 丢弃修改直接关闭
			if (action === 'cancel') {
				done()
			}
		})
}

// 递归查找并拼接路径
const getFullPathById = (id: number, nodes: Tree[], parentPath = ''): string => {
	for (const node of nodes) {
		const currentPath = parentPath ? `${parentPath}/${node.fileName}` : node.fileName
		if (node.id === id) {
			return currentPath
		}
		if (node.children?.length) {
			const result = getFullPathById(id, node.children, currentPath)
			if (result) {
				return result
			}
		}
	}
	return ''
}

// 构建树
const buildTree = (treeList: Tree[]) => {
	treeList.forEach(treeItem => {
		if (treeItem.templateType === 1) {
			treeItem.originalTemplateContent = treeItem.templateContent
		}
		if (treeItem.children && treeItem.children.length > 0) {
			buildTree(treeItem.children)
		}
	})
}

// 找到所有的模板文件和二进制文件
const buildFileList = (treeList: Tree[]) => {
	const templateContentList: Tree[] = []
	treeList.forEach(treeItem => {
		// 找到模板文件和二进制文件
		if (treeItem.templateType != 0) {
			templateContentList.push(treeItem)
		} else if (treeItem.children && treeItem.children.length > 0) {
			templateContentList.push(...buildFileList(treeItem.children))
		}
	})
	return templateContentList
}

const toggleCollapse = () => {
	isCollapse.value = !isCollapse.value
}

watch(treeSearchText, val => {
	treeRef.value!.filter(val)
})

// 过滤树节点
const filterNode = (value: string, data: Tree) => {
	if (!value) {
		return true
	}
	return data.fileName.includes(value)
}

// tree点击
const handleTreeNodeClick = (data: Tree) => {
	// 只有是文件才可以预览
	if (data.templateType != 0) {
		templateTreeData.activeItemId = data.id
		tabPush(data)
	}
}

// 删除勾选的树节点
const deleteCheckedNode = () => {
	const allCheckedKeys = treeRef.value.getCheckedKeys()
	if (allCheckedKeys.length === 0) {
		ElMessage.warning('请勾选目录或者文件')
		return
	}
	ElMessageBox.confirm('此操作将删除目录或者文件, 是否继续?', '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			genTemplateApi
				.deleteList(allCheckedKeys)
				.then(() => {
					ElMessage.success('删除成功')
				})
				.then(() => {
					// 删除tab
					allCheckedKeys.forEach((tempId: number) => {
						handleTabRemove(tempId)
						expandedKeys.value = expandedKeys.value.filter(id => id !== tempId)
					})
				})
				.then(() => {
					init()
				})
		})
		.catch(() => {
			ElMessage.info('已取消删除')
		})
}

// 判断树节点能否被拖拽
const treeAllowDrag = (draggingNode: any) => {
	// console.log('节点允许被拖拽:', draggingNode.data)
	// 可以根据需要设置哪些节点可以拖拽
	// 这里允许所有节点拖拽
	return true
}

// 判断树节点是否允许放置
const treeAllowDrop = (draggingNode: any, dropNode: any, type: 'prev' | 'next' | 'inner') => {
	const dragData = draggingNode.data
	const dropData = dropNode.data
	// console.log('节点是否允许放置:', dragData, dropData, type)

	// 1. inner 必须是目录（templateType === 0）
	if (type === 'inner') {
		// 如果父节点没变，不允许放置。树节点顺序是后端根据类型和名称排序，不是节点自有的顺序
		if (dragData.parentId === dropData.id) {
			return false
		}
		return dropData.templateType === 0
	} else if (type === 'prev' || type === 'next') {
		// 2. prev / next 永远允许同级拖拽，但父节点没变化则禁止
		return dragData.parentId !== dropData.parentId
	}

	return false
}

// 处理节点拖拽完成事件
const handleNodeDrop = (draggingNode: any, dropNode: any, dropType: 'before' | 'after' | 'inner', ev: Event) => {
	const dragData = draggingNode.data
	const dropData = dropNode.data
	// console.log('节点拖拽完成:', dragData, dropData, dropType, ev)

	// 计算新的 parentId
	let newParentId = dragData.parentId

	if (dropType === 'inner') {
		// 拖入目录内部
		newParentId = dropData.id
	} else if (dropType === 'before' || dropType === 'after') {
		// 和目标节点同级
		newParentId = dropData.parentId
	}

	if (newParentId === dragData.parentId) {
		ElMessage.warning('不能移动到自身')
		return
	}

	// 构造请求参数
	const dataForm = {
		dragNodeId: dragData.id,
		newParentId: newParentId
	}

	// console.log('拖拽完成:', dataForm)

	// 调用 API 更新位置
	genTemplateApi
		.updateParent(dataForm)
		.then(() => {
			ElMessage.success('移动成功')
			// 激活被拖拽的树节点
			templateTreeData.activeItemId = dragData.id
			// 加入到tab中
			tabPush(dragData)
			init() // 重新加载数据
		})
		.catch(() => {
			ElMessage.error('移动失败')
			init() // 恢复原状
		})
}
</script>

<style scoped></style>
