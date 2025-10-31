<template>
	<el-drawer v-model="testData.visible" :title="`${testData.templateName} - 模板测试`" size="100%" class="template-test-drawer" destroy-on-close>
		<el-container class="full-height">
			<!-- 左侧：选择面板（独立滚动） -->
			<el-aside v-show="!testData.asideCollapsed" class="aside-scroll">
				<div class="aside-header">
					<el-tooltip content="刷新级联数据" placement="top">
						<el-button :icon="Refresh" size="small" type="primary" :loading="loading" @click="refreshCascaderData()">刷新</el-button>
					</el-tooltip>
					<el-tooltip content="根据当前选择重新渲染模板" placement="top">
						<el-button :icon="Document" size="small" :disabled="!testData.cascaderValue" @click="refreshHandler()">渲染</el-button>
					</el-tooltip>
				</div>
				<el-cascader
					v-model="testData.cascaderValue"
					:options="testData.cascaderData"
					filterable
					clearable
					placeholder="请选择项目、表或者枚举"
					@change="handleCascaderChange"
				></el-cascader>
			</el-aside>

			<!-- 右侧：内容区 -->
			<el-container direction="vertical" class="right-container">
				<!-- 顶部工具栏（固定） -->
				<el-header class="header-fixed">
					<el-row>
						<!-- 展开折叠按钮 -->
						<el-col :span="1">
							<el-icon :size="20" class="collapse-icon" @click="testData.asideCollapsed = !testData.asideCollapsed">
								<Expand v-if="testData.asideCollapsed"></Expand>
								<Fold v-else></Fold>
							</el-icon>
						</el-col>
						<!-- 文件路径	-->
						<el-col :span="testData.activeName === 'template' ? 21 : 19">
							<text-tooltip :title="'路径：' + fullFilePath" max-width="100%"></text-tooltip>
						</el-col>
						<el-col :span="testData.activeName === 'template' ? 2 : 4" style="text-align: right">
							<el-button
								v-if="testData.activeName === 'template'"
								size="small"
								type="primary"
								:icon="Edit"
								:loading="loading"
								:disabled="testData.editTemplateContent === testData.originalTemplateContent"
								@click="saveTemplateContent()"
							>
								保存
							</el-button>
							<div v-else>
								<el-button
									size="small"
									type="primary"
									:icon="CopyDocument"
									:disabled="!testData.renderedTemplateContent"
									@click="copyTemplateContent()"
								>
									复制
								</el-button>
								<el-button
									size="small"
									:icon="DocumentAdd"
									:disabled="!testData.cascaderValue"
									type="success"
									:loading="loading"
									@click="generatorHandler()"
								>
									生成
								</el-button>
							</div>
						</el-col>
					</el-row>

					<el-tabs v-model="testData.activeName">
						<el-tab-pane name="template">
							<template #label>
								<div class="tab-label">
									<span>原始模板</span>
									<span v-if="testData.editTemplateContent !== testData.originalTemplateContent" class="edit-dot"></span>
								</div>
							</template>
						</el-tab-pane>
						<el-tab-pane name="render" label="渲染结果"></el-tab-pane>
					</el-tabs>
				</el-header>

				<!-- 主体内容（独立滚动） -->
				<el-main class="main-scroll">
					<el-scrollbar v-show="testData.activeName === 'template'">
						<code-mirror v-model="testData.editTemplateContent"></code-mirror>
					</el-scrollbar>
					<el-scrollbar v-show="testData.activeName === 'render'">
						<code-mirror v-model="testData.renderedTemplateContent" :read-only="true"></code-mirror>
					</el-scrollbar>
				</el-main>
			</el-container>
		</el-container>
	</el-drawer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import CodeMirror from '@/components/code-mirror/index.vue'
import { genTemplateApi } from '@/api/gen/template'
import { genTemplateGroupApi } from '@/api/gen/template-group'
import { ElLoading, ElMessage } from 'element-plus'
import { CopyDocument, Document, DocumentAdd, Edit, Expand, Fold, Refresh } from '@element-plus/icons-vue'
import { genGeneratorApi } from '@/api/gen/generator'
import { cloneObject, copyToClipboard, resetReactiveObject } from '@/utils/tool'
import TextTooltip from '@/components/text-tooltip/index.vue'

interface CascaderData {
	value: string
	label: string
	type: 'project' | 'table' | 'enum'
	id: number
	generatorType: number
	children?: CascaderData[]
}

const INIT_TEST_DATA = {
	visible: false,
	asideCollapsed: false,
	activeName: 'template',
	templateGroupId: 0,
	templateGroupType: 0,
	templateId: 0,
	templateName: '',
	originalFileName: '',
	originalTemplateContent: '',
	editTemplateContent: '',
	fullFilePath: '',
	renderedFileName: '',
	renderedTemplateContent: '',
	cascaderValue: '',
	cascaderData: [] as CascaderData[]
}

const testData = reactive(cloneObject(INIT_TEST_DATA))

const init = async (templateGroupId: number, templateGroupType: number, templateId: number) => {
	resetReactiveObject(testData, INIT_TEST_DATA)
	testData.visible = true
	testData.templateGroupId = templateGroupId
	testData.templateGroupType = templateGroupType
	testData.templateId = templateId

	// 获取级联数据
	await refreshCascaderData()
}

const loading = ref(false)

const fullFilePath = computed(() => {
	return testData.activeName === 'template' ? testData.fullFilePath : testData.renderedFileName
})

// 保存模板内容
const saveTemplateContent = () => {
	loading.value = true
	const dataForm = {
		id: testData.templateId,
		templateContent: testData.editTemplateContent
	}
	genTemplateApi
		.updateContent(dataForm)
		.then(() => {
			ElMessage.success({
				message: '保存成功',
				duration: 500
			})
			testData.originalTemplateContent = testData.editTemplateContent
		})
		.finally(() => {
			loading.value = false
		})
}

// 处理选中值变化
const handleCascaderChange = async val => {
	// val 是选中节点的值数组
	const queryForm = {
		projectId: val[0].split('_')[1],
		templateGroupId: testData.templateGroupId,
		templateGroupType: testData.templateGroupType,
		templateId: testData.templateId,
		templateContent: testData.editTemplateContent,
		testId: 0
	}
	if (testData.templateGroupType === 0) {
		queryForm.testId = testData.templateId
	} else if (testData.templateGroupType === 1) {
		queryForm.testId = val[1].split('_')[1]
	} else if (testData.templateGroupType === 2) {
		queryForm.testId = val[1].split('_')[1]
	}
	const loadingInstance = ElLoading.service({
		target: '.template-test-drawer .el-drawer__body',
		text: '模板渲染中...'
	})
	try {
		const data = await genGeneratorApi.templateTest(queryForm)
		testData.renderedFileName = data.filePath
		testData.renderedTemplateContent = data.content
		testData.activeName = 'render'
	} finally {
		loadingInstance.close()
	}
}

const refreshCascaderData = async () => {
	testData.activeName = 'template'
	// 获取级联数据
	const templateGroupType = testData.templateGroupType
	const queryForm = {
		templateGroupType,
		templateGroupId: testData.templateGroupId
	}
	loading.value = true
	const data = await genTemplateGroupApi.cascaderData(queryForm)
	loading.value = false
	if (!data.length) {
		if (templateGroupType === 0) {
			ElMessage.warning('请先关联项目')
		} else if (templateGroupType === 1) {
			ElMessage.warning('请先关联表')
		} else if (templateGroupType === 2) {
			ElMessage.warning('请先关联枚举')
		}
		return
	}
	testData.cascaderData = data

	// 获取模板详情
	const detailQueryForm = {
		id: testData.templateId,
		setPath: true
	}
	genTemplateApi.detail(detailQueryForm).then(data => {
		testData.templateName = data.templateName
		testData.originalFileName = data.fileName
		testData.originalTemplateContent = data.templateContent
		testData.editTemplateContent = data.templateContent
		testData.fullFilePath = data.generatorPath
	})
}

// 刷新渲染数据
const refreshHandler = () => {
	testData.activeName = 'render'
	handleCascaderChange(testData.cascaderValue)
}

const generatorHandler = () => {
	let id: number
	const projectId = Number(testData.cascaderValue[0].split('_')[1])
	if (testData.cascaderValue.length > 1) {
		id = Number.parseInt(testData.cascaderValue[1].split('_')[1])
	} else {
		id = Number.parseInt(testData.cascaderValue[0].split('_')[1])
	}
	const dataForm = {
		id: id,
		templateId: testData.templateId,
		templateGroupType: testData.templateGroupType
	}
	const generatorType = testData.cascaderData.find(item => item.id === projectId)!.generatorType
	if (generatorType === 0) {
		loading.value = true
		genGeneratorApi
			.downloadSingle(dataForm)
			.then(() => {
				ElMessage.success({
					message: '代码已经下载到浏览器',
					duration: 1000
				})
			})
			.finally(() => {
				loading.value = false
			})
	} else if (generatorType === 1) {
		loading.value = true
		genGeneratorApi
			.singleLocal(dataForm)
			.then(() => {
				ElMessage.success({
					message: '代码已经下载到本地',
					duration: 1000
				})
			})
			.finally(() => {
				loading.value = false
			})
	}
}

const copyTemplateContent = () => {
	copyToClipboard(testData.renderedTemplateContent).then(() => {
		ElMessage.success('代码已复制到剪贴板')
	})
}

defineExpose({ init })
</script>

<style scoped>
.full-height {
	height: 100%;
}

.aside-header {
	width: 100%;
	display: flex;
	justify-content: center; /* 水平居中 */
	align-items: center; /* 垂直居中 */
	margin: 0 0 20px 0; /* 去掉左外边距，只保留下边距 */
}

.aside-scroll {
	height: 100%;
	overflow: auto;
	border-right: 1px solid #e5e5e5;
	padding: 16px;
	box-sizing: border-box;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.collapse-icon {
	cursor: pointer;
}

.right-container {
	display: flex;
	flex-direction: column;
	height: 100%;
}

.header-fixed {
	height: auto;
	flex-shrink: 0;
}

.main-scroll {
	flex: 1;
	overflow: auto;
	padding: 0 16px 16px;
}
.tab-label {
	position: relative;
	display: inline-block;
}

/* 仅在包含编辑红点时添加右边距 */
.tab-label:has(.edit-dot) {
	padding-right: 10px;
}

/* 通用红点 */
.edit-dot {
	position: absolute;
	top: 2px;
	right: 0;
	width: 6px;
	height: 6px;
	background-color: #f56c6c;
	border-radius: 50%;
	display: inline-block;
}
</style>
