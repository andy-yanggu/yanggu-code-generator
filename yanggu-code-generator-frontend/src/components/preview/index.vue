<template>
	<!-- 预览界面 -->
	<el-drawer v-model="templateTreeData.visible" :title="`${templateTreeData.name} - 代码预览`" :size="'100%'" destroy-on-close>
		<el-splitter class="preview-container" style="height: 100%">
			<!-- 左侧：树结构 -->
			<el-splitter-panel collapsible size="30%" style="overflow: hidden">
				<el-row style="margin-bottom: 10px" :gutter="10">
					<el-col :span="16">
						<el-input
							v-model="treeSearchText"
							placeholder="请输入目录/文件名称"
							size="small"
							clearable
							:prefix-icon="Search"
							style="width: 100%"
						></el-input>
					</el-col>
					<el-col :span="8" style="display: flex; justify-content: center; align-items: center">
						<el-button
							size="small"
							type="primary"
							:icon="Refresh"
							@click="
								init(
									templateTreeData.id,
									templateTreeData.name,
									templateTreeData.projectId,
									templateTreeData.generatorType,
									templateTreeData.generatorProductType
								)
							"
						>
							刷新
						</el-button>
					</el-col>
				</el-row>
				<el-scrollbar ref="treeScrollbarRef" style="height: calc(100% - 30px); overflow-x: auto">
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
			</el-splitter-panel>

			<!-- 右侧：代码预览区 -->
			<el-splitter-panel v-if="templateTreeData.tabList.length > 0" :class="{ 'full-screen-mode': isFullscreen }">
				<el-container style="height: 100%">
					<!-- 头部操作区域 -->
					<el-header style="display: flex; flex-direction: column; margin-bottom: 10px">
						<el-row style="margin-bottom: 5px">
							<el-col :span="16">
								<div class="path-container">
									<text-tooltip :title="'路径：' + templateTreeData.item.filePath" :max-width="'100%'"></text-tooltip>
									<el-tooltip :disabled="copyIconState === Check" content="复制文件路径" placement="top">
										<el-icon class="copy-icon" @click="copyPath(templateTreeData.item.filePath)">
											<component :is="copyIconState"></component>
										</el-icon>
									</el-tooltip>
								</div>
							</el-col>
							<el-col :span="8" style="text-align: right">
								<el-tooltip content="复制代码" placement="top">
									<el-button
										v-if="templateTreeData.item.templateType === 1"
										type="primary"
										size="small"
										:icon="CopyDocument"
										@click="handleCopy(templateTreeData.item.templateContent!)"
									>
										复制
									</el-button>
								</el-tooltip>
								<el-tooltip :content="generatorState.tooltip" placement="top">
									<el-button
										type="success"
										size="small"
										:loading="submitLoading"
										:icon="generatorState.icon"
										@click="downloadTemplateData(templateTreeData.item)"
									>
										{{ generatorState.text }}
									</el-button>
								</el-tooltip>
								<el-tooltip :content="isFullscreen ? '退出全屏' : '开启全屏'" placement="top">
									<el-button size="small" @click="toggle()">
										<svg-icon :icon="isFullscreen ? 'icon-fullscreen-exit' : 'icon-fullscreen'" style="margin-right: 4px" is-pointer></svg-icon>
										{{ isFullscreen ? '退出' : '全屏' }}
									</el-button>
								</el-tooltip>
							</el-col>
						</el-row>
						<el-tabs
							v-model="templateTreeData.tabActiveName"
							tab-position="top"
							type="card"
							@tab-click="handleTabClick"
							@tab-remove="handleTabRemove"
						>
							<el-tab-pane
								v-for="tabItem in templateTreeData.tabList"
								:key="tabItem.filePath"
								:name="tabItem.filePath"
								:label="tabItem.fileName"
								closable
							>
								<template #label>
									<icon-text-tooltip :title="tabItem.fileName" :icon="getIcon({ expanded: false }, tabItem)"></icon-text-tooltip>
								</template>
							</el-tab-pane>
						</el-tabs>
					</el-header>

					<!-- 代码区域 -->
					<el-main style="padding: 10px; overflow: hidden">
						<template v-if="templateTreeData.item.templateType === 1">
							<el-scrollbar ref="codeScrollbarRef" style="height: 100%">
								<code-mirror v-model="templateTreeData.item.templateContent" :read-only="true"></code-mirror>
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
			</el-splitter-panel>
			<el-splitter-panel v-else style="display: flex; flex-direction: column; height: 100%" :class="{ 'full-screen-mode': isFullscreen }">
				<el-row>
					<el-col :span="24" style="text-align: right">
						<el-button size="small" @click="toggle()">
							<svg-icon :icon="isFullscreen ? 'icon-fullscreen-exit' : 'icon-fullscreen'" style="margin-right: 4px" is-pointer></svg-icon>
							{{ isFullscreen ? '退出' : '全屏' }}
						</el-button>
					</el-col>
				</el-row>
				<div style="flex: 1; display: flex; align-items: center; justify-content: center">
					<el-text size="large" tag="b">请点击左侧的文件树进行代码预览</el-text>
				</div>
			</el-splitter-panel>
		</el-splitter>
	</el-drawer>
</template>

<script setup lang="ts">
import { nextTick, reactive, ref, shallowReactive, shallowRef, watch } from 'vue'
import { ElLoading, ElMessage, TabsPaneContext } from 'element-plus'
import CodeMirror from '@/components/code-mirror/index.vue'
import TextTooltip from '@/components/text-tooltip/index.vue'
import { genGeneratorApi } from '@/api'
import { Check, CopyDocument, DocumentAdd, Download, Refresh, Search } from '@element-plus/icons-vue'
import { cloneObject, copyToClipboard, resetReactiveObject } from '@/utils/tool'
import { useFullscreen, useTimeoutFn } from '@vueuse/core'
import SvgIcon from '@/components/svg-icon/index'
import { useSubmitHandler } from '@/hooks'
import { SubmitOptions } from '@/types'
import IconTextTooltip from '@/components/icon-text-tooltip/index.vue'

defineOptions({
	name: 'Preview'
})

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
const copyIconState = shallowRef(CopyDocument)
const { start: startTimer } = useTimeoutFn(() => {
	copyIconState.value = CopyDocument
}, 2000)

const INIT_GENERATOR_STATE_ARRAY = [
	{
		icon: Download,
		text: '下载',
		tooltip: '下载代码'
	},
	{
		icon: DocumentAdd,
		text: '生成',
		tooltip: '生成代码'
	}
]
const generatorState = shallowReactive(cloneObject(INIT_GENERATOR_STATE_ARRAY[0]))
const INIT_TEMPLATE_TREE_DATA = {
	visible: false,
	id: -1,
	name: '',
	projectId: -1,
	generatorType: -1,
	generatorProductType: -1,
	treeList: [] as Tree[],
	dataList: [] as Tree[],
	item: {} as Tree,
	tabList: [] as Tree[],
	tabActiveName: ''
}
const templateTreeData = reactive(cloneObject(INIT_TEMPLATE_TREE_DATA))
const treeSearchText = ref('')
const { isFullscreen, toggle } = useFullscreen()
const imageTypeList = ref(['png', 'jpg', 'jpeg', 'gif', 'svg', 'bmp', 'git', 'ico'])

// 初始化方法
const init = async (id: number, name: string, projectId: number, generatorType: number, generatorProductType: number) => {
	resetReactiveObject(templateTreeData, INIT_TEMPLATE_TREE_DATA)
	templateTreeData.id = id
	templateTreeData.name = name
	templateTreeData.projectId = projectId
	templateTreeData.generatorType = generatorType
	if (generatorType === 0) {
		resetReactiveObject(generatorState, INIT_GENERATOR_STATE_ARRAY[0])
	} else {
		resetReactiveObject(generatorState, INIT_GENERATOR_STATE_ARRAY[1])
	}
	templateTreeData.generatorProductType = generatorProductType
	templateTreeData.visible = true
	// 等待DOM更新后再显示loading
	await nextTick()
	const loadingInstance = ElLoading.service({ target: '.preview-container', text: '数据加载中...' })
	try {
		const previewData = {
			previewProductId: id,
			generatorProductType: generatorProductType
		}
		const data: Tree[] = await genGeneratorApi.preview(previewData)
		templateTreeData.treeList = data
		const templateContentList = buildFileList(data)
		templateTreeData.dataList = templateContentList
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

const codeScrollbarRef = ref()

// 监听树节点变化，实现节点自动滚动到可视区域内
watch(
	() => templateTreeData.item.filePath,
	filePath => {
		// 等待 el-tree 渲染完成
		nextTick(() => {
			const dom = treeRef.value.$el.querySelector(`[data-key="${filePath}"]`)
			if (dom) {
				// 树节点滚动到可视区域
				dom.scrollIntoView({
					behavior: 'smooth',
					inline: 'center',
					block: 'nearest'
				})
			}

			// 代码编辑区滚动到顶部
			codeScrollbarRef.value?.scrollTo({
				top: 0,
				behavior: 'smooth'
			})
		})
	}
)

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
	// 如果当前是Check状态，则不处理点击事件（防止重复点击）
	if (copyIconState.value === Check) {
		return
	}

	// 切换图标为Check
	copyIconState.value = Check

	copyToClipboard(path)
		.then(() => {
			ElMessage.success('文件路径已复制到剪贴板')

			// 2秒后恢复为CopyDocument图标
			startTimer()
		})
		.catch(() => {
			// 如果复制失败也恢复图标
			copyIconState.value = CopyDocument
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
		submitState.submitApi = genGeneratorApi.downloadSingle
		submitState.successMessage = '代码已经下载到浏览器'
	} else if (templateTreeData.generatorType === 1) {
		submitState.submitApi = genGeneratorApi.singleLocal
		submitState.successMessage = '代码已经生成到本地'
	}
	submitHandle(dataForm)
}

const submitState = shallowReactive({
	successDuration: 1000
} as SubmitOptions)

const { submitLoading, submitHandle } = useSubmitHandler(submitState)

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
	max-width: 100%;
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

/* 覆盖 Element Plus 默认样式 */
:deep(.full-screen-mode) {
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	z-index: 3000;
	background: var(--theme-main-bg-color);
	margin: 0 !important;
	padding: 20px;
}
</style>
