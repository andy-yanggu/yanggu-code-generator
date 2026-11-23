<template>
	<div class="panel-body">
		<div class="panel-header">
			<el-text tag="b" size="small">Freemarker变量</el-text>
			<el-tag size="small" type="info" effect="plain">{{ totalCount }}</el-tag>
			<el-button link size="small" :icon="Refresh" :loading="loading" title="重新加载" @click="emit('reload')"></el-button>
		</div>
		<el-input v-model="keyword" size="small" clearable :prefix-icon="Search" placeholder="搜索变量名 / 说明" style="margin-bottom: 6px"></el-input>
		<!-- 已有目录时失败只加一条面板内提示，不把旧树换掉（规格 6.2 ①「保留上一次成功加载的目录」、6.3「保留上次目录 + 面板内提示重试」） -->
		<div v-if="error && totalCount > 0" class="panel-error">
			<el-text size="small" type="danger">{{ error }}</el-text>
			<el-button link size="small" type="primary" @click="emit('reload')">重试</el-button>
		</div>
		<el-skeleton v-if="loading && totalCount === 0" :rows="6" animated></el-skeleton>
		<el-empty v-else-if="totalCount === 0" :description="error || emptyDescription" :image-size="60">
			<el-button v-if="error" size="small" @click="emit('reload')">重试</el-button>
		</el-empty>
		<el-scrollbar v-else class="panel-scrollbar">
			<el-tree
				ref="treeRef"
				:data="treeData"
				node-key="uid"
				:props="treeProps"
				:expand-on-click-node="false"
				:default-expanded-keys="expandedKeyList"
				:filter-node-method="filterNodeMethod"
				class="panel-tree"
			>
				<template #default="{ data }">
					<div class="variable-row">
						<template v-if="data.kind === 'GROUP'">
							<el-text tag="b" size="small">{{ data.name }}（{{ data.children.length }}）</el-text>
						</template>
						<template v-else>
							<el-tooltip :content="data.desc" placement="top" :show-after="400">
								<span class="variable-name">{{ data.name }}</span>
							</el-tooltip>
							<el-tag v-if="kindTag(data.kind)" size="small" :type="kindTag(data.kind)!.type" effect="plain">
								{{ kindTag(data.kind)!.label }}
							</el-tag>
							<el-tooltip v-if="data.value" :content="data.value" placement="left" :show-after="400">
								<el-tag class="variable-value" size="small" type="info" effect="plain">{{ data.value }}</el-tag>
							</el-tooltip>
							<el-tooltip v-if="data.nullable" content="该变量渲染时可能为 null，模板需用 <#if xxx??> 保护" placement="left">
								<el-tag size="small" type="warning" effect="plain">可空</el-tag>
							</el-tooltip>
							<el-tooltip v-if="canInsert(data)" :content="disabled ? '请先打开一个模板文件' : '插入到光标处'" placement="left">
								<el-button class="row-icon insert-button" link size="small" :disabled="disabled" @click.stop="handleInsert(data)">
									{{ data.kind === 'SCALAR' ? '插' : '插循环' }}
								</el-button>
							</el-tooltip>
						</template>
					</div>
				</template>
			</el-tree>
		</el-scrollbar>
	</div>
</template>

<script setup lang="ts">
import { Refresh, Search } from '@element-plus/icons-vue'
import type { VariableNode } from '@/types'
import { buildInsertText, buildVariableTree, canInsert, countVariables, type DisplayVariableNode, kindTag, matchKeyword } from '../variable-utils'

defineOptions({
	name: 'VariablePanelBody'
})

const props = defineProps({
	// 当前 tab 不是模板文件时禁用插入
	disabled: {
		type: Boolean,
		default: false
	},
	// 由宿主提供：把文本写进编辑器光标处，返回是否成功
	insert: {
		type: Function as PropType<(text: string, caret: number) => boolean>,
		required: true
	},
	// 目录状态一律由宿主（tree.vue）持有并下传：面板只在展开时才挂载，而规格 5.2 要求收起态
	// 也要有总项数徽标，故不能由面板自持加载（评审 M1）
	catalog: {
		type: Array as PropType<VariableNode[]>,
		default: () => [] as VariableNode[]
	},
	loading: {
		type: Boolean,
		default: false
	},
	// 空串表示无异常
	error: {
		type: String,
		default: ''
	},
	// 带值目录模式（测试页）：SCALAR_LIST 展开真实元素，行内显示 value 文本；
	// 编辑器结构目录不传，行为与引入口径完全不变
	valueMode: {
		type: Boolean,
		default: false
	}
})

// 重试由宿主执行（加载逻辑在宿主），面板只负责把意图传上去
const emit = defineEmits<{
	(e: 'reload'): void
}>()

const treeRef = ref()
const keyword = ref('')

const treeProps = { children: 'children', label: 'name' }
const treeData = computed(() => buildVariableTree(props.catalog, props.valueMode))
const totalCount = computed(() => countVariables(props.catalog, props.valueMode))

// 空态文案分两种口径：测试页目录要选了测试对象才有值可合成，此时说「该模板组无可用变量」会误导，
// 改成指向级联选择器；编辑器目录与选择无关，保留原文案
const emptyDescription = computed(() => (props.valueMode ? '请先选择测试对象' : '该模板组无可用变量'))

// 默认只展开顶层分组，268 条全展开反而看不到东西
const expandedKeyList = computed(() => treeData.value.filter(item => item.kind === 'GROUP').map(item => item.uid))

// 目录替换 / 失败后重挂载都会重建整批树节点，而关键词并未变化，只 watch keyword 会漏掉这些时机，
// 故一并 watch 数据与树实例，保证画面过滤状态与搜索框一致；filter 用可选调用，避开实例尚未暴露方法的窗口
watch([keyword, treeData, treeRef], () => treeRef.value?.filter?.(keyword.value), { flush: 'post', immediate: true })

// el-tree 把 filter-node-method 的形参声明为 TreeNodeData（即 Record<string, any>），比面板节点宽，故内部断言回 DisplayVariableNode
const filterNodeMethod = (value: string, data: Record<string, any>) => matchKeyword(data as DisplayVariableNode, value)

const handleInsert = (data: DisplayVariableNode) => {
	const result = buildInsertText(data)
	if (!result) {
		return
	}
	// 宿主返回 false 表示编辑器还没就绪（例如刚切到模板 tab、实例尚未挂载），
	// 这里给出可见反馈，而不是静默失败
	if (!props.insert(result.text, result.caret)) {
		ElMessage.warning('编辑器尚未就绪，请稍后重试')
	}
}
</script>

<style scoped>
.panel-body {
	display: flex;
	flex: 1;
	min-height: 0;
	flex-direction: column;
}

.panel-header {
	display: flex;
	align-items: center;
	gap: 6px;
	margin-bottom: 6px;
}

.panel-error {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 6px;
	margin-bottom: 6px;
	padding: 4px 6px;
	border-radius: 4px;
	background: var(--el-color-danger-light-9);
}

.panel-scrollbar {
	flex: 1;
	min-height: 0;
}

.variable-row {
	display: flex;
	align-items: center;
	gap: 4px;
	width: 100%;
	font-size: 12px;
}

.variable-name {
	font-family: var(--el-font-family-mono, Consolas, Monaco, monospace);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

/* 带值目录的行内值：与变量名同用等宽字体，灰底弱一档，占满剩余宽度后省略 */
.variable-value {
	max-width: 60%;
	flex-shrink: 1;
	font-family: var(--el-font-family-mono, Consolas, Monaco, monospace);
}

.variable-value :deep(.el-tag__content) {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.row-icon {
	font-size: 12px;
	color: var(--el-text-color-secondary);
}

/* 插入按钮 hover 才出现；此处用 visibility 而非 tree.vue 的 display，
   避免按钮出现/消失把同一行的标签挤得左右移动 */
.insert-button {
	visibility: hidden;
	flex-shrink: 0;
}

.variable-row:hover .insert-button {
	visibility: visible;
}

:deep(.el-tree-node__content) {
	height: 26px;
}
</style>
