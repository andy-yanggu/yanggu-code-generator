<template>
	<el-drawer v-model="testData.visible" :title="`${testData.templateName} - 模板测试`" size="100%" :modal="false" class="template-test-drawer">
		<el-container class="full-height">
			<!-- 左侧：选择面板（独立滚动） -->
			<el-aside v-show="!testData.asideCollapsed" class="aside-scroll">
				<h3 style="margin-bottom: 20px">请选择项目、表或者枚举进行测试</h3>
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
						<el-col :span="19">
							<el-tooltip :content="fullFilePath" :disabled="!fullFilePath" placement="top">
								<el-text style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; width: 100%">
									路径：{{ fullFilePath }}
								</el-text>
							</el-tooltip>
						</el-col>
						<el-col :span="4" style="text-align: right">
							<el-button
								v-if="testData.activeName === 'template'"
								size="small"
								type="primary"
								:icon="Edit"
								:loading="submitLoading"
								:disabled="testData.editTemplateContent === testData.originalTemplateContent"
								@click="saveTemplateContent()"
							>
								保存
							</el-button>
							<div v-else>
								<el-button
									size="small"
									:icon="DocumentAdd"
									:disabled="!testData.cascaderValue"
									type="success"
									:loading="submitLoading"
									@click="generatorHandler()"
								>
									生成
								</el-button>
								<el-button size="small" :icon="Refresh" :disabled="!testData.cascaderValue" @click="refreshHandler()">刷新</el-button>
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
import { computed, nextTick, reactive, ref } from 'vue'
import CodeMirror from '@/components/code-mirror/index.vue'
import { templateDetailApi, templateUpdateContentApi } from '@/api/gen/template'
import { cascaderDataApi } from '@/api/gen/template-group'
import { ElLoading, ElMessage } from 'element-plus'
import { DocumentAdd, Edit, Expand, Fold, Refresh } from '@element-plus/icons-vue'
import { generatorDownloadSingleApi, generatorSingleLocalApi, generatorTemplateTestApi } from '@/api/gen/generator'

interface CascaderData {
	value: string
	label: string
	type: 'project' | 'table' | 'enum'
	id: number
	generatorType: number
	children?: CascaderData[]
}

const testData = reactive({
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
})

const init = async (templateGroupId: number, templateGroupType: number, templateId: number) => {
	testData.visible = true
	testData.templateGroupId = templateGroupId
	testData.templateGroupType = templateGroupType
	testData.templateId = templateId
	testData.activeName = 'template'

	// 获取项目ID列表
	const res = await cascaderDataApi({ templateGroupType, templateGroupId })
	if (!res.data.length) {
		if (templateGroupType === 0) {
			ElMessage.warning('请先关联项目')
		} else if (templateGroupType === 1) {
			ElMessage.warning('请先关联表')
		} else if (templateGroupType === 2) {
			ElMessage.warning('请先关联枚举')
		}
		return
	}
	testData.cascaderData = res.data

	// 获取模板详情
	const detailQueryForm = { id: templateId, setPath: true }
	templateDetailApi(detailQueryForm).then(res => {
		testData.templateName = res.data.templateName
		testData.originalFileName = res.data.fileName
		testData.originalTemplateContent = res.data.templateContent
		testData.editTemplateContent = res.data.templateContent
		testData.fullFilePath = res.data.generatorPath
	})
}

const submitLoading = ref(false)

const fullFilePath = computed(() => {
	return testData.activeName === 'template' ? testData.fullFilePath : testData.renderedFileName
})

// 保存模板内容
const saveTemplateContent = () => {
	submitLoading.value = true
	const dataForm = {
		id: testData.templateId,
		templateContent: testData.editTemplateContent
	}
	templateUpdateContentApi(dataForm)
		.then(() => {
			ElMessage.success({
				message: '保存成功',
				duration: 500
			})
			testData.originalTemplateContent = testData.editTemplateContent
		})
		.finally(() => {
			submitLoading.value = false
		})
}

// 处理选中值变化
const handleCascaderChange = async val => {
	// val 是选中节点的值数组
	const queryForm = {
		projectId: val[0].split('_')[1],
		templateGroupId: testData.templateGroupId,
		templateGroupType: testData.templateGroupType
	}
	if (testData.templateGroupType === 0) {
		queryForm.projectTemplateIdList = [testData.templateId]
	} else if (testData.templateGroupType === 1) {
		queryForm.tableTemplateIdList = [testData.templateId]
		queryForm.tableIdList = [val[1].split('_')[1]]
	} else if (testData.templateGroupType === 2) {
		queryForm.enumTemplateIdList = [testData.templateId]
		queryForm.enumIdList = [val[1].split('_')[1]]
	}
	const loadingInstance = ElLoading.service({
		target: '.template-test-drawer',
		text: '模板渲染中...'
	})
	try {
		const res = await generatorTemplateTestApi(queryForm)
		testData.renderedFileName = res.data.filePath
		testData.renderedTemplateContent = res.data.content
		nextTick(() => {
			testData.activeName = 'render'
		})
	} finally {
		loadingInstance.close()
	}
}

const refreshHandler = () => {
	testData.activeName = 'render'
	handleCascaderChange(testData.cascaderValue)
}

const generatorHandler = () => {
	let id
	const projectId = Number(testData.cascaderValue[0].split('_')[1])
	if (testData.cascaderValue.length > 1) {
		id = testData.cascaderValue[1].split('_')[1]
	} else {
		id = testData.cascaderValue[0].split('_')[1]
	}
	const dataForm = {
		id: id,
		templateId: testData.templateId,
		templateGroupType: testData.templateGroupType
	}
	const generatorType = testData.cascaderData.find(item => item.id === projectId)!.generatorType
	if (generatorType === 0) {
		submitLoading.value = true
		generatorDownloadSingleApi(dataForm)
			.then(() => {
				ElMessage.success({
					message: '代码已经下载到浏览器',
					duration: 1000
				})
			})
			.finally(() => {
				submitLoading.value = false
			})
	} else if (generatorType === 1) {
		submitLoading.value = true
		generatorSingleLocalApi(dataForm)
			.then(() => {
				ElMessage.success({
					message: '代码已经下载到本地',
					duration: 1000
				})
			})
			.finally(() => {
				submitLoading.value = false
			})
	}
}

defineExpose({ init })
</script>

<style scoped>
.full-height {
	height: 100%;
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
