<template>
	<!-- 预览界面 -->
	<el-drawer v-model="templateTreeData.visible" :title="`${templateGroupName} - 配置`" :size="'100%'" :modal="false">
		<el-container style="height: 100%">
			<!-- 左侧：树结构 -->
			<el-aside v-show="!isCollapseRef" width="400px" style="overflow: hidden">
				<div style="margin-bottom: 10px; gap: 20px; display: flex; justify-content: center; align-items: center">
					<el-input
						v-model="treeSearchText"
						placeholder="请输入目录/文件名称"
						size="small"
						clearable
						prefix-icon="Search"
						style="width: 200px"
					></el-input>
					<div style="display: flex; text-align: right">
						<el-button size="small" type="primary" :icon="Refresh" @click="refreshTree">刷新</el-button>
						<el-button size="small" type="danger" :icon="Delete" @click="deleteCheckedNode">删除</el-button>
					</div>
				</div>
				<el-scrollbar style="height: calc(100% - 50px); overflow-x: auto" @contextmenu.prevent.stop="handleScrollWrapperRightClick">
					<div class="tree-scroll-wrapper">
						<el-tree
							ref="treeRef"
							:data="templateTreeData.treeList"
							node-key="id"
							:current-node-key="templateTreeData.activeItem.id"
							highlight-current
							show-checkbox
							class="custom-tree"
							:props="{ label: 'fileName' }"
							:filter-node-method="filterNode"
							draggable
							:allow-drag="treeAllowDrag"
							:allow-drop="treeAllowDrop"
							@node-click="handleTreeNodeClick"
							@node-contextmenu="handleNodeRightClick"
							@node-drop="handleNodeDrop"
						>
							<template #default="{ node, data }">
								<el-tooltip :content="data.templateDesc" placement="top" effect="light" :disabled="!data.templateDesc">
									<div class="custom-tree-node">
										<svg-icon :icon="getIcon(node, data)"></svg-icon>
										<span>{{ node.label }}</span>
										<!-- <el-icon class="edit-icon" @click.stop="updateTemplate(data)">
											<Edit></Edit>
										</el-icon> -->
									</div>
								</el-tooltip>
							</template>
							<template #empty>
								<div style="text-align: center; padding: 20px">
									<div>暂无数据，请右键创建目录、模板文件或者二进制文件</div>
								</div>
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
			</el-aside>

			<add-or-update
				ref="addOrUpdateRef"
				:template-group-id="templateGroupId"
				:parent-id="contextMenu.parentId"
				:template-type="contextMenu.templateType"
				:template-path="contextMenu.templatePath"
				@refresh-data-list="init()"
			></add-or-update>

			<!-- 右侧：模板编辑 -->
			<el-main v-if="templateTreeData.tabList.length > 0" style="padding: 0" :class="{ 'full-screen-mode': isFullscreen }">
				<el-container style="height: 100%">
					<!-- 头部操作区域 -->
					<el-header style="display: flex; flex-direction: column; padding: 10px; height: 70px; margin-bottom: 5px">
						<el-row>
							<el-col v-if="!isFullscreen" :span="1">
								<el-icon :size="20" class="collapse-icon" @click="toggleCollapse()">
									<Expand v-if="isCollapseRef"></Expand>
									<Fold v-else></Fold>
								</el-icon>
							</el-col>
							<el-col :span="isFullscreen ? 18 : 17">
								<el-tooltip :content="fullFilePath" placement="top">
									<el-text style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; width: 100%">
										路径：{{ fullFilePath }}
									</el-text>
								</el-tooltip>
							</el-col>
							<el-col :span="6" style="text-align: right">
								<el-button size="small" type="primary" :icon="DocumentChecked" :loading="submitLoading" @click="saveTemplateContent">保存</el-button>
								<el-button size="small" @click="toggle()">{{ isFullscreen ? '退出全屏' : '全屏展示' }}</el-button>
							</el-col>
						</el-row>
						<el-tabs v-model="templateTreeData.activeItem.id" tab-position="top" @tab-click="handleTabClick" @tab-remove="handleTabRemove">
							<el-tab-pane v-for="(tabItem, index) in templateTreeData.tabList" :key="tabItem.id" :name="tabItem.id" closable>
								<template #label>
									<el-tooltip :content="tabItem.templateDesc" effect="light" :disabled="!tabItem.templateDesc" placement="bottom">
										<span @contextmenu.prevent.stop="showTabMenu($event, tabItem, index)">
											{{ tabItem.fileName }}
										</span>
									</el-tooltip>
								</template>
							</el-tab-pane>
						</el-tabs>
						<!-- tab右键菜单 -->
						<div v-if="tabContextMenu.visible">
							<ul class="context-menu" :style="{ top: tabContextMenu.y + 'px', left: tabContextMenu.x + 'px' }">
								<li @click="closeCurrentTab">
									<el-icon size="10"><CloseBold></CloseBold></el-icon>
									<span>关闭当前</span>
								</li>
								<li v-if="templateTreeData.tabList.length > 1" @click="closeOtherTabs">
									<el-icon size="10"><CircleClose></CircleClose></el-icon>
									<span>关闭其他</span>
								</li>
								<li v-if="tabContextMenu.index > 0" @click="closeLeftTabs">
									<el-icon size="10"><Back></Back></el-icon>
									<span>关闭左侧</span>
								</li>
								<li v-if="tabContextMenu.index < templateTreeData.tabList.length - 1" @click="closeRightTabs">
									<el-icon size="10"><Right></Right></el-icon>
									<span>关闭右侧</span>
								</li>
								<li v-if="templateTreeData.tabList.length > 1" @click="closeAllTabs">
									<el-icon size="10"><Close></Close></el-icon>
									<span>关闭全部</span>
								</li>
							</ul>
						</div>
					</el-header>

					<!-- 代码区域 -->
					<el-main style="padding: 10px; overflow: hidden">
						<template v-if="templateTreeData.activeItem.templateType === 1">
							<el-scrollbar style="height: 100%">
								<code-mirror v-model="templateTreeData.activeItem.templateContent" :height="contentHeight"></code-mirror>
							</el-scrollbar>
						</template>
						<template v-else-if="templateTreeData.activeItem.templateType === 2">
							<div style="display: flex; align-items: center; justify-content: center; height: 100%">
								<template v-if="imageTypeList.some(tempType => templateTreeData.activeItem.binaryOriginalFileName?.endsWith(tempType))">
									<el-image :src="templateTreeData.activeItem.templateContent" fit="fill"></el-image>
								</template>
								<template v-else>
									<el-text size="large" tag="b">文件暂不支持预览（目前只支持图片）</el-text>
								</template>
							</div>
						</template>
					</el-main>
				</el-container>
			</el-main>
			<el-main v-else style="display: flex; flex-direction: column; height: 100%" :class="{ 'full-screen-mode': isFullscreen }">
				<el-row>
					<el-col v-if="!isFullscreen" :span="1">
						<el-icon :size="20" class="collapse-icon" @click="toggleCollapse()">
							<Expand v-if="isCollapseRef"></Expand>
							<Fold v-else></Fold>
						</el-icon>
					</el-col>
					<el-col :span="23" style="text-align: right">
						<el-button size="small" @click="toggle()">{{ isFullscreen ? '退出全屏' : '全屏展示' }}</el-button>
					</el-col>
				</el-row>
				<div style="flex: 1; display: flex; align-items: center; justify-content: center">
					<el-text size="large" tag="b">请点击左侧的文件树进行代码预览</el-text>
				</div>
			</el-main>
		</el-container>
	</el-drawer>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ElLoading, ElMessageBox, TabsPaneContext } from 'element-plus'
import CodeMirror from '@/components/code-mirror/index.vue'
import AddOrUpdate from '@/views/template/add-or-update.vue'
import SvgIcon from '@/components/svg-icon/index'
import { templateDeleteListApi, templateTreeDataApi, templateUpdateContentApi, templateUpdateParentApi } from '@/api/template'
import { Back, CircleClose, Close, CloseBold, Delete, DocumentChecked, Edit, Expand, Fold, Refresh, Right } from '@element-plus/icons-vue'
import { useFullscreen } from '@vueuse/core'
import { ElMessage } from 'element-plus/es'

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
	// 模板内容
	templateContent?: string
	// 二进制原始文件名
	binaryOriginalFileName?: string
	// 子节点列表
	children?: Tree[]
}

const props = defineProps({
	templateGroupId: {
		type: Number,
		required: true
	},
	templateGroupName: {
		type: String,
		required: true
	}
})

const treeRef = ref()
const templateTreeData = reactive({
	visible: false,
	treeList: [] as Tree[],
	dataList: [] as Tree[],
	activeItem: { id: -1 } as Tree,
	tabList: [] as Tree[]
})

const treeSearchText = ref('')
const isCollapseRef = ref(false)
const addOrUpdateRef = ref()
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
	return getFullPathById(templateTreeData.activeItem.id, templateTreeData.treeList)
})

// 初始化方法
const init = async () => {
	isCollapseRef.value = false
	const loadingInstance = ElLoading.service({ fullscreen: true })
	try {
		const res = await templateTreeDataApi(props.templateGroupId)
		templateTreeData.treeList = res.data
		const templateContentList = buildFileList(res.data)
		templateTreeData.dataList = templateContentList
		templateTreeData.visible = true
		await nextTick()
		if (templateContentList.length > 0 && templateTreeData.activeItem.id === -1) {
			Object.assign(templateTreeData.activeItem, templateContentList[0])
			tabPush(templateContentList[0])
		}
		// 更新一下tab和activeItem中的数据，已新加载数据为准
		refreshData()
	} finally {
		loadingInstance.close()
	}
}

// 全量刷新树数据，回到初始状态
const refreshTree = () => {
	ElMessageBox.confirm('刷新会丢失所有编辑状态，确定要刷新吗？', '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			// 清空搜索条件
			treeSearchText.value = ''

			// 清空所有tab
			templateTreeData.tabList = []
			Object.assign(templateTreeData.activeItem, { id: -1 } as Tree)

			// 重新加载数据
			init()
		})
		.catch(() => {
			ElMessage.info('已取消刷新')
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
	isCollapseRef.value = !isCollapseRef.value
}

// 计算内容行数
const contentHeight = computed(() => {
	if (!templateTreeData.activeItem.templateContent) {
		return 800
	}
	const length = templateTreeData.activeItem.templateContent!.split('\n').length
	return Math.min(Math.max(20 * length, 800), 1000)
})

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
		const filterElement = getElementFromDataList(data.id)!
		Object.assign(templateTreeData.activeItem, filterElement)
		tabPush(filterElement)
	}
}

// 删除勾选的树节点
const deleteCheckedNode = () => {
	const allCheckedKeys = treeRef.value.getCheckedKeys()
	if (allCheckedKeys.length === 0) {
		ElMessage.warning('请勾选目录或者文件')
		return
	}
	ElMessageBox.confirm('此操作将删除该目录或者文件, 是否继续?', '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	}).then(() => {
		templateDeleteListApi(allCheckedKeys)
			.then(() => {
				ElMessage.success('删除成功')
			})
			.then(() => {
				// 删除tab
				allCheckedKeys.forEach((tempId: number) => handleTabRemove(tempId))
			})
			.then(() => {
				init()
			})
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
	templateUpdateParentApi(dataForm)
		.then(() => {
			ElMessage.success('移动成功')
			init() // 重新加载数据
		})
		.catch(() => {
			ElMessage.error('移动失败')
			init() // 恢复原状
		})
}

// tab点击
const handleTabClick = (tab: TabsPaneContext, _: Event) => {
	const id = tab.paneName as number
	const matchedItem = templateTreeData.tabList.find(item => item.id === id)
	if (matchedItem) {
		Object.assign(templateTreeData.activeItem, matchedItem)
	}
}

// tab删除
const handleTabRemove = (id: number) => {
	// 找到索引
	const index = templateTreeData.tabList.findIndex(item => item.id === id)
	if (index <= -1) {
		return
	}
	// 删除tab
	templateTreeData.tabList.splice(index, 1)
	let newTabActiveName = templateTreeData.activeItem.id
	// 删除的是否为当前tab
	if (templateTreeData.activeItem.id === id && templateTreeData.tabList.length > 0) {
		// 优先尝试右侧标签
		if (index < templateTreeData.tabList.length) {
			newTabActiveName = templateTreeData.tabList[index].id
		} else {
			// 右侧无标签时选择左侧最后一个
			newTabActiveName = templateTreeData.tabList[templateTreeData.tabList.length - 1].id
		}
	}
	if (templateTreeData.tabList.length > 0) {
		// 设置新的激活项
		const activeTab = templateTreeData.tabList.find(tab => tab.id === newTabActiveName)
		if (activeTab) {
			Object.assign(templateTreeData.activeItem, activeTab)
		}
	}
}

// 添加tab（进行去重）
const tabPush = (tree: Tree) => {
	if (templateTreeData.tabList.some(tab => tab.id === tree.id)) {
		return
	}
	templateTreeData.tabList.push(tree)
}

const refreshData = () => {
	// 更新激活项数据
	Object.assign(templateTreeData.activeItem, getElementFromDataList(templateTreeData.activeItem.id)!)
	if (templateTreeData.tabList.length === 0) {
		return
	}
	for (let index = 0; index < templateTreeData.tabList.length; index++) {
		const tabElementId = templateTreeData.tabList[index].id
		templateTreeData.tabList[index] = getElementFromDataList(tabElementId)!
	}
}

const getElementFromDataList = (id: number): Tree | undefined => {
	return templateTreeData.dataList.find(item => item.id === id)
}

const saveTemplateContent = () => {
	submitLoading.value = true
	const dataForm = {
		id: templateTreeData.activeItem.id,
		templateContent: templateTreeData.activeItem.templateContent
	}
	templateUpdateContentApi(dataForm)
		.then(() => {
			ElMessage.success({
				message: '保存成功',
				duration: 500
			})
		})
		.finally(() => {
			submitLoading.value = false
		})
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
		addOrUpdateRef.value.init(node.id)
	})
	hideContextMenu()
}

// 新建目录
const newDir = (parent: Tree) => {
	// console.log('新建目录在', parent)
	contextMenu.templateType = 0
	contextMenu.parentId = parent.id
	nextTick(() => {
		addOrUpdateRef.value.init()
	})
	hideContextMenu()
}

// 新建模板文件
const newTemplateFile = (parent: Tree) => {
	// console.log('新建模板文件在', parent)
	contextMenu.templateType = 1
	contextMenu.parentId = parent.id
	nextTick(() => {
		addOrUpdateRef.value.init()
	})
	hideContextMenu()
}

// 新建二进制文件
const newBinaryFile = (parent: Tree) => {
	// console.log('新建二进制文件在', parent)
	contextMenu.templateType = 2
	contextMenu.parentId = parent.id
	nextTick(() => {
		addOrUpdateRef.value.init()
	})
	hideContextMenu()
}

// 获取节点的图标
const getIcon = (node: any, data: Tree): string => {
	if (node.expanded) {
		return 'icon-folder-open'
	} else if (data.templateType === 0) {
		return 'icon-folder'
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

const showTabMenu = (e: MouseEvent, tab: Tree, index: number) => {
	e.preventDefault()
	// console.log('tab右键', tab)
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

// 关闭当前tab
const closeCurrentTab = () => {
	// 删除当前tab
	templateTreeData.tabList = templateTreeData.tabList.filter(tab => tab.id !== tabContextMenu.item.id)

	// 判断激活的是否为当前tab
	if (templateTreeData.activeItem.id === tabContextMenu.item.id && templateTreeData.tabList.length > 0) {
		let newActiveTab
		// 优先尝试右侧标签
		if (tabContextMenu.index < templateTreeData.tabList.length) {
			newActiveTab = templateTreeData.tabList[tabContextMenu.index]
		} else {
			// 右侧无标签是选择左侧最后一个
			newActiveTab = templateTreeData.tabList[templateTreeData.tabList.length - 1]
		}
		Object.assign(templateTreeData.activeItem, newActiveTab)
	}

	hideTabContextMenu()
}

// 关闭其他tab
const closeOtherTabs = () => {
	templateTreeData.tabList = templateTreeData.tabList.filter(tab => tab.id === tabContextMenu.item.id)
	Object.assign(templateTreeData.activeItem, tabContextMenu.item)
	hideTabContextMenu()
}

// 关闭左侧tab
const closeLeftTabs = () => {
	templateTreeData.tabList = templateTreeData.tabList.filter((_, index) => index >= tabContextMenu.index)
	Object.assign(templateTreeData.activeItem, tabContextMenu.item)
	hideTabContextMenu()
}

// 关闭右侧tab
const closeRightTabs = () => {
	templateTreeData.tabList = templateTreeData.tabList.filter((_, index) => index <= tabContextMenu.index)
	Object.assign(templateTreeData.activeItem, tabContextMenu.item)
	hideTabContextMenu()
}

// 关闭全部tab
const closeAllTabs = () => {
	templateTreeData.tabList = []
	hideTabContextMenu()
}

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
	background: #fff;
	border: 1px solid #ddd;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
	padding: 4px 0;
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
	width: 100%; /* 让项目占满容器宽度 */
	cursor: pointer; /* 鼠标变成小手 */
	padding: 6px 8px;
	border-radius: 4px;
	box-sizing: border-box; /* 避免padding让li超宽 */
	transition:
		background-color 0.2s ease,
		color 0.2s ease; /* 动画过渡 */
}
.context-menu li:hover {
	background-color: #f0f0f0; /* 浅灰色高亮，可自定义颜色 */
	color: #333; /* 可选：加深文字颜色 */
}

.collapse-icon {
	cursor: pointer;
}

/* 覆盖 Element Plus 默认样式 */
:deep(.full-screen-mode) {
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	z-index: 3000;
	background: white;
	margin: 0 !important;
	padding: 20px;
}
</style>
