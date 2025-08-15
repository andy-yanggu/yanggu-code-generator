<template>
	<!-- 预览界面 -->
	<el-drawer v-model="templateTreeData.visible" title="代码预览" :size="'100%'" :modal="false">
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
						style="width: 240px"
					></el-input>
				</div>
				<el-scrollbar ref="treeScrollbarRef" style="height: calc(100% - 50px); overflow-x: auto">
					<div class="tree-scroll-wrapper">
						<el-tree
							ref="treeRef"
							:data="templateTreeData.treeList"
							node-key="filePath"
							:current-node-key="currentNodeKey"
							highlight-current
							class="custom-tree"
							:props="{ label: 'fileName' }"
							:filter-node-method="filterNode"
							@node-click="handleTreeNodeClick"
						>
							<template #default="{ node, data }">
								<div class="custom-tree-node">
									<svg-icon :icon="getIcon(node, data)"></svg-icon>
									<span>{{ node.label }}</span>
								</div>
							</template>
						</el-tree>
					</div>
				</el-scrollbar>
			</el-aside>

			<!-- 右侧：代码预览区 -->
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
							<el-col :span="isFullscreen ? 16 : 15">
								<div class="path-container">
									<el-text truncated class="path-text">路径：{{ templateTreeData.item.filePath }}</el-text>
									<el-tooltip content="复制文件路径" placement="top" effect="dark" :teleported="false">
										<el-icon class="copy-icon" @click="copyPath(templateTreeData.item.filePath)">
											<CopyDocument></CopyDocument>
										</el-icon>
									</el-tooltip>
								</div>
							</el-col>
							<el-col :span="8" style="text-align: right">
								<el-button size="small" @click="handleCopy(templateTreeData.item.templateContent!)">复制代码</el-button>
								<el-button size="small" @click="downloadTemplateData(templateTreeData.item)">生成代码</el-button>
								<el-button size="small" @click="toggle()">{{ isFullscreen ? '退出全屏' : '全屏展示' }}</el-button>
							</el-col>
						</el-row>
						<el-tabs v-model="templateTreeData.tabActiveName" tab-position="top" @tab-click="handleTabClick" @tab-remove="handleTabRemove">
							<el-tab-pane
								v-for="tabItem in templateTreeData.tabList"
								:key="tabItem.filePath"
								:name="tabItem.filePath"
								:label="tabItem.fileName"
								closable
							>
							</el-tab-pane>
						</el-tabs>
					</el-header>

					<!-- 代码区域 -->
					<el-main style="padding: 10px; overflow: hidden">
						<template v-if="templateTreeData.item.templateType === 1">
							<el-scrollbar style="height: 100%">
								<code-mirror v-model="templateTreeData.item.templateContent" :height="contentHeight"></code-mirror>
							</el-scrollbar>
						</template>
						<template v-else-if="templateTreeData.item.templateType === 2">
							<div style="display: flex; align-items: center; justify-content: center; height: 100%">
								<template v-if="imageTypeList.some(tempType => templateTreeData.item.fileName.endsWith(tempType))">
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
import { ElLoading, ElMessage, TabsPaneContext } from 'element-plus'
import CodeMirror from '@/components/code-mirror/index.vue'
import { generatorDownloadSingleApi, generatorSingleLocalApi, generatorPreviewApi } from '@/api/generator'
import { CopyDocument, Expand, Fold } from '@element-plus/icons-vue'
import { copyToClipboard } from '@/utils/tool'
import { useFullscreen } from '@vueuse/core'
import SvgIcon from '@/components/svg-icon/index'

interface Tree {
	// 模板ID
	templateId: number
	// 表ID
	tableId?: number
	// 枚举ID
	enumId?: number
	// 文件名称
	fileName: string
	// 文件路径
	filePath: string
	// 模板组类型（0-项目，1-表，2-枚举）
	templateGroupType: number
	// 模板类型（0-目录，1-模板文件，2-二进制文件）
	templateType: number
	// 模板内容
	templateContent: string
	// 子节点列表
	children?: Tree[]
}

const currentNodeKey = ref('')
const treeRef = ref()
const treeScrollbarRef = ref()
const templateTreeData = reactive({
	visible: false,
	id: -1,
	projectId: -1,
	generatorType: -1,
	treeList: [] as Tree[],
	dataList: [] as Tree[],
	item: {} as Tree,
	tabList: [] as Tree[],
	tabActiveName: ''
})
const treeSearchText = ref('')
const isCollapseRef = ref(false)
const { isFullscreen, toggle } = useFullscreen()
const imageTypeList = ref(['png', 'jpg', 'jpeg', 'gif', 'svg', 'bmp', 'git', 'ico'])

// 初始化方法
const init = async (id: number, projectId: number, generatorType: number, generatorProductType: number) => {
	templateTreeData.id = id
	templateTreeData.projectId = projectId
	templateTreeData.generatorType = generatorType
	isCollapseRef.value = false
	const loadingInstance = ElLoading.service({ fullscreen: true })
	try {
		const previewData = {
			previewProductId: id,
			generatorProductType: generatorProductType
		}
		const res = await generatorPreviewApi(previewData)
		templateTreeData.treeList = res.data
		const templateContentList = buildFileList(res.data)
		templateTreeData.dataList = templateContentList
		templateTreeData.visible = true
		await nextTick()
		if (templateContentList.length > 0 && templateTreeData.tabActiveName === '') {
			templateTreeData.item = templateContentList[0]
			currentNodeKey.value = templateContentList[0].filePath
			treeRef.value.setCurrentKey(currentNodeKey.value)
			templateTreeData.tabActiveName = templateContentList[0].filePath
			tabPush(templateContentList[0])
		}
	} finally {
		loadingInstance.close()
	}
}

const toggleCollapse = () => {
	isCollapseRef.value = !isCollapseRef.value
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

// 计算内容行数
const contentHeight = computed(() => {
	if (!templateTreeData.item.templateContent) {
		return 800
	}
	const length = templateTreeData.item.templateContent!.split('\n').length
	return Math.min(Math.max(20 * length, 800), 1000)
})
watch(treeSearchText, val => {
	treeRef.value!.filter(val)
})

// tree点击
const handleTreeNodeClick = (data: Tree) => {
	// 只有是文件才可以预览
	if (data.templateType != 0) {
		templateTreeData.item = data
		tabPush(data)
		templateTreeData.tabActiveName = data.filePath
	}
}

// tab点击
const handleTabClick = (tab: TabsPaneContext, _: Event) => {
	const filePath = tab.paneName as string
	const matchedItem = templateTreeData.tabList.find(item => item.filePath === filePath)
	if (matchedItem) {
		templateTreeData.item = matchedItem
		currentNodeKey.value = matchedItem.filePath
		treeRef.value.setCurrentKey(currentNodeKey.value)
	}
}

// tab删除
const handleTabRemove = (filePath: string) => {
	// 找到索引
	const index = templateTreeData.tabList.findIndex(item => item.filePath === filePath)
	if (index <= -1) {
		return
	}
	// 删除tab
	templateTreeData.tabList.splice(index, 1)
	let newTabActiveName = templateTreeData.tabActiveName
	// 删除的是否为当前tab
	if (templateTreeData.tabActiveName === filePath && templateTreeData.tabList.length > 0) {
		// 优先尝试右侧标签
		if (index < templateTreeData.tabList.length) {
			newTabActiveName = templateTreeData.tabList[index].filePath
		} else {
			// 右侧无标签时选择左侧最后一个
			newTabActiveName = templateTreeData.tabList[templateTreeData.tabList.length - 1].filePath
		}
	}
	if (templateTreeData.tabList.length > 0) {
		// 设置新的激活项
		const activeTab = templateTreeData.tabList.find(tab => tab.filePath === newTabActiveName)
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
	if (templateTreeData.tabList.some(tab => tab.filePath === tree.filePath)) {
		return
	}
	templateTreeData.tabList.push(tree)
}

// 代码复制到剪切板
const handleCopy = (content: string) => {
	copyToClipboard(content).then(() => {
		ElMessage.success('代码已复制到剪贴板')
	})
}

// 文件路径复制到剪切板
const copyPath = (path: string) => {
	copyToClipboard(path).then(() => {
		ElMessage.success('文件路径已复制到剪切板')
	})
}

//下载单个模板代码
const downloadTemplateData = (item: Tree) => {
	let id
	if (item.templateGroupType === 0) {
		id = templateTreeData.projectId
	} else if (item.templateGroupType === 1) {
		id = item.tableId
	} else if (item.templateGroupType === 2) {
		id = item.enumId
	} else {
		ElMessage.error('暂不支持该类型模板下载')
		return
	}
	const dataForm = {
		id: id,
		templateId: item.templateId,
		templateGroupType: item.templateGroupType
	}
	if (templateTreeData.generatorType === 0) {
		generatorDownloadSingleApi(dataForm)
	} else if (templateTreeData.generatorType === 1) {
		generatorSingleLocalApi(dataForm).then(() => {
			ElMessage.success({
				message: '代码已经下载到本地',
				duration: 1000
			})
		})
	}
}

const filterNode = (value: string, data: Tree) => {
	if (!value) {
		return true
	}
	return data.fileName.includes(value)
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
		const some = imageTypeList.value.some(item => data.fileName.endsWith(item))
		if (some) {
			return 'icon-file-image'
		} else {
			return 'icon-file-unknown'
		}
	} else {
		return 'icon-file-unknown'
	}
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

.path-container {
	display: inline-flex; /* 改为 inline-flex 保持内容紧密排列 */
	align-items: center;
	max-width: 100%; /* 限制最大宽度，防止超出 */
}

.path-text {
	white-space: nowrap; /* 保持文本不换行 */
	overflow: hidden; /* 超出部分隐藏 */
	text-overflow: ellipsis; /* 使用省略号 */
}

.copy-icon {
	cursor: pointer;
	margin-left: 5px; /* 紧跟文本 */
	flex-shrink: 0; /* 防止被压缩 */
}

.custom-tree-node {
	display: flex;
	align-items: center;
	gap: 5px;
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
