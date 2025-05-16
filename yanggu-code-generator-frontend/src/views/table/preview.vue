<template>
	<!-- 预览界面 -->
	<el-drawer v-model="preview.visible" title="代码预览" :size="1200">
		<div class="common-layout">
			<el-container>
				<el-aside :style="{ width: '300px', overflowX: 'auto' }">
					<div class="tree-scroll-wrapper">
						<el-tree
							:data="preview.treeData"
							node-key="filePath"
							:current-node-key="currentNodeKey"
							highlight-current
							class="custom-tree"
							@node-click="handleTreeNodeClick"
						/>
					</div>
				</el-aside>
				<el-main>
					<el-tabs v-model="preview.activeName" tab-position="top" @tab-click="handleTabNodeClick">
						<el-tab-pane v-for="(item, key) in preview.data" :key="key" :label="item.fileName" :name="item.fileName">
							<el-row :gutter="10">
								<el-col :span="18"></el-col>
								<el-col :span="6" style="text-align: right">
									<el-button @click="handleCopy(item.content)">复制</el-button>
									<el-button @click="generatorCode(item)">生成代码</el-button>
								</el-col>
							</el-row>
							<code-mirror v-model="item.content" :height="680"></code-mirror>
						</el-tab-pane>
					</el-tabs>
				</el-main>
			</el-container>
		</div>
	</el-drawer>
</template>
<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import { ElLoading } from 'element-plus'
import { generatorTableDownloadApi, generatorTableDownloadLocalApi, generatorTablePreviewApi, generatorTableTreeDataApi } from '@/api/generator'
import CodeMirror from '@/components/codemirror/CodeMirror.vue'
import { ElMessage } from 'element-plus'
import { TabsPaneContext } from 'element-plus/es'

const currentNodeKey = ref()
const preview = reactive({
	visible: false,
	title: '代码预览',
	data: [],
	tableId: 0,
	generatorType: null,
	treeData: [],
	activeName: ''
})

interface Tree {
	label: string
	templateId: number
	children?: Tree[]
}

const handleTreeNodeClick = (data: Tree) => {
	if (data.templateId) {
		preview.activeName = data.label
	}
}

const handleTabNodeClick = (pane: TabsPaneContext, ev: Event) => {
	// 1. 获取当前tab的name
	const currentTabName = pane.props.name

	// 2. 遍历preview.data查找对应项
	const currentItem = Object.values(preview.data).find(item => {
		return item.fileName === currentTabName
	})

	// 3. 现在currentItem就是当前选中的数据项
	currentNodeKey.value = currentItem.filePath
}

const init = async (tableItem: any) => {
	const tableId = tableItem.id
	preview.generatorType = tableItem.generatorType
	const loadingInstance = ElLoading.service({ fullscreen: true })
	try {
		const dataForm = {
			tableId: tableId
		}
		let restTreeData = await generatorTableTreeDataApi(dataForm)
		preview.treeData = restTreeData.data

		let resData = await generatorTablePreviewApi(dataForm)
		preview.data = resData.data
		preview.visible = true
		preview.tableId = tableId
		preview.activeName = resData.data[0].fileName

		// 确保数据加载完成后再设置树节点
		await nextTick() // 等待 DOM 更新
		currentNodeKey.value = resData.data[0].filePath
	} finally {
		loadingInstance.close()
	}
}
defineExpose({
	init
})

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

//生成代码
const generatorCode = item => {
	const tableId = item.tableId
	if (preview.generatorType === 0) {
		const dataForm = {
			tableId: tableId,
			templateId: item.templateId
		}
		generatorTableDownloadApi(dataForm)
	} else if (preview.generatorType === 1) {
		const dataForm = {
			tableId: tableId,
			templateIdList: [item.templateId]
		}
		generatorTableDownloadLocalApi(dataForm).then(() => {
			ElMessage.success({
				message: '代码已经下载到本地',
				duration: 1000
			})
		})
	}
}
</script>

<style scoped>
.tree-scroll-wrapper {
	width: 100%;
	overflow-x: auto;
	white-space: nowrap; /* 防止节点内容换行 */
}

.custom-tree {
	display: inline-block; /* 使树形结构横向排列 */
	min-width: max-content; /* 根据内容自适应宽度 */
}

/* 覆盖 Element Plus 默认样式 */
::v-deep .el-tree-node {
	min-width: 100% !important; /* 确保节点占满容器宽度 */
	.el-tree-node__children {
		overflow: visible !important; /* 防止子节点溢出隐藏 */
	}
}
</style>
