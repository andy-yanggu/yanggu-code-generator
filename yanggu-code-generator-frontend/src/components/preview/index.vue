<template>
	<!-- 预览界面 -->
	<el-drawer v-model="preview.visible" title="代码预览" :size="'100%'" :modal="false">
		<el-container style="height: 100%">
			<!-- 左侧：树结构 -->
			<el-aside v-show="!isCollapseRef" width="300px" style="overflow: hidden">
				<div style="padding: 15px; border-bottom: 1px solid #eee">
					<el-input v-model="treeSearchText" placeholder="请输入文件/路径" size="small" clearable prefix-icon="Search"></el-input>
				</div>
				<el-scrollbar style="height: calc(100% - 50px); overflow-x: auto">
					<div class="tree-scroll-wrapper">
						<el-tree
							ref="treeRef"
							:data="preview.treeData"
							node-key="filePath"
							:current-node-key="currentNodeKey"
							highlight-current
							class="custom-tree"
							:filter-node-method="filterNode"
							@node-click="handleTreeNodeClick"
						/>
					</div>
				</el-scrollbar>
			</el-aside>

			<!-- 右侧：代码预览区 -->
			<el-main v-if="preview.tabList.length > 0" style="padding: 0" :class="{ 'full-screen-mode': isFullscreen }">
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
							<el-col :span="isFullscreen ? 17 : 16">
								路径：<el-text>{{ preview.item.filePath }}</el-text>
								<el-tooltip content="复制路径" placement="top" effect="dark" :teleported="false">
									<el-icon style="cursor: pointer; margin-left: 10px" @click="copyPath(preview.item.filePath)">
										<CopyDocument></CopyDocument>
									</el-icon>
								</el-tooltip>
							</el-col>
							<el-col :span="7" style="text-align: right">
								<el-button size="small" @click="handleCopy(preview.item.content)">复制代码</el-button>
								<el-button size="small" @click="downloadTemplateData(preview.item)">生成代码</el-button>
								<el-button size="small" @click="toggle()">{{ isFullscreen ? '退出全屏' : '全屏展示' }}</el-button>
							</el-col>
						</el-row>
					</el-header>

					<!-- 代码区域 -->
					<el-main style="padding: 10px; overflow: hidden">
						<el-tabs v-model="preview.tabActiveName" tab-position="top" @tab-click="handleTabClick" @tab-remove="handleTabRemove">
							<el-tab-pane v-for="tabItem in preview.tabList" :key="tabItem.filePath" :name="tabItem.filePath" :label="tabItem.fileName" closable>
							</el-tab-pane>
						</el-tabs>
						<el-scrollbar style="height: 100%">
							<code-mirror v-model="preview.item.content" :height="contentHeight"></code-mirror>
						</el-scrollbar>
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

interface Tree {
	label: string
	filePath: string
	templateId: number
	children?: Tree[]
}

interface TemplateContent {
	// 表ID
	tableId?: number | null
	// 枚举ID
	enumId?: number | null
	// 模板ID
	templateId?: number | null
	// 模板组类型
	templateGroupType?: number | null
	// 模板类型
	templateType?: number | null
	// 文件路径
	filePath: string
	// 文件名称
	fileName: string
	// 内容
	content: string
}

const currentNodeKey = ref()
const treeRef = ref()
const preview = reactive({
	visible: false,
	title: '代码预览',
	dataList: [] as TemplateContent[],
	projectId: 0,
	id: -1,
	generatorType: -1,
	treeData: [] as Tree[],
	item: {
		tableId: null,
		templateId: null,
		enumId: null,
		templateGroupType: null,
		templateType: null,
		filePath: '',
		fileName: '',
		content: ''
	} as TemplateContent,
	tabList: [] as TemplateContent[],
	tabActiveName: ''
})

const treeSearchText = ref('')
const isCollapseRef = ref(false)
const { isFullscreen, toggle } = useFullscreen()

// 初始化方法
const init = async (id: number, projectId: number, generatorType: number, generatorProductType: number) => {
	preview.id = id
	preview.projectId = projectId
	preview.generatorType = generatorType
	isCollapseRef.value = false
	const loadingInstance = ElLoading.service({ fullscreen: true })
	try {
		const previewData = {
			previewProductId: id,
			generatorProductType: generatorProductType
		}
		const res = await generatorPreviewApi(previewData)
		const { templateContentList, treeList } = res.data
		preview.dataList = templateContentList
		preview.treeData = treeList
		preview.item = templateContentList[0]

		preview.visible = true
		await nextTick()
		currentNodeKey.value = templateContentList[0].filePath
		treeRef.value.setCurrentKey(currentNodeKey.value)
		preview.tabActiveName = templateContentList[0].filePath
		preview.tabList.push(templateContentList[0])
	} finally {
		loadingInstance.close()
	}
}

const toggleCollapse = () => {
	isCollapseRef.value = !isCollapseRef.value
}

// 计算内容行数
const contentHeight = computed(() => {
	const length = preview.item.content.split('\n').length
	return Math.min(Math.max(20 * length, 800), 1000)
})
watch(treeSearchText, val => {
	treeRef.value!.filter(val)
})

// tab点击
const handleTabClick = (tab: TabsPaneContext, _: Event) => {
	const filePath = tab.paneName as string
	const matchedItem = preview.tabList.find(item => item.filePath === filePath)
	if (matchedItem) {
		preview.item = matchedItem
		currentNodeKey.value = matchedItem.filePath
		treeRef.value.setCurrentKey(currentNodeKey.value)
	}
}

// tab删除
const handleTabRemove = (filePath: string) => {
	// 首页保护逻辑（当只有一个标签且是首页时不允许关闭）
	// 找到索引
	const index = preview.tabList.findIndex(item => item.filePath === filePath)
	if (index <= -1) {
		return
	}
	// 删除tab
	preview.tabList.splice(index, 1)
	let newTabActiveName = preview.tabActiveName
	// 删除的是否为当前tab
	if (preview.tabActiveName === filePath && preview.tabList.length > 0) {
		// 优先尝试右侧标签
		if (index < preview.tabList.length) {
			newTabActiveName = preview.tabList[index].filePath
		} else {
			// 右侧无标签时选择左侧最后一个
			newTabActiveName = preview.tabList[preview.tabList.length - 1].filePath
		}
	}
	if (preview.tabList.length > 0) {
		// 设置新的激活项
		const activeTab = preview.tabList.find(tab => tab.filePath === newTabActiveName)
		if (activeTab) {
			preview.item = activeTab
		}
		preview.tabActiveName = newTabActiveName
		currentNodeKey.value = newTabActiveName
		treeRef.value?.setCurrentKey(currentNodeKey.value)
	}
}

// tree点击
const handleTreeNodeClick = (data: Tree) => {
	if (data.templateId) {
		const filterElement = preview.dataList.filter(item => item.filePath === data.filePath)[0]
		preview.item = filterElement
		const result = preview.tabList.some(tab => tab.filePath === filterElement.filePath)
		if (!result) {
			preview.tabList.push(filterElement)
			preview.tabActiveName = filterElement.filePath
		}
	}
}

// 代码复制到剪切板
const handleCopy = (content: string) => {
	copyToClipboard(content).then(() => {
		ElMessage.success('代码已复制到剪贴板')
	})
}

// 全文件名复制到剪切板
const copyPath = (path: string) => {
	copyToClipboard(path).then(() => {
		ElMessage.success('全文件名已复制到剪贴板')
	})
}

//下载单个模板代码
const downloadTemplateData = (item: TemplateContent) => {
	let id
	if (item.templateGroupType === 0) {
		id = preview.projectId
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
	if (preview.generatorType === 0) {
		generatorDownloadSingleApi(dataForm)
	} else if (preview.generatorType === 1) {
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
	return data.label.includes(value)
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
