<template>
	<el-container v-if="state.tabList.length > 0" style="height: 100%">
		<!-- 头部操作区域 -->
		<el-header style="display: flex; flex-direction: column; padding: 0 5px 5px 5px; margin-bottom: 10px">
			<el-row style="margin-bottom: 5px">
				<el-col :span="17" style="padding-left: 10px">
					<text-tooltip :title="`路径：${fullFilePath}`" :tooltip-content="fullFilePath" max-width="100%"></text-tooltip>
				</el-col>
				<el-col :span="7" style="text-align: right">
					<el-tooltip content="用当前编辑内容试渲染" placement="bottom">
						<el-button v-if="![0, 2].includes(activeTabItem.templateType)" size="small" type="warning" :icon="DocumentChecked" @click="testTemplateContent"> 测试 </el-button>
					</el-tooltip>
					<!-- 禁用态也照弹：实测 disabled 只抑制 click，mouseenter/mouseover 与 :hover 仍命中按钮本身，所以不必外层再包一个 span 去承载 tooltip -->
					<el-tooltip :content="activeTabItem.isEdited ? '保存当前文件（Ctrl+S）' : '当前文件无改动'" placement="bottom">
						<el-button :disabled="!activeTabItem.isEdited" size="small" type="primary" :icon="Edit" :loading="submitLoading" @click="saveTemplateContent"> 保存 </el-button>
					</el-tooltip>
					<!-- 文案说清行为边界：只翻 fullScreen 走 CSS 铺满，没走原生全屏，所以 ESC 退不掉 -->
					<el-tooltip content="编辑区铺满窗口（ESC 无效）" placement="bottom">
						<el-button size="small" @click="emit('toggle-full-screen')">
							<svg-icon :icon="fullScreen ? 'icon-fullscreen-exit' : 'icon-fullscreen'" style="margin-right: 5px"></svg-icon>
							{{ fullScreen ? '退出' : '全屏' }}
						</el-button>
					</el-tooltip>
					<el-tooltip content="Freemarker 变量目录" placement="bottom">
						<el-button size="small" :type="variablePanelVisible ? 'primary' : 'default'" plain @click="emit('toggle-variable-panel')">
							<svg-icon icon="icon-variable" style="margin-right: 5px"></svg-icon>
							变量
						</el-button>
					</el-tooltip>
				</el-col>
			</el-row>
			<el-divider style="margin: 5px 0"></el-divider>
			<el-tabs
				ref="tabsRef"
				v-model="state.activeItemId"
				style="--el-tabs-header-height: 35px"
				tab-position="top"
				type="card"
				@tab-click="handleTabClick"
				@tab-remove="handleTabRemove"
			>
				<el-tab-pane v-for="tabItem in state.tabList" :key="tabItem.id" :name="tabItem.id" closable>
					<template #label>
						<el-tooltip :content="tabItem.templateDesc" :disabled="!tabItem.templateDesc" placement="top">
							<div class="tab-label">
								<!-- 修改时添加小红点 -->
								<el-badge is-dot :hidden="!tabItem.isEdited">
									<div style="display: flex; align-items: center; gap: 5px">
										<svg-icon :icon="getIcon({ expanded: false }, tabItem)"></svg-icon>
										{{ tabItem.fileName }}
									</div>
								</el-badge>
							</div>
						</el-tooltip>
					</template>
				</el-tab-pane>
			</el-tabs>
			<!-- tab右键菜单：通用 ContextMenu，target 携带被右键的 tab 项与索引 -->
			<context-menu ref="tabMenuRef" :items="tabMenuItems"></context-menu>
			<!-- 测试页面 -->
			<template-test ref="templateTestRef" @refresh-data-list="refreshActiveItemContent"></template-test>
		</el-header>

		<!-- 代码区域 -->
		<el-main style="padding: 5px; overflow: hidden">
			<template v-if="activeTabItem.templateType === 1">
				<el-scrollbar ref="codeScrollbarRef" style="height: 100%">
					<code-mirror ref="codeMirrorRef" v-model="activeTabItem.templateContent!" @keydown.ctrl.s.prevent="saveTemplateContent()"></code-mirror>
				</el-scrollbar>
			</template>
			<template v-else-if="activeTabItem.templateType === 2">
				<div style="display: flex; align-items: center; justify-content: center; height: 100%">
					<template v-if="IMAGE_TYPE_LIST.some(tempType => activeTabItem.binaryOriginalFileName?.endsWith(tempType))">
						<el-image :src="activeTabItem.templateContent" fit="fill"></el-image>
					</template>
					<template v-else>
						<el-text size="large" tag="b">文件暂不支持预览（目前只支持图片）</el-text>
					</template>
				</div>
			</template>
		</el-main>
	</el-container>
	<el-main v-else style="display: flex; flex-direction: column; height: 100%">
		<div style="flex: 1; display: flex; align-items: center; justify-content: center">
			<el-text size="large" tag="b">请点击左侧的模板进行查看和修改</el-text>
		</div>
	</el-main>
</template>

<script setup lang="ts">
import type { TabsPaneContext } from 'element-plus'
import CodeMirror from '@/business/code-mirror/index.vue'
import TemplateTest from '@/views/gen/template/test.vue'
import TextTooltip from '@/components/text-tooltip/index.vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import ContextMenu from '@/components/context-menu/index.vue'
import type { ContextMenuItem } from '@/components/context-menu/types'
import { Back, CloseBold, DocumentChecked, Edit, Remove, Right } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { IMAGE_TYPE_LIST, type Tree } from './tree-utils'
import type { TemplateTreeState } from './use-template-tree-state'

defineOptions({
	name: 'GenTemplateEditorPanel'
})

const props = defineProps<{
	treeState: TemplateTreeState
	templateGroupId: number
	templateGroupType: number
	fullScreen: boolean
	variablePanelVisible: boolean
}>()

const emit = defineEmits<{
	(e: 'toggle-full-screen'): void
	(e: 'toggle-variable-panel'): void
}>()

const {
	state,
	submitLoading,
	activeTabItem,
	fullFilePath,
	getIcon,
	getElementFromTabList,
	saveTemplateContent,
	refreshActiveItemContent,
	handleTabRemove,
	closeCurrentTab,
	closeOtherTabs,
	closeLeftTabs,
	closeRightTabs,
	closeUneditedTabs,
	closeAllTabs,
	rollbackNode
} = props.treeState

const tabsRef = ref()
const templateTestRef = ref()
const codeScrollbarRef = ref()
const tabMenuRef = ref<InstanceType<typeof ContextMenu> | null>(null)
// 带组件实例类型，让 vue-tsc 能校验 code-mirror 新暴露的 insertAtCursor 契约
const codeMirrorRef = ref<InstanceType<typeof CodeMirror> | null>(null)

// 代码编辑区滚动到顶部（树节点滚动在 tree-panel 内各自处理）
watch(
	() => state.activeItemId,
	() => {
		nextTick(() => {
			codeScrollbarRef.value?.scrollTo({
				top: 0,
				behavior: 'smooth'
			})
		})
	}
)

// 初始化tab拖拽效果
watch(
	() => state.tabList.length > 0,
	() => {
		nextTick(() => {
			initTabSortable()
		})
	}
)

// 实现tab的拖拽排序
const initTabSortable = () => {
	if (!tabsRef.value) {
		return
	}

	const tabsWrapper = tabsRef.value.$el.querySelector('.el-tabs__nav') as HTMLElement
	if (!tabsWrapper) {
		return
	}

	new Sortable(tabsWrapper, {
		animation: 200,
		handle: '.tab-label', // 拖拽手柄
		filter: '.el-icon', // 排除关闭按钮
		preventOnFilter: true,
		onEnd: evt => {
			// el-tab会有一个高亮的样式需要排除掉
			const oldIndex = evt.oldIndex! - 1
			const newIndex = evt.newIndex! - 1
			// 从原数组中取出移动的 tab
			const movedTab = state.tabList.splice(oldIndex, 1)[0]
			// 插入到新位置
			state.tabList.splice(newIndex, 0, movedTab)
			// 设置当前激活的tab
			if (movedTab.id != state.activeItemId) {
				state.activeItemId = movedTab.id
			}
		}
	})
}

// tab点击
const handleTabClick = (tab: TabsPaneContext, _: Event) => {
	state.activeItemId = tab.paneName as number
}

// tab右键菜单 target：被右键的 tab 项与其索引
type TabMenuTarget = { item: Tree; index: number }

const tabMenuItems = computed<ContextMenuItem<TabMenuTarget>[]>(() => [
	// 回滚置顶：只丢内容不关 tab（区别于关闭确认的「否(N)」退出链路），仅脏 tab 显示
	{ key: 'rollback', label: '回滚当前', icon: 'icon-rollback', visible: t => !!t.item.isEdited, onClick: t => rollbackNode(t.item) },
	{ key: 'closeCurrent', label: '关闭当前', icon: CloseBold, onClick: t => closeCurrentTab(t.item, t.index) },
	{ key: 'closeOthers', label: '关闭其他', icon: 'icon-close-others', visible: () => state.tabList.length > 1, onClick: t => closeOtherTabs(t.item) },
	{ key: 'closeLeft', label: '关闭左侧', icon: Back, visible: t => t.index > 0, onClick: t => closeLeftTabs(t.item, t.index) },
	{
		key: 'closeRight',
		label: '关闭右侧',
		icon: Right,
		visible: t => t.index < state.tabList.length - 1,
		onClick: t => closeRightTabs(t.item, t.index)
	},
	{
		key: 'closeUnedited',
		label: '关闭未改',
		icon: Remove,
		visible: () => state.tabList.some(item => !item.isEdited),
		onClick: t => closeUneditedTabs(t.index)
	},
	{ key: 'closeAll', label: '关闭全部', icon: 'icon-close-all', visible: () => state.tabList.length > 1, onClick: () => closeAllTabs() }
])

// 标签页元素
const tabNavEl = computed(() => tabsRef.value?.tabNavRef?.tabListRef)

// tab右键菜单
const onTabContextMenu = (e: MouseEvent) => {
	const item = (e.target as HTMLElement).closest('.el-tabs__item') as HTMLElement
	if (!item) {
		return
	}

	e.preventDefault()

	const tempId = Number.parseInt(item.id.replace(/^tab-/, ''))
	const index = state.tabList.findIndex(t => t.id === tempId)
	if (index === -1) {
		return
	}

	tabMenuRef.value?.open(e, { item: getElementFromTabList(tempId)!, index })
}

useEventListener(tabNavEl, 'contextmenu', onTabContextMenu)

// 测试模板内容
const testTemplateContent = () => {
	nextTick(() => {
		templateTestRef.value.init(props.templateGroupId, props.templateGroupType, activeTabItem.value.id, activeTabItem.value.templateContent)
	})
}

// 供容器转发给变量面板：把文本写进编辑器光标处，编辑器未就绪返回 false
const insertAtCursor = (text: string, caret: number): boolean => {
	return codeMirrorRef.value?.insertAtCursor(text, caret) ?? false
}

defineExpose({
	insertAtCursor
})
</script>

<style scoped>
.tab-label {
	position: relative;
	display: inline-block;
}

/* 两种容器：tab 和 tree node 都可以挂红点 */
:deep(.el-badge__content.is-fixed.is-dot) {
	right: 0;
}
:deep(.el-badge__content.is-dot) {
	height: 6px;
	width: 6px;
}

/* el-header 是 flex-shrink: 0 的固定 60px 盒：展开面板后按钮换成两行，内容会直接溢出盒外压住编辑器首行，改成随内容自适应（评审 I1） */
:deep(.el-header) {
	height: auto;
}
</style>
