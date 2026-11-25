<template>
	<div class="panel-body">
		<!-- 左右分组：标题与问号同属「这是什么」贴左，统计与刷新靠右。之前四个元素用同一个 6px 间距在左端串成一排，
		     实测 257px 的行只用了 173px、右边 84px 全程空着，拥挤感来自堆积而不是宽度不够 -->
		<div class="panel-header">
			<el-text class="panel-title" tag="b" size="small" truncated>Freemarker变量</el-text>
			<!-- 双击是个隐形交互，行内插入按钮又只在 hover 时出现，解释性文案需要一个落脚处 -->
			<el-tooltip placement="bottom-start" :show-after="200">
				<template #content>
					<div style="max-width: 240px; line-height: 1.6; white-space: pre-line">{{ usageTip }}</div>
				</template>
				<el-icon class="header-tip-icon"><QuestionFilled></QuestionFilled></el-icon>
			</el-tooltip>
			<div class="header-actions">
				<!-- 总数不用 el-tag：面板每一行都在用 tag（类型 / 值 / 可空），标题栏再带边框会把统计读成又一个标签 -->
				<el-text class="panel-count" size="small" type="info">共 {{ totalCount }} 项</el-text>
				<el-tooltip content="重新加载">
					<el-button link size="small" :icon="Refresh" :loading="loading" @click="emit('reload')"></el-button>
				</el-tooltip>
			</div>
		</div>
		<!-- 行间距由 .panel-body 的 row-gap 统一掌控，不在子项上再挂内联 margin -->
		<el-input v-model="keyword" size="small" clearable :prefix-icon="Search" placeholder="搜索变量名 / 说明"></el-input>
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
					<!-- 双击整行即插入：热区比 hover 才出现的「插」按钮大很多；不可插的行（元素行 / 对象 / MAP / 分组）
					     由 buildInsertText 返回 null 静默兜住，与这些行本来就不显示插入按钮一致 -->
					<div class="variable-row" @dblclick="handleRowDblclick($event, data)">
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
						</template>
					</div>
				</template>
			</el-tree>
		</el-scrollbar>
	</div>
</template>

<script setup lang="ts">
import { QuestionFilled, Refresh, Search } from '@element-plus/icons-vue'
import type { VariableNode } from '@/types'
import { buildInsertText, buildVariableTree, countVariables, type DisplayVariableNode, kindTag, matchKeyword } from '../variable-utils'

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
	// 目录状态一律由宿主（tree-v2.vue / test.vue）持有并下传：面板只在展开时才挂载，而规格 5.2 要求收起态
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

// 两种目录要多交代一句的东西不一样：带值目录的灰底标签看着像硬编码样例值，得说明它来自真实渲染数据且敏感字段已掩码；
// 结构目录则要点明口径来自 Model 类。文案里的 <#list> 只能放在脚本字符串里，写进模板会被当成标签解析
const usageTip = computed(() =>
	props.valueMode
		? '双击任意一行即可把变量插到编辑器光标处\n列表行插入 <#list …> 循环骨架，元素行（field[0]）与对象 / 属性行不参与插入\n灰底标签是所选测试对象的真实渲染值，敏感字段已由后端掩码'
		: '双击任意一行即可把变量插到编辑器光标处\n列表行插入 <#list …> 循环骨架\n目录来自模板组绑定的 Model 类'
)

// 默认只展开顶层分组，268 条全展开反而看不到东西
const expandedKeyList = computed(() => treeData.value.filter(item => item.kind === 'GROUP').map(item => item.uid))

// 目录替换 / 失败后重挂载都会重建整批树节点，而关键词并未变化，只 watch keyword 会漏掉这些时机，
// 故一并 watch 数据与树实例，保证画面过滤状态与搜索框一致；filter 用可选调用，避开实例尚未暴露方法的窗口
watch([keyword, treeData, treeRef], () => treeRef.value?.filter?.(keyword.value), { flush: 'post', immediate: true })

// el-tree 把 filter-node-method 的形参声明为 TreeNodeData（即 Record<string, any>），比面板节点宽，故内部断言回 DisplayVariableNode
const filterNodeMethod = (value: string, data: Record<string, any>) => matchKeyword(data as DisplayVariableNode, value)

const handleInsert = (data: DisplayVariableNode) => {
	// 禁用态判定收在这里而不是只给按钮加 :disabled：按钮挡得住单击，挡不住行级双击，
	// 而测试页切到「渲染结果」时编辑器仍挂在 v-model 上，绕开门控会往看不见的模板正文里写内容
	if (props.disabled) {
		return
	}
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

// 按钮上连击会派生两次 click（各插一次），再叠加一次冒泡到行的 dblclick 就变成插三次，
// 所以这里按事件源把插入按钮排除。本来想用 @dblclick.stop 拦，实测拦不住：
// EP Button 只把 click 声明成 emit，dblclick 只能走 attrs 透传，而外层 el-tooltip 会重建 trigger 属性，
// 这条链不可靠；改成看 target 就不依赖任何组件内部实现
const handleRowDblclick = (event: MouseEvent, data: DisplayVariableNode) => {
	if ((event.target as HTMLElement).closest('.insert-button')) {
		return
	}
	handleInsert(data)
}
</script>

<style scoped>
.panel-body {
	display: flex;
	flex: 1;
	min-height: 0;
	flex-direction: column;
	/* 纵向节奏统一由这里给：之前靠 header 的 margin-bottom 加搜索框的内联 margin-bottom 两段各自拼，
		改一处就漏一处（实测去掉内联值后搜索框与目录之间直接贴成了 0） */
	row-gap: 8px;
}

.panel-header {
	display: flex;
	align-items: center;
	/* 行高对齐下方 24px 的搜索框：原来 header 只有 20px，两行基线错位也是这一带显得紧的原因之一 */
	height: 24px;
	/* 标题与问号靠这个间距 + 图标自身 padding 拉开，视觉上约 6px */
	gap: 4px;
}

/* flex 子项默认 min-width:auto 撑不开省略号，这里必须归零，面板拖到 240 下限时标题自己缩而不挤右侧控件 */
.panel-title {
	min-width: 0;
}

/* 右组用 margin-left:auto 顶到行尾，把原本堆在末尾的空白让给两组之间 */
.header-actions {
	display: flex;
	align-items: center;
	gap: 8px;
	margin-left: auto;
}

.panel-count {
	/* 数字等宽，加载后项数变化时不会把右边的刷新按钮推得左右跳 */
	font-variant-numeric: tabular-nums;
}

/* 说明图标：14px 本体热区太小，补 padding 抬到 18px。
   必须同时改 content-box：EP 全局重置了 box-sizing:border-box，否则 padding 会吃掉 el-icon 的 1em 宽高、
   盒子仍是 14px 而字形被挤小 */
.header-tip-icon {
	box-sizing: content-box;
	padding: 2px;
	font-size: 14px;
	color: var(--el-text-color-secondary);
	/* tooltip 是 hover 触发，点它没有任何动作，用 help 说「这里有解释」；
		pointer 承诺的是「我能点」，反而给了个兑现不了的空头承诺 */
	cursor: help;
}

/* 可交互感靠 hover 变色给，不靠光标：tooltip 弹出来本身就是最直接的反应 */
.header-tip-icon:hover {
	color: var(--el-color-primary);
}

.panel-error {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 6px;
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
	/* 双击插入是本行的主交互，默认的双击选词高亮会看着像行被选中了，这里关掉；
	   代价是这行的文字不能再拖选复制 */
	user-select: none;
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

/* 插入按钮 hover 才出现；用 visibility 而非 display，
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
