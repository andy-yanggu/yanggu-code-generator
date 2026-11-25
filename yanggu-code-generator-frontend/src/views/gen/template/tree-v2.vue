<template>
	<!-- 预览界面：抽屉外壳与生命周期归容器，左树 / 右编辑区下沉到面板，共享状态由 treeState 编排 -->
	<el-drawer
		v-model="visible"
		:title="`模板配置（${templateGroupName}）`"
		size="100%"
		:modal="false"
		body-class="template-tree-drawer-body"
		:before-close="() => handleClose(() => (visible = false))"
		destroy-on-close
	>
		<el-splitter class="tree-splitter" style="height: 100%">
			<!-- 左侧：树结构 -->
			<el-splitter-panel collapsible size="30%" style="overflow: hidden; margin-right: 5px">
				<tree-panel ref="treePanelRef" :tree-state="treeState" :template-group-id="templateGroupId" @refresh="refreshTree"></tree-panel>
			</el-splitter-panel>
			<!-- 右侧：模板编辑 + 变量目录 -->
			<el-splitter-panel>
				<div class="editor-area" :class="{ 'full-screen-mode': treeFullScreen }">
					<!-- 两块 panel 的尺寸一律按实测容器宽度算出 px 后断言，编辑区也带 min，
					     避免 EP 归一化分支在容器宽度变化时触发等比缩放（口径见 use-variable-panel-size） -->
					<el-splitter ref="editorSplitterRef" class="editor-splitter" style="height: 100%">
						<el-splitter-panel :min="editorFloor" :size="editorPanelSize">
							<editor-panel
								ref="editorPanelRef"
								:tree-state="treeState"
								:template-group-id="templateGroupId"
								:template-group-type="templateGroupType"
								:full-screen="treeFullScreen"
								:variable-panel-visible="variablePanelVisible"
								@toggle-full-screen="handleToggleFullScreen"
								@toggle-variable-panel="toggleVariablePanel"
							></editor-panel>
						</el-splitter-panel>
						<el-splitter-panel v-if="variablePanelVisible" :size="clampedSize" :min="240" :max="560" @update:size="panelSize = $event">
							<variable-panel
								:catalog="variableCatalog"
								:loading="variableLoading"
								:error="variableError"
								:disabled="!canInsertVariable"
								:insert="insertVariable"
								@reload="reloadVariableCatalog"
							></variable-panel>
						</el-splitter-panel>
					</el-splitter>
					<!-- 收起态常驻编辑区右缘的 28px 竖柄，与头部「变量」按钮控制同一个状态 -->
					<variable-edge v-if="!variablePanelVisible" :count="variableTotal" @toggle="toggleVariablePanel"></variable-edge>
				</div>
			</el-splitter-panel>
		</el-splitter>
	</el-drawer>
</template>

<script setup lang="ts">
import { LOADING_MASK_OPTIONS } from '@/components/loading-icon/loading-mask'
import TreePanel from '@/views/gen/template/tree-panel.vue'
import EditorPanel from '@/views/gen/template/editor-panel.vue'
import VariablePanel from '@/views/gen/template/variable-panel/index.vue'
import VariableEdge from '@/views/gen/template/variable-panel/variable-edge.vue'
import { useTemplateTreeState } from './use-template-tree-state'
import { useVariableCatalog } from './variable-panel/use-variable-catalog'
import { useVariablePanelSize } from './variable-panel/use-variable-panel-size'
import { countVariables } from './variable-utils'

defineOptions({
	name: 'GenTemplateTreeV2'
})

const props = defineProps({
	templateGroupId: {
		type: Number,
		required: true
	},
	templateGroupName: {
		type: String,
		required: true
	},
	templateGroupType: {
		type: Number,
		required: true
	}
})

// 核心状态：树/tab/激活项与所有状态动作都在此实例内，两个面板共享同一份对象引用
const treeState = useTemplateTreeState(toRef(props, 'templateGroupId'))
const { state, tabPush, refreshTabData, loadTreeData, reset, canInsertVariable, handleClose } = treeState

// 抽屉显隐归容器生命周期所有
const visible = ref(false)

const treePanelRef = ref()
const editorPanelRef = ref()

// 全屏：isFullscreen 反映文档级原生全屏（navbar 按钮共用 documentElement），treeFullScreen 只是 CSS 铺满态
const { isFullscreen } = useFullscreen()
const treeFullScreen = ref(false)

// 原生全屏结束（含 ESC、navbar 退出）时撤销铺满态，避免本地状态悬挂
watch(isFullscreen, value => {
	if (!value) {
		treeFullScreen.value = false
	}
})

// 只翻 treeFullScreen 走 CSS 铺满，没走原生全屏，所以 ESC 退不掉（口径与 v1 一致）
const handleToggleFullScreen = () => {
	treeFullScreen.value = !treeFullScreen.value
}

// 变量面板默认收起：展开才挂载；目录状态由宿主持有，收起态徽标仍要有总数，首轮加载押到抽屉真正打开
const variablePanelVisible = ref(false)
const {
	catalog: variableCatalog,
	loading: variableLoading,
	error: variableError,
	loaded: variableCatalogLoaded,
	ensureLoaded: ensureVariableCatalog,
	reload: reloadVariableCatalog
} = useVariableCatalog(toRef(props, 'templateGroupId'), { immediate: false })

// null 表示该组目录尚未就绪，此时竖柄不渲染徽标；countVariables 跳出面板错误边界，畸形目录失败就撤下徽标做局部降级
const variableTotal = computed(() => {
	if (!variableCatalogLoaded.value) {
		return null
	}
	try {
		return countVariables(variableCatalog.value)
	} catch {
		return null
	}
})

// 抽屉打开才加载：init() 会把 visible 置 true，这里覆盖所有打开入口；缓存命中不会重复请求
watch(
	() => visible.value,
	visible => {
		if (visible) {
			ensureVariableCatalog()
		}
	}
)

const toggleVariablePanel = () => {
	variablePanelVisible.value = !variablePanelVisible.value
}

// 宽度 px 体系：实测 editorSplitter 容器宽度派生编辑区下限/面板夹紧值/编辑区铺满值
const editorSplitterRef = ref()
const { editorFloor, clampedSize, editorPanelSize, panelSize, resetInit } = useVariablePanelSize(editorSplitterRef, variablePanelVisible)

// 插入入口：把文本交给 editor-panel 转发的 code-mirror.insertAtCursor；编辑器未就绪返回 false 由面板提示
const insertVariable = (text: string, caret: number): boolean => {
	return editorPanelRef.value?.insertAtCursor(text, caret) ?? false
}

// 初始化方法：复位上次打开遗留状态，保证每次打开都是干净开局（DOM 相关复位下沉到面板）
const init = async () => {
	// destroy-on-close 只销毁抽屉内容 DOM、组件实例常驻，需手动复位
	reset()
	treePanelRef.value?.resetSearch()
	treeFullScreen.value = false
	variablePanelVisible.value = false
	resetInit()

	const loadingInstance = ElLoading.service({ target: '.tree-splitter', text: '数据加载中...', ...LOADING_MASK_OPTIONS })
	try {
		const templateContentList = await loadTreeData()
		visible.value = true
		await nextTick()

		// 恢复展开状态（fresh open 时 expandedKeys 已被 reset 清空，此处为忠实搬运）
		treePanelRef.value?.restoreExpanded(state.expandedKeys)

		if (templateContentList.length > 0 && state.activeItemId === -1) {
			state.activeItemId = templateContentList[0].id
			state.expandedKeys.push(templateContentList[0].parentId)
			tabPush(templateContentList[0])
		}
		// 更新一下tab中的数据，已新加载数据为准
		refreshTabData(templateContentList)
	} finally {
		// 延迟 500ms 再收蒙层
		useTimeoutFn(() => loadingInstance.close(), 500)
	}
}

// 全量刷新树数据，回到初始状态
const refreshTree = () => {
	ElMessageBox.confirm('刷新将丢失所有未保存的编辑内容及界面状态（包括节点展开状态、搜索条件等），确定要刷新吗？', '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			init()
		})
		.catch(() => {
			ElMessage.info('刷新已取消')
		})
}

defineExpose({
	init
})
</script>
<!-- 抽屉 body padding 写成非 scoped 的全局块：类名落在 el-drawer 内部 DOM 上，scoped 选择器追不到；本组件自带，不依赖其他组件的样式注入 -->
<style>
.template-tree-drawer-body {
	padding: 10px 20px 20px;
}
</style>
<style scoped>
.editor-area {
	position: relative;
	display: flex;
	height: 100%;
	/* 容器被外层分栏挤到极窄时不让分栏被压扁，而是横向滚动（配合下面 splitter 的 min-width） */
	overflow-x: auto;
}

.editor-splitter {
	flex: 1;
	/* 不能是 0：一旦实测宽度 < 面板下界 240，两块之和无法同时满足「面板 ≥ 240」与「两块之和 = 容器」，
	   EP 会退回等比归一化把面板压到 240 以下；给分栏一个 240 硬底，面板就能守住规格下界 */
	min-width: 240px;
}

.full-screen-mode {
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	z-index: 3000;
	background: var(--el-bg-color-page);
	margin: 0 !important;
	padding: 20px;
}
</style>
