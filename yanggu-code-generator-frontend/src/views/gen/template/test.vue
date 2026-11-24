<template>
	<el-drawer v-model="testData.visible" size="100%" class="template-test-drawer" destroy-on-close :before-close="handleClose">
		<template #header>
			<el-link
				type="primary"
				class="el-drawer__title"
				style="font-size: 16px"
				underline="never"
				@click="formInitHandle({ type: 'detail', id: testData.templateId })">
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
				placeholder="请选择模板">
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
							@click="handleCascaderChange(testData.cascaderValue)">
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
						@change="handleCascaderChange">
						<template #default="{ data }">
							<option-label :label="data.label" :desc="data.description"></option-label>
						</template>
					</el-cascader>
				</el-row>
			</el-splitter-panel>

			<!-- 右侧：内容区（内层再分「编辑容器 + 变量面板」，宽度数值体系对齐 tree.vue） -->
			<el-splitter-panel>
				<div class="editor-area">
					<el-splitter ref="editorSplitterRef" class="editor-splitter" style="height: 100%">
						<el-splitter-panel :min="editorFloor" :size="editorPanelSize">
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
											<el-button size="small" :type="variablePanelVisible ? 'primary' : 'default'" plain @click="toggleVariablePanel">
												<svg-icon icon="icon-variable" style="margin-right: 5px"></svg-icon>
												变量
											</el-button>
											<template v-if="testData.activeName === 'template'">
												<el-button
													:icon="Document"
													size="small"
													:disabled="isEmpty(testData.cascaderValue)"
													@click="handleCascaderChange(testData.cascaderValue)">
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
														@click="copyTemplateContent()">
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
														@click="generatorHandler()">
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
										<code-mirror ref="codeMirrorRef" v-model="testData.editTemplateContent" @keydown.ctrl.s.prevent="saveTemplateContent()"></code-mirror>
									</el-scrollbar>
									<el-scrollbar v-show="testData.activeName === 'render'">
										<code-mirror v-model="testData.renderedTemplateContent" read-only></code-mirror>
									</el-scrollbar>
								</el-main>
							</el-container>
						</el-splitter-panel>
						<el-splitter-panel v-if="variablePanelVisible" :size="clampedSize" :min="240" :max="560" @update:size="panelSize = $event">
							<variable-panel
								:catalog="variableCatalog"
								:loading="variableLoading"
								:error="variableError"
								:disabled="!canInsertVariable"
								value-mode
								:insert="insertVariable"
								@reload="reloadVariableCatalog"
							></variable-panel>
						</el-splitter-panel>
					</el-splitter>
					<!-- 收起态常驻右缘的 28px 竖柄，与头部「变量」按钮控制同一个状态（共享件自带 tooltip 与「目录未就绪不渲染徽标」降级） -->
					<variable-edge v-if="!variablePanelVisible" :count="variableTotal" @toggle="toggleVariablePanel"></variable-edge>
				</div>
			</el-splitter-panel>
		</el-splitter>
		<gen-template-form
			ref="formRef"
			:template-path="testData.fullFilePath"
			:template-type="testData.templateType"
			:parent-id="testData.parentId"
			:template-group-id="testData.templateGroupId"></gen-template-form>
	</el-drawer>
</template>

<script setup lang="ts">
import CodeMirror from '@/business/code-mirror/index.vue'
import { Action, CascaderValue } from 'element-plus'
import { Check, CopyDocument, Document, DocumentAdd, Download, Edit, Refresh } from '@element-plus/icons-vue'
import { copyToClipboard, isEmpty } from '@/utils/tool'
import TextTooltip from '@/components/text-tooltip/index.vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import VariablePanel from '@/views/gen/template/variable-panel/index.vue'
import VariableEdge from '@/views/gen/template/variable-panel/variable-edge.vue'
import { genGeneratorApi, genTemplateApi, genTemplateGroupApi } from '@/api'
import { GeneratorQuery, Result, SubmitOptions } from '@/types'
import { useInitForm, useSubmitHandler } from '@/hooks'
import OptionLabel from '@/components/option/label/index.vue'
import GenTemplateForm from '@/views/gen/template/form.vue'
import { LOADING_MASK_OPTIONS } from '@/components/loading-icon/loading-mask'
import { useVariableValueCatalog } from './variable-panel/use-variable-value-catalog'
import { useVariablePanelSize } from './variable-panel/use-variable-panel-size'
import { countVariables } from './variable-utils'

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

// 带值变量目录：数据模型只由「选中的测试对象」决定，与模板内容无关，故单独持有一对引用
const valueProjectId = ref(0)
const valueTestId = ref(0)
const {
	catalog: variableCatalog,
	loading: variableLoading,
	error: variableError,
	loaded: variableCatalogLoaded,
	load: loadVariableCatalog
} = useVariableValueCatalog(
	toRef(testData, 'templateGroupId'),
	toRef(testData, 'templateGroupType'),
	valueProjectId,
	valueTestId
)

// 面板默认收起；总项数上到宿主 computed 后跳过了面板的 onErrorCaptured 边界，
// 目录数据畸形时会在宿主 render 阶段抛错把整个抽屉带上，故失败时直接撤下徽标（与 tree.vue 同款策略）
const variablePanelVisible = ref(false)
const variableTotal = computed(() => {
	if (!variableCatalogLoaded.value) {
		return null
	}
	try {
		return countVariables(variableCatalog.value, true)
	} catch {
		return null
	}
})

const toggleVariablePanel = () => {
	variablePanelVisible.value = !variablePanelVisible.value
}

// 面板宽度数值体系走共享件 use-variable-panel-size（默认 320px、区间 [240, 560]、编辑区下限 320 且窄容器退让、首次展开取容器 28%），
// 与 tree-v2.vue 同源：实测容器同样取编辑区的内层 el-splitter
const editorSplitterRef = ref()
const { editorFloor, clampedSize, editorPanelSize, panelSize, resetInit } = useVariablePanelSize(editorSplitterRef, variablePanelVisible)

// 只有「原始模板」 tab 有可写编辑器，渲染结果 tab 是 read-only
const canInsertVariable = computed(() => testData.activeName === 'template')

const codeMirrorRef = ref<InstanceType<typeof CodeMirror> | null>(null)

// 插入入口：把文本交给 code-mirror 暴露的 insertAtCursor；返回 false 让面板提示「编辑器未就绪」
const insertVariable = (text: string, caret: number): boolean => {
	return codeMirrorRef.value?.insertAtCursor(text, caret) ?? false
}

// 面板重试：值目录不做缓存，直接重查当前对象
const reloadVariableCatalog = () => {
	return loadVariableCatalog()
}

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

// 带值目录入参：数据模型只取决于选中的测试对象，从级联值里拆出 projectId / testId。
// 项目组（type 0）只靠 projectId，后端不读 testId，故保持 0；表/枚举组取二级节点 id
const syncValueCatalogParams = (val: string[]) => {
	if (isEmpty(val)) {
		valueProjectId.value = 0
		valueTestId.value = 0
		return
	}
	valueProjectId.value = Number(val[0].split('_')[1])
	valueTestId.value = testData.templateGroupType === 0 ? 0 : Number(val[1]?.split('_')[1] ?? 0)
}

const init = async (templateGroupId: number, templateGroupType: number, templateId: number, templateContent: string) => {
	Object.assign(testData, initTestData())
	testData.visible = true
	editTemplateList.value = new Set<number>()
	treeList.value = [] as any[]
	testData.templateGroupId = templateGroupId
	testData.templateGroupType = templateGroupType
	testData.templateId = templateId
	testData.editTemplateContent = templateContent

	// 面板与值目录都是上次打开的遗留状态，抽屉 destroy-on-close 只销毁 DOM、组件实例常驻，需手动复位
	variablePanelVisible.value = false
	resetInit()
	valueProjectId.value = 0
	valueTestId.value = 0
	loadVariableCatalog()

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
// el-cascader 的 change 契约是 (value: CascaderValue | null | undefined) => any，
// CascaderValue 是「单值 / 路径数组 / 多选数组」的联合。本组件是单选 + emitPath，
// 运行时拿到的必然是 string[]，但形参必须按契约放宽，否则 strictFunctionTypes
// 的逆变检查会报 TS2322；这里在函数体内再收窄回 string[]。
const handleCascaderChange = async (value: CascaderValue | null | undefined) => {
	const val = (value ?? []) as string[]
	if (isEmpty(val)) {
		// 级联清空：值目录入参与已缓存的目录一并清掉，避免上一个对象的值停留在面板里
		syncValueCatalogParams(val)
		loadVariableCatalog()
		return
	}
	// 值目录与渲染并发：请求在渲染前发起，但不 await——目录加载失败不能拖死渲染（规格 6.2「辅助不阻断主体」），
	// 渲染报条件表达式 false（code 700）时面板仍应可读，那正是定位表达式为什么 false 的现场
	syncValueCatalogParams(val)
	loadVariableCatalog()
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
		text: '模板渲染中...',
		...LOADING_MASK_OPTIONS
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
	const dataForm: GeneratorQuery = {
		productType: testData.templateGroupType,
		productIds: [id],
		templateIds: [testData.templateId]
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
/* 右侧内容区变成「内层分栏 + 收起态竖柄」的容器，竖柄要贴右缘故取定位上下文 */
.editor-area {
	position: relative;
	display: flex;
	height: 100%;
	/* 容器被外层分栏挤到极窄时不让分栏被压扁，而是横向滚动（配合下面 splitter 的 min-width） */
	overflow-x: auto;
}

.editor-splitter {
	flex: 1;
	/* 不能是 0：一旦实测宽度 < 面板下界 240，两块之和就无法同时满足「面板 ≥ 240」与「两块之和 = 容器」，
	   EP 会退回等比归一化把面板压到 240 以下；给分栏一个 240 硬底，面板就能守住下界 */
	min-width: 240px;
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
