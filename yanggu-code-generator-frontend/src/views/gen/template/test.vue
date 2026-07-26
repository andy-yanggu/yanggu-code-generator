<template>
	<el-drawer v-model="testData.visible" size="100%" class="template-test-drawer" destroy-on-close :before-close="handleClose">
		<template #header>
			<el-link
				type="primary"
				class="el-drawer__title"
				style="font-size: 16px"
				underline="never"
				@click="formInitHandle({ type: 'detail', id: testData.templateId })"
			>
				{{ `模板测试（${testData.templateName}）` }}
			</el-link>
			<el-tree-select
				v-model="testData.templateId"
				:data="treeList"
				node-key="id"
				:current-node-key="testData.templateId"
				highlight-current
				:props="{ label: 'fileName' }"
				filterable
				style="width: 300px"
				placeholder="请选择模板"
			>
				<template #default="{ data }">
					<text-tooltip :title="data.fileName" max-width="100%"> </text-tooltip>
				</template>
			</el-tree-select>
		</template>
		<el-splitter>
			<!-- 左侧：选择面板（独立滚动） -->
			<el-splitter-panel collapsible size="25%">
				<el-row justify="center" style="margin: 0 0 20px 0">
					<el-tooltip content="刷新级联数据" placement="top">
						<el-button :icon="Refresh" size="small" type="primary" :loading="loading" @click="refreshCascaderData()">刷新</el-button>
					</el-tooltip>
					<el-tooltip content="根据当前选择重新渲染模板" placement="top">
						<el-button
							:icon="Document"
							size="small"
							:disabled="isEmpty(testData.cascaderValue)"
							@click="handleCascaderChange(testData.cascaderValue)"
						>
							渲染
						</el-button>
					</el-tooltip>
				</el-row>
				<el-row justify="center">
					<el-cascader
						v-model="testData.cascaderValue"
						:options="testData.cascaderData"
						:props="{
							expandTrigger: 'hover'
						}"
						filterable
						clearable
						placeholder="请选择项目、表或者枚举"
						style="width: 300px"
						@change="handleCascaderChange"
					>
						<template #default="{ data }">
							<option-label :label="data.label" :desc="data.description"></option-label>
						</template>
					</el-cascader>
				</el-row>
			</el-splitter-panel>

			<!-- 右侧：内容区 -->
			<el-splitter-panel>
				<el-container direction="vertical" class="right-container">
					<!-- 顶部工具栏（固定） -->
					<el-header class="header-fixed">
						<el-row>
							<!-- 文件路径	-->
							<el-col :span="19">
								<div class="path-container">
									<text-tooltip :title="'路径：' + fullFilePath" max-width="100%" :tooltip-content="fullFilePath"></text-tooltip>
									<el-tooltip v-if="testData.activeName === 'render'" :disabled="copyIconState === Check" content="复制文件路径" placement="top">
										<el-icon style="cursor: pointer; margin-left: 5px" @click="copyPath(fullFilePath)">
											<component :is="copyIconState"></component>
										</el-icon>
									</el-tooltip>
								</div>
							</el-col>
							<el-col :span="5" style="text-align: right">
								<template v-if="testData.activeName === 'template'">
									<el-button
										:icon="Document"
										size="small"
										:disabled="isEmpty(testData.cascaderValue)"
										@click="handleCascaderChange(testData.cascaderValue)"
									>
										渲染
									</el-button>
									<el-button size="small" type="primary" :icon="Edit" :loading="loading" :disabled="!isEdit" @click="saveTemplateContent()">
										保存
									</el-button>
								</template>
								<template v-else>
									<el-tooltip content="复制代码" :disabled="copyCodeState.icon === Check" placement="top">
										<el-button
											size="small"
											type="primary"
											:icon="copyCodeState.icon"
											:disabled="!testData.renderedTemplateContent"
											@click="copyTemplateContent()"
										>
											{{ copyCodeState.text }}
										</el-button>
									</el-tooltip>
									<el-tooltip :content="generatorState.tooltip" placement="top">
										<el-button
											size="small"
											:icon="generatorState.icon"
											:disabled="isEmpty(testData.cascaderValue)"
											type="success"
											:loading="submitLoading"
											@click="generatorHandler()"
										>
											{{ generatorState.text }}
										</el-button>
									</el-tooltip>
								</template>
							</el-col>
						</el-row>

						<el-tabs v-model="testData.activeName">
							<el-tab-pane name="template">
								<template #label>
									<el-badge is-dot :hidden="!isEdit">原始模板</el-badge>
								</template>
							</el-tab-pane>
							<el-tab-pane name="render" :disabled="isEmpty(testData.cascaderValue)" label="渲染结果"></el-tab-pane>
						</el-tabs>
					</el-header>

					<!-- 主体内容（独立滚动） -->
					<el-main class="main-scroll">
						<el-scrollbar v-show="testData.activeName === 'template'">
							<code-mirror v-model="testData.editTemplateContent" @keydown.ctrl.s.prevent="saveTemplateContent()"></code-mirror>
						</el-scrollbar>
						<el-scrollbar v-show="testData.activeName === 'render'">
							<code-mirror v-model="testData.renderedTemplateContent" read-only></code-mirror>
						</el-scrollbar>
					</el-main>
				</el-container>
			</el-splitter-panel>
		</el-splitter>
		<gen-template-form
			ref="formRef"
			:template-path="testData.fullFilePath"
			:template-type="testData.templateType"
			:parent-id="testData.parentId"
			:template-group-id="testData.templateGroupId"
		></gen-template-form>
	</el-drawer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, shallowReactive, shallowRef, watch } from 'vue'
import CodeMirror from '@/business/code-mirror/index.vue'
import { Action, ElLoading, ElMessage, ElMessageBox, ElTreeSelect } from 'element-plus'
import { Check, CopyDocument, Document, DocumentAdd, Download, Edit, Refresh } from '@element-plus/icons-vue'
import { copyToClipboard, isEmpty } from '@/utils/tool'
import TextTooltip from '@/components/text-tooltip/index.vue'
import { genGeneratorApi, genTemplateApi, genTemplateGroupApi } from '@/api'
import { Result, SubmitOptions } from '@/types'
import { useInitForm, useSubmitHandler } from '@/hooks'
import { useTimeoutFn } from '@vueuse/core'
import OptionLabel from '@/components/option/label/index.vue'
import GenTemplateForm from '@/views/gen/template/form.vue'

defineOptions({
	name: 'GenTemplateTest'
})

const emit = defineEmits(['refreshDataList'])

interface CascaderData {
	value: string
	label: string
	type: 'project' | 'table' | 'enum'
	id: number
	generatorType: number
	children?: CascaderData[]
}

const initTestData = () => ({
	visible: false,
	asideCollapsed: false,
	activeName: 'template',
	templateGroupId: 0,
	templateGroupType: 0,
	templateId: 0,
	templateType: -1,
	parentId: 0,
	templateName: '',
	originalFileName: '',
	originalTemplateContent: '',
	editTemplateContent: '',
	fullFilePath: '',
	renderedFileName: '',
	renderedTemplateContent: '',
	cascaderValue: [] as string[],
	cascaderData: [] as CascaderData[]
})

const testData = reactive(initTestData())
const editTemplateList = ref(new Set<number>())
const treeList = ref([] as any[])

const isEdit = computed(() => testData.editTemplateContent !== testData.originalTemplateContent)
const initGeneratorStateArray = () => [
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
const generatorState = shallowReactive(initGeneratorStateArray()[0])

const initCopyCodeStateArray = () => [
	{
		icon: CopyDocument,
		text: '复制'
	},
	{
		icon: Check,
		text: '已复制'
	}
]

const copyCodeState = shallowReactive(initCopyCodeStateArray()[0])

const { start: startCopyCode } = useTimeoutFn(() => {
	Object.assign(copyCodeState, initCopyCodeStateArray()[0])
}, 2000)

const copyIconState = shallowRef(CopyDocument)
const { start: startTimer } = useTimeoutFn(() => {
	copyIconState.value = CopyDocument
}, 2000)

const init = async (templateGroupId: number, templateGroupType: number, templateId: number, templateContent: string) => {
	Object.assign(testData, initTestData())
	testData.visible = true
	editTemplateList.value = new Set<number>()
	treeList.value = [] as any[]
	testData.templateGroupId = templateGroupId
	testData.templateGroupType = templateGroupType
	testData.templateId = templateId
	testData.editTemplateContent = templateContent

	// 获取模板详情数据
	await setTemplateData()

	// 获取树形数据
	await setTreeListData()

	// 获取级联数据
	await refreshCascaderData()
}

// 当选中的模板ID变化时，变更模板详情
watch(
	() => testData.templateId,
	async (_, oldValue) => {
		// console.log('templateId changed', value, oldValue)
		// 获取模板详情
		await setTemplateData()
		if (oldValue !== 0) {
			testData.editTemplateContent = testData.originalTemplateContent
		}
	}
)

// 监听级联选择器变化
watch(
	() => testData.cascaderValue,
	newValue => {
		if (isEmpty(newValue)) {
			testData.activeName = 'template'
		}
	}
)

const loading = ref(false)

const fullFilePath = computed(() => {
	return testData.activeName === 'template' ? testData.fullFilePath : testData.renderedFileName
})

// 保存模板内容
const saveTemplateContent = (callBack?: (() => void) | undefined) => {
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
			editTemplateList.value.add(testData.templateId)
			// console.log('保存成功', editTemplateList.value)
			testData.originalTemplateContent = testData.editTemplateContent
			callBack?.()
		})
		.finally(() => {
			loading.value = false
		})
}

// 处理选中值变化
const handleCascaderChange = async (val: string[]) => {
	if (isEmpty(val)) {
		return
	}
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
		queryForm.testId = Number(val[1].split('_')[1])
	} else if (testData.templateGroupType === 2) {
		queryForm.testId = Number(val[1].split('_')[1])
	}
	testData.activeName = 'render'
	const loadingInstance = ElLoading.service({
		text: '模板渲染中...'
	})
	try {
		const data = await genGeneratorApi.templateTest(queryForm, { noErrorMessage: true })
		testData.renderedFileName = data.filePath
		testData.renderedTemplateContent = data.content
		ElMessage.success({
			message: '渲染成功',
			duration: 1000
		})
		const projectId = Number(testData.cascaderValue[0].split('_')[1])
		const generatorType = testData.cascaderData.find(item => item.id === projectId)!.generatorType
		if (generatorType === 0) {
			Object.assign(generatorState, initGeneratorStateArray()[0])
		} else {
			Object.assign(generatorState, initGeneratorStateArray()[1])
		}
	} catch (error) {
		testData.activeName = 'template'
		// 检查错误是否包含预期的错误码
		const result: Result = error as any
		if (result.code === 700) {
			ElMessage.warning({
				message: '当前模板的表达式执行为false，不渲染模板',
				duration: 2000
			})
			return
		}
		if (result.code !== 200) {
			ElMessage.error({
				message: result.message,
				duration: 2000
			})
		}
	} finally {
		loadingInstance.close()
	}
}

const setTreeListData = async () => {
	treeList.value = await genTemplateApi.treeData(testData.templateGroupId)
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
	}
	testData.cascaderData = data

	// 获取模板详情
	await setTemplateData()
}

const setTemplateData = async () => {
	// 获取模板详情
	const detailQueryForm = {
		id: testData.templateId,
		setPath: true
	}
	const data = await genTemplateApi.detailData(detailQueryForm)
	testData.templateName = data.templateName
	testData.templateType = data.templateType
	testData.templateGroupType = data.templateGroupType
	testData.templateGroupId = data.templateGroupId
	testData.parentId = data.parentId
	testData.originalFileName = data.fileName
	testData.originalTemplateContent = data.templateContent
	testData.fullFilePath = data.generatorPath
}

const generatorHandler = () => {
	if (isEdit.value) {
		ElMessage.warning({
			message: `请先保存模板内容，再进行${generatorState.text}`,
			duration: 1000
		})
		return
	}
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
		submitState.submitApi = genGeneratorApi.downloadSingle
		submitState.successMessage = '代码已经下载到浏览器'
	} else if (generatorType === 1) {
		submitState.submitApi = genGeneratorApi.singleLocal
		submitState.successMessage = '代码已经生成到本地'
	}
	submitHandle(dataForm)
}

const submitState = shallowReactive({
	successDuration: 1000
} as SubmitOptions)

const { submitLoading, submitHandle } = useSubmitHandler(submitState)

const copyTemplateContent = () => {
	if (copyCodeState.icon === Check) {
		return
	}
	Object.assign(copyCodeState, initCopyCodeStateArray()[1])
	copyToClipboard(testData.renderedTemplateContent).then(() => {
		ElMessage.success('代码已复制到剪贴板')
		startCopyCode()
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

// 关闭抽屉方法。确认保存修改
const handleClose = (done: () => void) => {
	const editIdList = [...editTemplateList.value]
	// console.log('关闭抽屉方法。确认保存修改', editIdList, isEdit.value)
	if (!isEdit.value) {
		done()
		emit('refreshDataList', editIdList)
		return
	}
	const message = `${testData.originalFileName}已修改未保存，是否保存后再关闭？`
	ElMessageBox.confirm(message, '提示', {
		distinguishCancelAndClose: true,
		confirmButtonText: '是(Y)',
		cancelButtonText: '否(N)',
		type: 'warning'
	})
		.then(() => {
			// 保存修改后关闭
			saveTemplateContent(() => {
				done()
				emit('refreshDataList', editIdList)
			})
		})
		.catch((action: Action) => {
			// 丢弃修改直接关闭
			if (action === 'cancel') {
				done()
				emit('refreshDataList', editIdList)
			}
		})
}

const { formRef, formInitHandle } = useInitForm()

defineExpose({
	init
})
</script>

<style>
.template-test-drawer .el-drawer__header {
	display: flex;
	align-items: center;
}

.template-test-drawer .el-drawer__title {
	flex: none;
	white-space: nowrap;
	margin-right: 12px;
}

.template-test-drawer .el-drawer__close-btn {
	margin-left: auto;
}
</style>
<style scoped>
.right-container {
	display: flex;
	flex-direction: column;
	height: 100%;
}

.header-fixed {
	height: auto;
	flex-shrink: 0;
}
.path-container {
	display: inline-flex; /* 改为 inline-flex 保持内容紧密排列 */
	align-items: center;
	max-width: 100%;
}

.main-scroll {
	flex: 1;
	overflow: auto;
	padding: 0 16px 16px;
}
:deep(.el-badge__content.is-fixed.is-dot) {
	right: 0;
}
:deep(.el-badge__content.is-dot) {
	height: 6px;
	width: 6px;
}
</style>
