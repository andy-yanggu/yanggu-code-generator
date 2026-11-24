<template>
	<div class="tree-panel">
		<el-affix :offset="20">
			<el-row style="margin-bottom: 10px" :gutter="10">
				<el-col :span="14">
					<el-input v-model="treeSearchText" placeholder="请输入目录/文件名称" size="small" clearable prefix-icon="Search" style="width: 100%; margin-left: 10px"></el-input>
				</el-col>
				<el-col :span="10" style="display: flex; justify-content: center; align-items: center">
					<el-button size="small" type="primary" :icon="Refresh" @click="emit('refresh')">刷新</el-button>
					<el-button size="small" type="danger" :icon="Delete" :disabled="checkedNodeListLength === 0" @click="deleteCheckedNode()"> 删除 </el-button>
				</el-col>
			</el-row>
		</el-affix>
		<el-divider style="margin: 5px 0"></el-divider>
		<el-scrollbar style="height: calc(100% - 40px); overflow-x: auto" @contextmenu.prevent.stop="handleScrollWrapperRightClick">
			<div class="tree-scroll-wrapper">
				<el-tree
					ref="treeRef"
					:data="state.treeList"
					node-key="id"
					:current-node-key="state.activeItemId"
					highlight-current
					show-checkbox
					:check-on-click-leaf="false"
					class="custom-tree"
					:props="{ label: 'fileName' }"
					:filter-node-method="filterNode"
					draggable
					:allow-drag="treeAllowDrag"
					:allow-drop="treeAllowDrop"
					:default-expanded-keys="state.expandedKeys"
					@node-click="handleTreeNodeClick"
					@node-contextmenu="handleNodeRightClick"
					@node-drop="handleNodeDrop"
					@node-expand="handleNodeExpand"
					@node-collapse="handleNodeCollapse"
				>
					<template #default="{ node, data }">
						<el-tooltip :content="data.templateDesc" placement="top" :disabled="!data.templateDesc">
							<div class="custom-tree-node">
								<!-- 修改时添加小红点 -->
								<el-badge is-dot :hidden="!data.isEdited">
									<div style="display: flex; align-items: center; justify-content: center; gap: 5px">
										<svg-icon :icon="getIcon(node, data)"></svg-icon>
										{{ node.label }}
									</div>
								</el-badge>
								<div class="tree-node-actions">
									<el-icon class="edit-icon" @click.stop="updateTemplate(data)">
										<Edit></Edit>
									</el-icon>
								</div>
							</div>
						</el-tooltip>
					</template>
					<template #empty>
						<el-empty>
							<template #description>
								<div style="display: flex; flex-direction: column; align-items: center; justify-content: center">
									<el-row>
										<el-text size="large" tag="b">暂无数据</el-text>
									</el-row>
									<el-row>
										<el-text size="large" tag="b">请在空白处点击右键新建目录、模板文件或者二进制文件</el-text>
									</el-row>
								</div>
							</template>
						</el-empty>
					</template>
				</el-tree>
			</div>
		</el-scrollbar>
		<!-- 自定义模板右键菜单：定位/显隐/关闭生命周期交给通用 ContextMenu，本组件只声明 items -->
		<context-menu ref="treeMenuRef" :items="treeMenuItems"></context-menu>
		<!-- 新增和修改页面 -->
		<template-form
			ref="formRef"
			:template-group-id="templateGroupId"
			:parent-id="formContext.parentId"
			:template-type="formContext.templateType"
			:template-path="formContext.templatePath"
			@refresh-data-list="initAfterHandler"
		></template-form>
	</div>
</template>

<script setup lang="ts">
import type { FilterNodeMethodFunction } from 'element-plus'
import { Delete, Edit, Refresh } from '@element-plus/icons-vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import ContextMenu from '@/components/context-menu/index.vue'
import TemplateForm from '@/views/gen/template/form.vue'
import type { ContextMenuItem } from '@/components/context-menu/types'
import { useInitForm } from '@/hooks/use-init-form'
import { genTemplateApi } from '@/api'
import { getFullPathById, type Tree } from './tree-utils'
import type { TemplateTreeState } from './use-template-tree-state'

defineOptions({
	name: 'GenTemplateTreePanel'
})

const props = defineProps<{
	treeState: TemplateTreeState
	templateGroupId: number
}>()

const emit = defineEmits<{
	(e: 'refresh'): void
}>()

// 解构共享同一批对象引用（红点/编辑态双向同步依赖此同一性），不得 clone
const { state, getIcon, tabPush, initData, handleTabRemove } = props.treeState

const treeRef = ref()
const treeMenuRef = ref<InstanceType<typeof ContextMenu> | null>(null)
const treeSearchText = ref('')

// 表单入口上下文：新增/修改的 parentId/templateType/templatePath 由右键入口决定（不信详情回传）
const formContext = reactive({
	parentId: 0,
	templateType: -1,
	templatePath: ''
})

const { formRef, formInitHandle } = useInitForm()

const checkedNodeListLength = computed(() => treeRef.value?.getCheckedKeys().length)

// 监听树节点变化，激活项变化时把节点滚动到可视区域（编辑器滚动在 editor-panel 内各自处理）
watch(
	() => state.activeItemId,
	id => {
		nextTick(() => {
			// 抽屉 destroy-on-close 后本 watch 仍可能触发一次，此时 treeRef 已卸载为 null，用可选链避免 TypeError
			const dom = treeRef.value?.$el?.querySelector(`[data-key="${id}"]`)
			if (dom) {
				dom.scrollIntoView({
					behavior: 'smooth',
					inline: 'center',
					block: 'nearest'
				})
			}
		})
	}
)

watch(treeSearchText, val => {
	treeRef.value!.filter(val)
})

// 过滤树节点（filter-node-method 契约签名，体内把 data 断言回 Tree）
const filterNode: FilterNodeMethodFunction = (value, data) => {
	if (!value) {
		return true
	}
	const node = data as Tree
	return node.fileName.includes(String(value))
}

// tree点击
const handleTreeNodeClick = (data: Tree) => {
	// 只有是文件才可以预览
	if (data.templateType != 0) {
		state.activeItemId = data.id
		tabPush(data)
	}
}

// 节点展开时，把 id 加进 expandedKeys
const handleNodeExpand = (data: Tree) => {
	if (!state.expandedKeys.includes(data.id)) {
		state.expandedKeys.push(data.id)
	}
}

// 节点折叠时，递归移除自己和所有后代 id
const handleNodeCollapse = (data: Tree) => {
	const node = treeRef.value.getNode(data.id)
	node.expanded = false
	data.children?.forEach(child => {
		handleNodeCollapse(child)
	})
	const collectIds = (n: Tree): number[] => [n.id, ...(n.children?.flatMap(collectIds) || [])]
	const removeIds = collectIds(data)
	state.expandedKeys = state.expandedKeys.filter(id => !removeIds.includes(id))
}

// 删除勾选的树节点
const deleteCheckedNode = () => {
	const allCheckedKeys = treeRef.value.getCheckedKeys()
	if (allCheckedKeys.length === 0) {
		ElMessage.warning('请勾选目录或者文件')
		return
	}
	ElMessageBox.confirm('此操作将删除目录或者文件, 是否继续?', '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			genTemplateApi
				.deleteList(allCheckedKeys)
				.then(() => {
					ElMessage.success('删除成功')
				})
				.then(() => {
					// 删除tab
					allCheckedKeys.forEach((tempId: number) => {
						handleTabRemove(tempId)
						state.expandedKeys = state.expandedKeys.filter(id => id !== tempId)
					})
				})
				.then(() => {
					initData()
				})
		})
		.catch(() => {
			ElMessage.info('已取消删除')
		})
}

// 判断树节点能否被拖拽
const treeAllowDrag = (_draggingNode: any): boolean => {
	// 这里允许所有节点拖拽
	return true
}

// 判断树节点是否允许放置
const treeAllowDrop = (draggingNode: any, dropNode: any, type: 'prev' | 'next' | 'inner'): boolean => {
	const dragData = draggingNode.data
	const dropData = dropNode.data

	// 1. inner 必须是目录（templateType === 0）
	if (type === 'inner') {
		// 如果父节点没变，不允许放置。树节点顺序是后端根据类型和名称排序，不是节点自有的顺序
		if (dragData.parentId === dropData.id) {
			return false
		}
		return dropData.templateType === 0
	} else if (type === 'prev' || type === 'next') {
		// 2. prev / next 永远允许同级拖拽，但父节点没变化则禁止
		return dragData.parentId !== dropData.parentId
	}

	return false
}

// 处理节点拖拽完成事件
const handleNodeDrop = (draggingNode: any, dropNode: any, dropType: 'before' | 'after' | 'inner', _ev: Event) => {
	const dragData = draggingNode.data
	const dropData = dropNode.data

	// 计算新的 parentId
	let newParentId = dragData.parentId

	if (dropType === 'inner') {
		// 拖入目录内部
		newParentId = dropData.id
	} else if (dropType === 'before' || dropType === 'after') {
		// 和目标节点同级
		newParentId = dropData.parentId
	}

	if (newParentId === dragData.parentId) {
		ElMessage.warning('不能移动到自身')
		return
	}

	// 构造请求参数
	const dataForm = {
		dragNodeId: dragData.id,
		newParentId: newParentId
	}

	// 调用 API 更新位置
	genTemplateApi.updateParent(dataForm).then(() => {
		ElMessage.success('移动成功')
		// 激活被拖拽的树节点
		state.activeItemId = dragData.id
		// 加入到tab中
		tabPush(dragData)
		initData() // 重新加载数据
	})
}

// 右键菜单 target：可辨识联合，节点命中带 node，空白命中 kind=blank
type TreeMenuTarget = { kind: 'treeNode'; node: Tree } | { kind: 'blank' }

// 入口上下文计算：空白=根级 parentId=0 路径 '/'；节点=该目录自身全路径
const resolveEntry = (t: TreeMenuTarget): { parentId: number; templatePath: string } => {
	if (t.kind === 'blank') {
		return { parentId: 0, templatePath: '/' }
	}
	return { parentId: t.node.id, templatePath: getFullPathById(t.node.id, state.treeList) }
}

const treeMenuItems = computed<ContextMenuItem<TreeMenuTarget>[]>(() => [
	{
		key: 'edit',
		label: '修改',
		icon: 'icon-edit',
		visible: t => t.kind === 'treeNode',
		onClick: t => {
			if (t.kind === 'treeNode') {
				updateTemplate(t.node)
			}
		}
	},
	{
		key: 'newDir',
		label: '新建目录',
		icon: 'icon-folder-add',
		visible: t => t.kind === 'blank' || (t.kind === 'treeNode' && t.node.templateType === 0),
		onClick: t => createEntry(0, t)
	},
	{
		key: 'newFile',
		label: '新建模板文件',
		icon: 'icon-file-add',
		visible: t => t.kind === 'blank' || (t.kind === 'treeNode' && t.node.templateType === 0),
		onClick: t => createEntry(1, t)
	},
	{
		key: 'newBinary',
		label: '新建二进制文件',
		icon: 'icon-file-unknown',
		visible: t => t.kind === 'blank' || (t.kind === 'treeNode' && t.node.templateType === 0),
		onClick: t => createEntry(2, t)
	}
])

// 节点右键
const handleNodeRightClick = (event: MouseEvent, data: Tree) => {
	event.preventDefault()
	treeMenuRef.value?.open(event, { kind: 'treeNode', node: data })
}

// 空白右键
const handleScrollWrapperRightClick = (event: MouseEvent) => {
	const target = event.target as HTMLElement
	if (target.closest('.custom-tree-node')) {
		return
	}
	treeMenuRef.value?.open(event, { kind: 'blank' })
}

// 新建目录/模板文件/二进制文件：templateType 0/1/2，parentId/templatePath 由入口决定
const createEntry = (templateType: number, t: TreeMenuTarget) => {
	formContext.templateType = templateType
	const { parentId, templatePath } = resolveEntry(t)
	formContext.parentId = parentId
	formContext.templatePath = templatePath
	nextTick(() => {
		formInitHandle()
	})
}

// 修改模板：templatePath 去掉自身尾段得到父目录路径
const updateTemplate = (node: Tree) => {
	formContext.templateType = node.templateType
	formContext.parentId = node.parentId
	const templatePath = getFullPathById(node.id, state.treeList).replace(node.fileName, '').replace(/\/$/, '')
	formContext.templatePath = templatePath ? templatePath : '/'
	nextTick(() => {
		formInitHandle(node.id)
	})
}

// 表单保存后链路：加入 tab → 刷新数据 → 激活文件类节点
const initAfterHandler = (tree: Tree) => {
	tabPush(tree)
	nextTick(() => {
		initData()
	})
	if (tree.templateType === 0) {
		return
	}
	// 激活模板文件
	state.activeItemId = tree.id
}

// 供容器 init 编排：清空搜索、按 keys 恢复展开
const resetSearch = () => {
	treeSearchText.value = ''
}

const restoreExpanded = (keys: number[]) => {
	keys.forEach(key => {
		const node = treeRef.value?.getNode(key)
		if (node) {
			node.expanded = true
		}
	})
}

defineExpose({
	resetSearch,
	restoreExpanded
})
</script>

<style scoped>
.tree-panel {
	display: flex;
	flex-direction: column;
	height: 100%;
	overflow: hidden;
}

.tree-scroll-wrapper {
	min-width: max-content;
	width: fit-content;
	overflow-x: auto;
	margin-bottom: 10px;
}

.custom-tree {
	display: block;
	width: 100%;
}

.custom-tree-node {
	display: flex;
	align-items: center;
	gap: 5px;
	position: relative;
}

.tree-node-actions {
	display: none;
	margin-left: 5px;
}

.custom-tree-node:hover .tree-node-actions {
	display: block;
}

.edit-icon {
	margin-left: auto;
	cursor: pointer;
	color: var(--el-text-color-secondary);
	font-size: 14px;
}
.edit-icon:hover {
	color: var(--el-color-primary);
}

/* 两种容器：tab 和 tree node 都可以挂红点 */
:deep(.el-badge__content.is-fixed.is-dot) {
	right: 0;
}
:deep(.el-badge__content.is-dot) {
	height: 6px;
	width: 6px;
}
</style>
