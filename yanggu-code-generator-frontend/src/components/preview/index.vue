<template>
	<!-- 预览界面 -->
	<el-drawer v-model="preview.visible" title="代码预览" :size="'100%'" :modal="false">
		<el-container style="height: 100%">
			<!-- 左侧：树结构 -->
			<el-aside v-show="!isCollapseRef" width="300px" style="overflow: hidden">
				<div style="padding: 15px; border-bottom: 1px solid #eee">
					<el-input v-model="treeSearchText" placeholder="搜索文件/路径" size="small" clearable prefix-icon="Search"></el-input>
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
			<el-main ref="codeContainer" style="padding: 0; overflow: hidden" :class="{ 'full-screen-mode': isFullscreen }">
				<el-container style="height: 100%">
					<!-- 头部操作区域 -->
					<el-header style="display: flex; flex-direction: column; gap: 10px; padding: 10px">
						<el-row>
							<el-col v-if="!isFullscreen" :span="1">
								<el-icon :size="22" class="collapse-icon" @click="toggleCollapse()">
									<Expand v-if="isCollapseRef" />
									<Fold v-else />
								</el-icon>
							</el-col>
							<el-col :span="23">
								路径：<el-text>{{ preview.item.filePath }}</el-text>
								<el-tooltip content="复制路径" placement="top" effect="light">
									<el-icon style="cursor: pointer; margin-left: 10px" @click="copyPath(preview.item.filePath)">
										<CopyDocument />
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
								<el-button size="small" @click="toggle()">{{ isFullscreen ? '退出全屏' : '全屏展示' }}</el-button>
							</el-col>
						</el-row>
					</el-header>

					<!-- 代码区域 -->
					<el-main style="padding: 10px; overflow: hidden">
						<el-scrollbar style="height: 100%">
							<code-mirror v-model="preview.item.content" :height="contentHeight"></code-mirror>
						</el-scrollbar>
					</el-main>
				</el-container>
			</el-main>
		</el-container>
	</el-drawer>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ElLoading, ElMessage } from 'element-plus'
import CodeMirror from '@/components/code-mirror/index.vue'
import { generatorDownloadSingleApi, generatorSingleLocalApi, generatorPreviewApi } from '@/api/generator'
import { CopyDocument, Expand, Fold } from '@element-plus/icons-vue'
import { copyToClipboard } from '@/utils/tool'
import { useFullscreen } from '@vueuse/core'

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

const treeSearchText = ref('')
const isCollapseRef = ref(false)
const codeContainer = ref<HTMLElement>()
const { isFullscreen, toggle } = useFullscreen(codeContainer)

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
	copyToClipboard(content).then(() => {
		ElMessage.success('代码已复制到剪贴板')
	})
}

const copyPath = (path: string) => {
	copyToClipboard(path).then(() => {
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

.el-message {
	z-index: 4000 !important;
}

.el-tooltip__popper {
	z-index: 4000 !important;
}
</style>
