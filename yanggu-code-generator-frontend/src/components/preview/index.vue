<template>
	<!-- 预览界面 -->
	<el-drawer v-model="preview.visible" title="代码预览" :size="'100%'">
		<el-scrollbar>
			<el-container>
				<el-aside v-show="!isCollapseRef" :style="{ width: '300px', overflowX: 'auto' }">
					<div class="tree-scroll-wrapper">
						<el-scrollbar>
							<el-tree
								ref="treeRef"
								:data="preview.treeData"
								node-key="filePath"
								:current-node-key="currentNodeKey"
								highlight-current
								class="custom-tree"
								@node-click="handleTreeNodeClick"
							></el-tree>
						</el-scrollbar>
					</div>
				</el-aside>
				<el-main>
					<el-container>
						<el-header style="display: flex; flex-direction: column; gap: 10px">
							<el-row>
								<el-col :span="1">
									<el-icon :size="22" class="collapse-icon" @click="toggleCollapse()">
										<Expand v-if="isCollapseRef"></Expand>
										<Fold v-else></Fold>
									</el-icon>
								</el-col>
								<el-col :span="23">
									路径：<el-text>{{ preview.item.filePath }}</el-text>
									<el-tooltip content="点击复制路径" placement="top-start" effect="light">
										<el-icon style="cursor: pointer; margin-left: 10px" title="点击复制路径" @click="copyPath(preview.item.filePath)">
											<CopyDocument></CopyDocument>
										</el-icon>
									</el-tooltip>
								</el-col>
							</el-row>
							<el-row>
								<el-col :span="12">
									名称：<el-text>{{ preview.item.fileName }}</el-text>
								</el-col>
								<el-col :span="12" style="text-align: right">
									<el-button size="small" @click="handleCopy(preview.item.content)">复制代码</el-button>
									<el-button size="small" @click="downloadTemplateData(preview.item)">生成代码</el-button>
									<el-button size="small" @click="handleFullScreen">全屏展示</el-button>
								</el-col>
							</el-row>
						</el-header>
						<el-main ref="codeContainer" style="margin-top: 10px">
							<code-mirror v-model="preview.item.content" :height="contentHeight" :class="{ 'full-screen-mode': isFullScreen }"></code-mirror>
						</el-main>
					</el-container>
				</el-main>
			</el-container>
		</el-scrollbar>
	</el-drawer>
</template>
<script setup lang="ts">
import { computed, nextTick, reactive, ref } from 'vue'
import { ElLoading, ElMessage } from 'element-plus'
import CodeMirror from '@/components/code-mirror/code-mirror.vue'
import { generatorDownloadSingleApi, generatorSingleLocalApi, generatorPreviewApi } from '@/api/generator'
import { CopyDocument, Expand, Fold } from '@element-plus/icons-vue'

const currentNodeKey = ref()
const treeRef = ref()
const preview = reactive({
	visible: false,
	title: '代码预览',
	dataList: [],
	projectId: 0,
	id: -1,
	generatorType: -1,
	treeData: [],
	item: {
		filePath: '',
		fileName: '',
		content: '',
		tableId: null
	}
})

const isCollapseRef = ref(false)

const toggleCollapse = () => {
	isCollapseRef.value = !isCollapseRef.value
}

// 计算内容行数
const contentHeight = computed(() => {
	const length = preview.item.content.split('\n').length
	return Math.min(Math.max(20 * length, 800), 1000)
})

interface Tree {
	label: string
	filePath: string
	templateId: number
	children?: Tree[]
}

const handleTreeNodeClick = (data: Tree) => {
	if (data.templateId) {
		preview.item = preview.dataList.filter(item => item.filePath === data.filePath)[0]
	}
}

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
	} finally {
		loadingInstance.close()
	}
}

//复制到剪切板
const handleCopy = (content: string) => {
	// 新增环境检测逻辑
	if (!navigator || !navigator.clipboard || !navigator.clipboard.writeText) {
		ElMessage.error('当前浏览器不支持复制功能，或者使用https域名')
		return
	}

	navigator.clipboard
		.writeText(content)
		.then(() => {
			ElMessage.success('代码已复制到剪贴板')
		})
		.catch(() => {
			ElMessage.error('复制失败，请重试')
		})
}

const copyPath = (path: string) => {
	navigator.clipboard.writeText(path).then(() => {
		ElMessage.success('路径已复制到剪贴板')
	})
}

//下载单个模板代码
const downloadTemplateData = (item: any) => {
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

const isFullScreen = ref(false)
const codeContainer = ref<HTMLElement>()

const handleFullScreen = () => {
	if (!codeContainer.value) {
		return
	}

	if (!document.fullscreenElement) {
		isFullScreen.value = true
		const container = codeContainer.value.$el || codeContainer.value

		if (container.requestFullscreen) {
			container.requestFullscreen().catch(err => {
				ElMessage.error(`全屏请求失败: ${err}`)
				isFullScreen.value = false
			})
		} else {
			// 处理浏览器前缀
			const docElm = document.documentElement as any
			if (docElm.webkitRequestFullScreen) {
				docElm.webkitRequestFullScreen()
			}
			ElMessage.warning('当前浏览器不支持原生全屏功能')
		}
	} else {
		isFullScreen.value = false
		if (document.exitFullscreen) {
			document.exitFullscreen()
		}
	}
}

// 监听全屏状态变化
document.addEventListener('fullscreenchange', () => {
	isFullScreen.value = !!document.fullscreenElement
})

defineExpose({
	init
})
</script>

<style scoped>
.tree-scroll-wrapper {
	width: 100%;
	overflow-x: auto;
	white-space: nowrap; /* 防止节点内容换行 */
}
.collapse-icon {
	cursor: pointer;
}

.custom-tree {
	display: inline-block; /* 使树形结构横向排列 */
	min-width: max-content; /* 根据内容自适应宽度 */
}

/* 覆盖 Element Plus 默认样式 */

:deep(.full-screen-mode) {
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	z-index: 9999;
	background: white;
	margin: 0 !important;
	padding: 20px;
}
</style>
