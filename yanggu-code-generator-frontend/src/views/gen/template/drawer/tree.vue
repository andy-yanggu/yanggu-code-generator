<template>
	<el-row style="margin-bottom: 10px" :gutter="10">
		<el-col :span="14">
			<el-input v-model="treeSearchText" placeholder="请输入目录/文件名称" size="small" clearable prefix-icon="Search" style="width: 100%"></el-input>
		</el-col>
		<el-col :span="10" style="display: flex; justify-content: center; align-items: center">
			<el-button size="small" type="primary" :icon="Refresh" @click="refreshTree()">刷新</el-button>
			<el-button size="small" type="danger" :icon="Delete" :disabled="checkedNodeListLength === 0" @click="deleteCheckedNode()"> 删除 </el-button>
		</el-col>
	</el-row>
	<el-scrollbar style="height: calc(100% - 40px); overflow-x: auto" @contextmenu.prevent.stop="handleScrollWrapperRightClick">
		<div class="tree-scroll-wrapper">
			<el-tree
				ref="treeRef"
				:data="templateTreeData.treeList"
				node-key="id"
				:current-node-key="templateTreeData.activeItemId"
				highlight-current
				show-checkbox
				:check-on-click-leaf="false"
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
							<!-- 修改时添加小红点 -->
							<el-badge is-dot :hidden="!data.isEdited">
								<div style="display: flex; align-items: center; justify-content: center; gap: 5px">
									<svg-icon :icon="getIcon(node, data)"></svg-icon>
									{{ node.label }}
								</div>
							</el-badge>
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
	<!-- 新增和修改页面	-->
	<template-form
		ref="formRef"
		:template-group-id="templateGroupId"
		:parent-id="contextMenu.parentId"
		:template-type="contextMenu.templateType"
		:template-path="contextMenu.templatePath"
		@refresh-data-list="initAfterHandler"
	></template-form>
</template>
<script setup lang="ts">
import { Delete, Edit, Refresh } from '@element-plus/icons-vue'
import TemplateForm from '@/views/gen/template/form.vue'
import { computed, nextTick, reactive, Ref, ref, watch } from 'vue'
import { Action, ElLoading, ElMessageBox } from 'element-plus'
import SvgIcon from '@/components/svg-icon/index'
import { useEventListener } from '@vueuse/core'
import { ElMessage } from 'element-plus/es'
import { useInitForm } from '@/hooks/use-init-form'
import { genTemplateApi } from '@/api'

defineOptions({
	name: 'TemplateDrawerTree'
})

interface Tree {
	// 主键ID
	id: number
	// 父级ID
	parentId: number
	// 模板名称
	templateName: string
	// 文件名称
	fileName: string
	// 描述
	templateDesc?: string
	// 模板类型（0-目录，1-模板文件，2-二进制文件）
	templateType: number
	// 原始内容（用于恢复）
	originalTemplateContent?: string
	// 模板内容
	templateContent?: string
	// 二进制原始文件名
	binaryOriginalFileName?: string
	// 子节点列表
	children?: Tree[]
	// 新增编辑状态属性
	isEdited?: boolean // 标记是否已编辑但未保存
}

const props = defineProps({
	templateGroupId: {
		type: Number,
		required: true
	},
	templateGroupName: {
		type: String,
		required: true
	},
	templateGroupType: {
		type: Number,
		required: true
	}
})

const treeRef = ref()
const tabsRef = ref()
const templateTreeData = reactive({
	visible: false,
	treeList: [] as Tree[],
	activeItemId: -1,
	tabList: [] as Tree[]
})

// 监听树节点变化，实现节点自动滚动到可视区域内
watch(
	() => templateTreeData.activeItemId,
	id => {
		// 等待 el-tree 渲染完成
		nextTick(() => {
			const dom = treeRef.value.$el.querySelector(`[data-key="${id}"]`)
			if (dom) {
				// 树节点滚动到可视区域
				dom.scrollIntoView({
					behavior: 'smooth',
					inline: 'center',
					block: 'nearest'
				})
			}
		})
	}
)

const checkedNodeListLength = computed(() => treeRef.value?.getCheckedKeys().length)

const getElementFromTabList = (id: number): Tree | undefined => {
	if (templateTreeData.tabList.length === 0 || id === -1) {
		return { id: -1, templateContent: '', templateType: -1 } as Tree
	} else {
		return templateTreeData.tabList.find(item => item.id === id)
	}
}

const activeTabItem: Ref<Tree> = computed(() => {
	const treeData = findByIdFromTree(templateTreeData.activeItemId, templateTreeData.treeList)
	if (!treeData) {
		return { id: -1, templateContent: '', templateType: -1 } as Tree
	} else {
		return treeData
	}
})

const treeSearchText = ref('')
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

const imageTypeList = ref(['png', 'jpg', 'jpeg', 'gif', 'svg', 'bmp', 'git', 'ico'])

const expandedKeys = ref([] as number[])

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
			expandedKeys.value.push(templateContentList[0].parentId)
			tabPush(templateContentList[0])
		}
		// 更新一下tab中的数据，已新加载数据为准
		refreshTabData(templateContentList)
	} finally {
		loadingInstance.close()
	}
}

// 只刷新数据，不改变其他状态
const initData = async () => {
	const data = await genTemplateApi.treeData(props.templateGroupId)
	templateTreeData.treeList = data
	buildTree(templateTreeData.treeList)
	const templateContentList = buildFileList(data)
	// 更新一下tab中的数据，已新加载数据为准
	refreshTabData(templateContentList)
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
						// 考虑删除tab
						// handleTabRemove(tempId)
						expandedKeys.value = expandedKeys.value.filter(id => id !== tempId)
					})
				})
				.then(() => {
					initData()
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
	genTemplateApi.updateParent(dataForm).then(() => {
		ElMessage.success('移动成功')
		// 激活被拖拽的树节点
		templateTreeData.activeItemId = dragData.id
		// 加入到tab中
		tabPush(dragData)
		initData() // 重新加载数据
	})
}

const refreshTabData = (dataList: Tree[]) => {
	if (dataList.length === 0) {
		templateTreeData.tabList = []
		return
	}
	const tabLength = templateTreeData.tabList.length
	if (tabLength === 0) {
		return
	}
	// 保存当前的编辑状态
	const editStatusSet = new Set<number>()
	templateTreeData.tabList.forEach(tab => {
		if (tab.isEdited) {
			editStatusSet.add(tab.id)
		}
	})

	for (let index = 0; index < tabLength; index++) {
		const oldData = templateTreeData.tabList[index]
		const tabElementId = oldData.id
		const newData = dataList.find(item => item.id === tabElementId)!
		if (newData) {
			templateTreeData.tabList.splice(index, 1, newData)
		}
		if (editStatusSet.has(tabElementId)) {
			templateTreeData.tabList[index].templateContent = oldData.templateContent
			templateTreeData.tabList[index].isEdited = true
		}
	}
}

const hideContextMenu = () => {
	contextMenu.visible = false
	document.removeEventListener('click', hideContextMenu)
}

// 节点右键
const handleNodeRightClick = (event: MouseEvent, data: Tree) => {
	event.preventDefault()
	contextMenu.visible = true
	contextMenu.isTree = true
	contextMenu.nodeData = data
	contextMenu.templatePath = getFullPathById(data.id, templateTreeData.treeList)

	nextTick(() => {
		calculateTemplateMenu(event)
	})

	document.addEventListener('click', hideContextMenu)
}

// 空白右键
const handleScrollWrapperRightClick = (event: MouseEvent) => {
	const target = event.target as HTMLElement
	if (target.closest('.custom-tree-node')) {
		return
	}

	contextMenu.visible = true
	contextMenu.isTree = false
	contextMenu.nodeData = { id: 0 } as Tree
	contextMenu.parentId = 0
	contextMenu.templatePath = '/'
	nextTick(() => {
		calculateTemplateMenu(event)
	})

	document.addEventListener('click', hideContextMenu)
}

// 计算模板菜单的位置，考虑在底部，展示不全的情况
const calculateTemplateMenu = (event: MouseEvent) => {
	const menuEl = document.querySelector('.context-menu') as HTMLElement
	if (!menuEl) {
		return
	}

	const viewportWidth = window.innerWidth
	const viewportHeight = window.innerHeight
	const menuWidth = menuEl.offsetWidth
	const menuHeight = menuEl.offsetHeight

	let x = event.clientX
	let y = event.clientY

	if (x + menuWidth > viewportWidth) {
		x = viewportWidth - menuWidth - 10
	}
	if (y + menuHeight > viewportHeight) {
		y = viewportHeight - menuHeight - 10
	}

	contextMenu.x = x
	contextMenu.y = y
}

document.addEventListener('mousedown', e => {
	if (contextMenu.visible) {
		// 如果点击不在菜单区域内，就隐藏
		const menuEl = document.querySelector('.context-menu')
		if (menuEl && !menuEl.contains(e.target as Node)) {
			hideContextMenu()
		}
	}
})

// 修改模板
const updateTemplate = (node: Tree) => {
	// console.log('编辑', node)
	contextMenu.templateType = node.templateType
	contextMenu.parentId = node.parentId
	contextMenu.nodeData = node
	const templatePath = getFullPathById(node.id, templateTreeData.treeList).replace(node.fileName, '').replace(/\/$/, '')
	contextMenu.templatePath = templatePath ? templatePath : '/'
	nextTick(() => {
		formInitHandle(node.id)
	})
	hideContextMenu()
}

// 新建目录
const newDir = (parent: Tree) => {
	// console.log('新建目录在', parent)
	contextMenu.templateType = 0
	contextMenu.parentId = parent.id
	nextTick(() => {
		formInitHandle()
	})
	hideContextMenu()
}

// 新建模板文件
const newTemplateFile = (parent: Tree) => {
	// console.log('新建模板文件在', parent)
	contextMenu.templateType = 1
	contextMenu.parentId = parent.id
	nextTick(() => {
		formInitHandle()
	})
	hideContextMenu()
}

// 新建二进制文件
const newBinaryFile = (parent: Tree) => {
	// console.log('新建二进制文件在', parent)
	contextMenu.templateType = 2
	contextMenu.parentId = parent.id
	nextTick(() => {
		formInitHandle()
	})
	hideContextMenu()
}

const initAfterHandler = (tree: Tree) => {
	tabPush(tree)
	nextTick(() => {
		initData()
	})
	if (tree.templateType === 0) {
		return
	}
	// 激活模板文件
	templateTreeData.activeItemId = tree.id
}

// 获取节点的图标
const getIcon = (node: any, data: Tree): string => {
	if (data.templateType === 0) {
		if (node.expanded) {
			return 'icon-folder-open'
		} else {
			return 'icon-folder'
		}
	} else if (data.templateType === 1) {
		return 'icon-file-text'
	} else if (data.templateType === 2) {
		const some = imageTypeList.value.some(item => data.binaryOriginalFileName?.endsWith(item))
		if (some) {
			return 'icon-file-image'
		} else {
			return 'icon-file-unknown'
		}
	} else {
		return 'icon-file-unknown'
	}
}

const hideTabContextMenu = () => {
	tabContextMenu.visible = false
	document.removeEventListener('click', hideTabContextMenu)
}

// 点击空白地方关闭 tab 菜单
document.addEventListener('mousedown', e => {
	if (tabContextMenu.visible) {
		const menuEl = document.querySelector('.context-menu') // 如果 tab 菜单和 tree 菜单共用 class
		if (menuEl && !menuEl.contains(e.target as Node)) {
			hideTabContextMenu()
		}
	}
})
// 标签页元素
const tabNavEl = computed(() => tabsRef.value?.tabNavRef?.tabListRef)

const showTabMenu = (e: MouseEvent, tab: Tree, index: number) => {
	e.preventDefault()
	tabContextMenu.index = index
	tabContextMenu.item = tab
	tabContextMenu.visible = true

	nextTick(() => {
		const menuEl = document.querySelector('.context-menu') as HTMLElement
		if (menuEl) {
			const windowWidth = window.innerWidth
			const windowHeight = window.innerHeight
			const menuWidth = menuEl.offsetWidth
			const menuHeight = menuEl.offsetHeight

			tabContextMenu.x = Math.min(e.clientX, windowWidth - menuWidth - 10)
			tabContextMenu.y = Math.min(e.clientY, windowHeight - menuHeight - 10)
		}
	})
}

// tab右键菜单
const onTabContextMenu = (e: MouseEvent) => {
	const item = (e.target as HTMLElement).closest('.el-tabs__item') as HTMLElement
	if (!item) {
		return
	}

	e.preventDefault()

	const tempId = Number.parseInt(item.id.replace(/^tab-/, ''))
	const index = templateTreeData.tabList.findIndex(t => t.id === tempId)
	if (index === -1) {
		return
	}

	showTabMenu(e, getElementFromTabList(tempId)!, index)
}

useEventListener(tabNavEl, 'contextmenu', onTabContextMenu)

// 添加tab（进行去重）
const tabPush = (tree: Tree) => {
	if (tree.templateType === 0) {
		return
	}
	const find = templateTreeData.tabList.find(tab => tab.id === tree.id)
	if (find) {
		return
	}
	templateTreeData.tabList.push(tree)
}

const joinFileName = (editTabs: Tree[]): string => {
	if (editTabs.length > 3) {
		return (
			editTabs
				.slice(0, 3)
				.map(tab => tab.fileName)
				.join('、') + `等${editTabs.length}个模板文件`
		)
	} else {
		return editTabs.map(tab => tab.fileName).join('、') + '模板文件'
	}
}

// 通用关闭处理
const handleCloseTabs = (toCloseTabs: Tree[], closeTab: () => void) => {
	const editTabs = toCloseTabs.filter(tab => tab.isEdited)

	if (editTabs.length === 0) {
		closeTab()
	} else {
		const fileNameJoin = joinFileName(editTabs)
		// 关闭确认
		const message = `${fileNameJoin}已修改未保存，是否保存后再关闭？`
		ElMessageBox.confirm(message, '提示', {
			distinguishCancelAndClose: true,
			confirmButtonText: '是(Y)',
			cancelButtonText: '否(N)',
			type: 'warning'
		})
			.then(() => {
				const dataFormList = editTabs.map(tab => {
					return {
						id: tab.id,
						templateContent: tab.templateContent
					}
				})
				genTemplateApi.updateContentList(dataFormList).then(() => {
					ElMessage.success('保存成功')
					editTabs.forEach(tab => {
						tab.isEdited = false
						tab.originalTemplateContent = tab.templateContent
					})
					nextTick(() => {
						// 等待视图更新完成
						closeTab()
					})
				})
			})
			.catch((action: Action) => {
				if (action === 'cancel') {
					nextTick(() => {
						// 等待视图更新完成
						closeTab()
					})
				}
			})
	}
	hideTabContextMenu()
}

const { formRef, formInitHandle } = useInitForm()

defineExpose({
	init
})
</script>

<style scoped>
.tree-scroll-wrapper {
	min-width: max-content;
	width: fit-content;
	overflow-x: auto;
	margin-bottom: 10px;
}

.custom-tree {
	display: block;
	width: 100%;
}

.custom-tree-node {
	display: flex;
	align-items: center;
	gap: 5px;
	position: relative;
}

.tree-node-actions {
	display: none;
	margin-left: 5px;
}

.custom-tree-node:hover .tree-node-actions {
	display: block;
}

.edit-icon {
	margin-left: auto;
	cursor: pointer;
	color: #909399;
	font-size: 14px;
}
.edit-icon:hover {
	color: #409eff;
}

.context-menu {
	position: fixed;
	align-items: center;
	background: #fff;
	border: 1px solid #ddd;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
	padding: 5px 5px;
	z-index: 9999;
	list-style: none;
	min-width: 85px;
	max-width: 200px; /* 限制最大宽度 */
	overflow: hidden; /* 防止超出 */
	white-space: nowrap; /* 如果需要单行显示 */
	text-overflow: ellipsis; /* 超出部分用省略号 */
}
.context-menu li {
	display: flex;
	align-items: center;
	gap: 5px;
	font-size: 12px;
	box-sizing: border-box;
	cursor: pointer;
	padding: 5px 10px;
	text-align: left;
	transition:
		background-color 0.2s ease,
		color 0.2s ease;
}

.context-menu li:hover {
	background-color: #f0f0f0; /* 浅灰色高亮，可自定义颜色 */
	color: #333; /* 可选：加深文字颜色 */
}

/* 添加暗黑模式下的右键菜单样式 */
html.dark .context-menu {
	background: #1e1e1e;
	border: 1px solid #3a3a3a;
	color: #e0e0e0;
}

html.dark .context-menu li:hover {
	background-color: #3a3a3a;
	color: #e0e0e0;
}

/* 为图标添加暗黑模式适配 */
html.dark .context-menu svg {
	color: #e0e0e0;
}
/* 两种容器：tab 和 tree node 都可以挂红点 */
.tab-label {
	position: relative;
	display: inline-block;
}

:deep(.el-badge__content.is-fixed.is-dot) {
	right: 0;
}
:deep(.el-badge__content.is-dot) {
	height: 6px;
	width: 6px;
}
</style>
