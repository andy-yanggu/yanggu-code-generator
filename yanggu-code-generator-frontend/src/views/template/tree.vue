<template>
	<!-- 预览界面 -->
	<el-drawer v-model="templateTreeData.visible" title="配置" :size="'100%'" :modal="false">
		<el-container style="height: 100%">
			<!-- 左侧：树结构 -->
			<el-aside v-show="!isCollapseRef" width="400px" style="overflow: hidden">
				<div style="padding: 15px; border-bottom: 1px solid #eee">
					<el-input v-model="treeSearchText" placeholder="请输入文件/路径" size="small" clearable prefix-icon="Search"></el-input>
				</div>
				<el-scrollbar style="height: calc(100% - 50px); overflow-x: auto">
					<div class="tree-scroll-wrapper">
						<el-tree
							ref="treeRef"
							:data="templateTreeData.treeList"
							node-key="id"
							:current-node-key="currentNodeKey"
							highlight-current
							class="custom-tree"
							:props="{ label: 'fileName' }"
							:filter-node-method="filterNode"
							@node-click="handleTreeNodeClick"
							@node-contextmenu="handleNodeRightClick"
						></el-tree>
					</div>
				</el-scrollbar>
				<!-- 自定义右键菜单 -->
				<div v-if="contextMenu.visible">
					<ul class="context-menu" :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }">
						<li @click="updateTemplate(contextMenu.nodeData)">
							<el-icon size="10"><Edit></Edit></el-icon>修改
						</li>
						<li @click="deleteTemplate(contextMenu.nodeData)">
							<el-icon size="10"><Delete></Delete></el-icon>删除
						</li>
						<template v-if="contextMenu.nodeData?.templateType === 0">
							<li @click="newDir(contextMenu.nodeData)">
								<el-icon size="10"><FolderAdd></FolderAdd></el-icon>新建目录
							</li>
							<li @click="newTemplateFile(contextMenu.nodeData)">
								<el-icon><DocumentAdd></DocumentAdd></el-icon>新建模板文件
							</li>
							<li @click="newBinaryFile(contextMenu.nodeData)">
								<el-icon><DocumentAdd></DocumentAdd></el-icon>新建二进制文件
							</li>
						</template>
					</ul>
				</div>
			</el-aside>

			<add-or-update
				ref="addOrUpdateRef"
				:template-group-id="templateTreeData.id"
				:parent-id="contextMenu.nodeData.id"
				:template-type="contextMenu.templateType"
				@refresh-data-list="init(templateTreeData.id)"
			></add-or-update>

			<!-- 右侧：模板编辑 -->
			<el-main v-if="templateTreeData.tabList.length > 0" style="padding: 0" :class="{ 'full-screen-mode': isFullscreen }">
				<el-container style="height: 100%">
					<!-- 头部操作区域 -->
					<el-header style="display: flex; flex-direction: column; padding: 10px; height: 30px; margin-bottom: 5px">
						<el-row>
							<el-col v-if="!isFullscreen" :span="1">
								<el-icon :size="20" class="collapse-icon" @click="toggleCollapse()">
									<Expand v-if="isCollapseRef"></Expand>
									<Fold v-else></Fold>
								</el-icon>
							</el-col>
							<el-col :span="isFullscreen ? 18 : 17">
								<el-text tag="b" truncated>路径：{{ fullFilePath }}</el-text>
							</el-col>
							<el-col :span="5" style="text-align: right">
								<el-button size="small" type="primary" :icon="Edit" :loading="submitLoading" @click="saveTemplateContent">保存模板</el-button>
							</el-col>
							<el-col :span="1" style="display: flex; justify-content: center; align-items: center">
								<el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" effect="dark" placement="bottom">
									<el-icon :size="18" class="collapse-icon" @click="toggle()">
										<FullScreen v-if="!isFullscreen"></FullScreen>
										<Aim v-else></Aim>
									</el-icon>
								</el-tooltip>
							</el-col>
						</el-row>
					</el-header>

					<!-- 代码区域 -->
					<el-main style="padding: 10px; overflow: hidden">
						<el-tabs v-model="templateTreeData.tabActiveName" tab-position="top" @tab-click="handleTabClick" @tab-remove="handleTabRemove">
							<el-tab-pane v-for="tabItem in templateTreeData.tabList" :key="tabItem.id" :name="tabItem.id" :label="tabItem.templateName" closable>
							</el-tab-pane>
						</el-tabs>
						<template v-if="templateTreeData.item.templateType === 1">
							<el-scrollbar style="height: 100%">
								<code-mirror v-model="templateTreeData.item.templateContent" :height="contentHeight"></code-mirror>
							</el-scrollbar>
						</template>
						<template v-else-if="templateTreeData.item.templateType === 2">
							<div style="display: flex; align-items: center; justify-content: center; height: 100%">
								<template
									v-if="
										['png', 'jpg', 'jpeg', 'gif', 'svg', 'bmp', 'git', 'ico'].some(tempType => templateTreeData.item.templateName.endsWith(tempType))
									"
								>
									<el-image :src="templateTreeData.item.templateContent" fit="fill"></el-image>
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
import { templateDeleteApi, templateTreeDataApi, templateUpdateContentApi } from '@/api/template'
import { Aim, Delete, DocumentAdd, Edit, Expand, Fold, FolderAdd, FullScreen } from '@element-plus/icons-vue'
import { useFullscreen } from '@vueuse/core'
import { templateSubmitApi } from '@/api/template'
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
	// 模板描述
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

const currentNodeKey = ref()
const treeRef = ref()
const templateTreeData = reactive({
	visible: false,
	id: -1,
	treeList: [] as Tree[],
	dataList: [] as Tree[],
	item: {} as Tree,
	tabList: [] as Tree[],
	tabActiveName: -1
})

const treeSearchText = ref('')
const isCollapseRef = ref(false)
const addOrUpdateRef = ref()
const { isFullscreen, toggle } = useFullscreen()
// 右键菜单状态
const contextMenu = reactive({
	visible: false,
	x: 0,
	y: 0,
	templateType: -1,
	nodeData: { id: 0 } as Tree
})
const submitLoading = ref(false)

const fullFilePath = computed(() => {
	return getFullPathById(templateTreeData.item.id, templateTreeData.treeList)
})

// 初始化方法
const init = async (id: number) => {
	templateTreeData.id = id
	isCollapseRef.value = false
	const loadingInstance = ElLoading.service({ fullscreen: true })
	try {
		const res = await templateTreeDataApi(id)
		templateTreeData.treeList = res.data
		const templateContentList = buildFileList(res.data)
		templateTreeData.dataList = templateContentList
		templateTreeData.visible = true
		await nextTick()
		if (templateContentList.length > 0) {
			templateTreeData.item = templateTreeData.dataList[0]
			currentNodeKey.value = templateContentList[0].id
			treeRef.value.setCurrentKey(currentNodeKey.value)
			if (templateTreeData.tabActiveName === -1) {
				templateTreeData.tabActiveName = templateContentList[0].id
				tabPush(templateContentList[0])
			}
		}
	} finally {
		loadingInstance.close()
	}
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
	const length = templateTreeData.item.templateContent!.split('\n').length
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
		const filterElement = templateTreeData.dataList.filter(item => item.id === data.id)[0]
		templateTreeData.item = filterElement
		tabPush(filterElement)
		templateTreeData.tabActiveName = filterElement.id
	}
}

// tab点击
const handleTabClick = (tab: TabsPaneContext, _: Event) => {
	const id = tab.paneName as number
	const matchedItem = templateTreeData.tabList.find(item => item.id === id)
	if (matchedItem) {
		templateTreeData.item = matchedItem
		currentNodeKey.value = matchedItem.id
		treeRef.value.setCurrentKey(currentNodeKey.value)
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
	let newTabActiveName = templateTreeData.tabActiveName
	// 删除的是否为当前tab
	if (templateTreeData.tabActiveName === id && templateTreeData.tabList.length > 0) {
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
			templateTreeData.item = activeTab
		}
		templateTreeData.tabActiveName = newTabActiveName
		currentNodeKey.value = newTabActiveName
		treeRef.value?.setCurrentKey(currentNodeKey.value)
	}
}

// 添加tab（进行去重）
const tabPush = (tree: Tree) => {
	if (templateTreeData.tabList.some(tab => tab.id === tree.id)) {
		return
	}
	templateTreeData.tabList.push(tree)
}

const saveTemplateContent = () => {
	submitLoading.value = true
	const dataForm = {
		id: templateTreeData.item.id,
		templateContent: templateTreeData.item.templateContent
	}
	templateUpdateContentApi(dataForm)
		.then(() => {
			ElMessage.success({
				message: '操作成功',
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

// 右键事件
const handleNodeRightClick = (event: MouseEvent, data: Tree) => {
	event.preventDefault()
	contextMenu.visible = true
	contextMenu.x = event.clientX
	contextMenu.y = event.clientY
	contextMenu.nodeData = data
	document.addEventListener('click', hideContextMenu)
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
	console.log('编辑', node)
	contextMenu.templateType = node.templateType
	nextTick(() => {
		addOrUpdateRef.value.init(node.id)
	})
	hideContextMenu()
}

// 删除模板
const deleteTemplate = (node: Tree) => {
	console.log('删除', node)
	const message = node.templateType === 0 ? '确定删除本身及所有子节点吗？' : '确定删除文件吗？'
	ElMessageBox.confirm(message, '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	}).then(() => {
		templateDeleteApi(node.id).then(() => {
			ElMessage.success({
				message: '删除成功'
			})
		})
	})
	hideContextMenu()
}

// 新建目录
const newDir = (parent: Tree) => {
	console.log('新建目录在', parent)
	contextMenu.templateType = 0
	nextTick(() => {
		addOrUpdateRef.value.init()
	})
	hideContextMenu()
}

// 新建模板文件
const newTemplateFile = (parent: Tree) => {
	console.log('新建模板文件在', parent)
	contextMenu.templateType = 1
	nextTick(() => {
		addOrUpdateRef.value.init()
	})
	hideContextMenu()
}

// 新建二进制文件
const newBinaryFile = (parent: Tree) => {
	console.log('新建二进制文件在', parent)
	contextMenu.templateType = 2
	nextTick(() => {
		addOrUpdateRef.value.init()
	})
	hideContextMenu()
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
}

.custom-tree {
	display: block;
	width: 100%;
}

.context-menu {
	position: fixed;
	background: #fff;
	border: 1px solid #ddd;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
	padding: 4px 0;
	z-index: 9999;
	list-style: none;
	min-width: 100px;
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
