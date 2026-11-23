// 模板变量目录的纯工具函数：不依赖响应式状态，纯输入 -> 输出，便于复用与单测

import type { VariableKind, VariableNode } from '@/types'

// 面板显示用节点：额外的 GROUP 是前端合成的顶层分组，后端不产出
export type DisplayKind = VariableKind | 'GROUP'

export interface DisplayVariableNode {
	name: string
	path: string
	javaType: string
	kind: DisplayKind
	desc: string
	loopVar: string
	nullable: boolean
	// 带值目录（测试页）的渲染值文本；结构目录为 undefined，行内不显示
	value?: string
	// 自身可用表达式，如 table.field.attrName；即 ${} 内该写的内容
	selfExpr: string
	// 子级表达式前缀：集合用循环变量名，其余同 selfExpr
	expr: string
	// el-tree 的 node-key
	uid: string
	// 带值目录合成的集合元素行（`field[0]` / `[0]`）：名字不可作表达式片段，不提供插入
	elementRow?: boolean
	children: DisplayVariableNode[]
}

// 顶层标量归入的虚拟分组名（规格 3.1 结构自动分区）
const SCALAR_GROUP_NAME = '标量变量'

// 合法 FreeMarker 标识符，否则降级为 ['x-y'] 下标写法
const IDENTIFIER_REGEX = /^[A-Za-z_$][A-Za-z0-9_$]*$/

const appendName = (prefix: string, name: string): string => {
	if (!IDENTIFIER_REGEX.test(name)) {
		const quoted = `['${name}']`
		return prefix ? `${prefix}${quoted}` : quoted
	}
	return prefix ? `${prefix}.${name}` : name
}

const isList = (kind: VariableKind): boolean => kind === 'MODEL_LIST' || kind === 'SCALAR_LIST'

// MAP 节点无子变量时的说明行：避免用户以为面板加载不完整（规格 6.3）
// 文案对两种成因都为真：当前组确实未配属性，或是其他模板组的 MAP（后端有意不展开）
const makeEmptyMapNode = (uid: string): DisplayVariableNode => ({
	name: '无子变量（未配置属性或属于其他模板组）',
	path: '',
	javaType: '',
	kind: 'GROUP',
	desc: '',
	loopVar: '',
	nullable: false,
	selfExpr: '',
	expr: '',
	uid: `${uid}-empty-map`,
	children: []
})

// 本行是否为带值目录合成的集合元素行（MODEL_LIST 的 `field[0]` / SCALAR_LIST 的 `[0]`）：
// 名字含下标、不可作为表达式片段，也不提供插入（插入值字面量违背模板语义，循环骨架请插父行）
const isElementRow = (valueMode: boolean, parentKind: DisplayKind | ''): boolean =>
	valueMode && (parentKind === 'MODEL_LIST' || parentKind === 'SCALAR_LIST')

// 装饰目录：自顶向下合成表达式，集合的子级以循环变量名为前缀；
// valueMode=true（测试页带值目录）时多一个入参 parentKind，用于识别后端合成的集合元素行（见下方 isElementRow）
export const decorateVariables = (
	nodeList: VariableNode[],
	parentExpr = '',
	uidPrefix = '',
	valueMode = false,
	parentKind: DisplayKind | '' = ''
): DisplayVariableNode[] => {
	return nodeList.map((node, index) => {
		const uid = uidPrefix ? `${uidPrefix}-${index}` : `${index}`
		const elementRow = isElementRow(valueMode, parentKind)
		// 元素行名字不参与真实表达式（appendName 会把 `field[0]` 包成 ['field[0]'] 垃圾片段）：
		// 名字自带下标（`field[0]` / `[0]`）时直用名字展示——前者已含循环变量名，后者由父前缀补成合法的 `i[0]`；
		// 元素行的 childPrefix 沿用父前缀（循环变量名），保证子字段合成出 `field.attrName`；
		// 插入已被 buildInsertText 禁用（插值字面量违背模板语义，循环骨架请插父行）
		let selfExpr: string
		if (!elementRow) {
			selfExpr = appendName(parentExpr, node.name)
		} else if (node.name.startsWith('[')) {
			selfExpr = `${parentExpr}${node.name}`
		} else {
			selfExpr = node.name
		}
		const expr = isList(node.kind) ? node.loopVar || node.name : selfExpr
		// MODEL_LIST 恒以循环变量名作子级前缀（旧规则，两种目录同款）；元素行往下则丢弃自己那段非标识符名字，
		// 直接沿用本层拿到的父前缀；其余 kind 照 selfExpr 拼接，MAP key 的 ['x-y'] 降级写法不受影响
		const childPrefix =
			node.kind === 'MODEL_LIST' || (valueMode && node.kind === 'SCALAR_LIST') ? expr : elementRow ? parentExpr : selfExpr
		const children =
			node.kind === 'SCALAR_LIST' && !valueMode ? [] : decorateVariables(node.children, childPrefix, uid, valueMode, node.kind)
		if (node.kind === 'MAP' && children.length === 0) {
			children.push(makeEmptyMapNode(uid))
		}
		return { ...node, elementRow, selfExpr, expr, uid, children }
	})
}

// 生成面板数据：标量收进一个虚拟分组，容器变量各自成组
export const buildVariableTree = (nodeList: VariableNode[], valueMode = false): DisplayVariableNode[] => {
	const decorated = decorateVariables(nodeList, '', '', valueMode)
	const scalarList: DisplayVariableNode[] = []
	const containerList: DisplayVariableNode[] = []
	decorated.forEach(node => {
		if (node.kind === 'SCALAR') {
			scalarList.push(node)
		} else {
			containerList.push(node)
		}
	})
	const groupList: DisplayVariableNode[] = []
	if (scalarList.length > 0) {
		groupList.push({
			name: SCALAR_GROUP_NAME,
			path: '',
			javaType: '',
			kind: 'GROUP',
			desc: '',
			loopVar: '',
			nullable: false,
			selfExpr: '',
			expr: '',
			uid: 'scalar-group',
			children: scalarList
		})
	}
	return [...groupList, ...containerList]
}

// 递归统计变量条目数（不含虚拟分组），用于面板标题徽标
// 标量集合在编辑器目录下只有 1 个骨架行，子行不计入；带值目录（valueMode）的子行是真实元素，计入总数
export const countVariables = (nodeList: VariableNode[], valueMode = false): number => {
	return nodeList.reduce((sum, node) => {
		const childCount = node.kind === 'SCALAR_LIST' && !valueMode ? 0 : countVariables(node.children, valueMode)
		return sum + 1 + childCount
	}, 0)
}

// 行内插入按钮可见性：与 buildInsertText 同口径——带值目录的元素行（elementRow）名字不是合法表达式片段，不显示插入
export const canInsert = (node: Pick<DisplayVariableNode, 'kind' | 'elementRow'>): boolean =>
	!node.elementRow && (node.kind === 'SCALAR' || isList(node.kind as VariableKind))

export const kindTag = (kind: DisplayKind): { label: string; type: 'info' | 'primary' | 'warning' | 'success' } | null => {
	switch (kind) {
		case 'SCALAR':
			return { label: '标量', type: 'info' }
		case 'SCALAR_LIST':
		case 'MODEL_LIST':
			return { label: '列表', type: 'primary' }
		case 'OBJECT':
			return { label: '对象', type: 'warning' }
		case 'MAP':
			return { label: '属性', type: 'success' }
		default:
			return null
	}
}

// 搜索命中：变量名、中文说明、可用表达式三者之一
export const matchKeyword = (node: DisplayVariableNode, keyword: string): boolean => {
	if (!keyword) {
		return true
	}
	const lower = keyword.toLowerCase()
	return node.name.toLowerCase().includes(lower) || node.desc.toLowerCase().includes(lower) || node.selfExpr.toLowerCase().includes(lower)
}

// 点击插入的文本与光标偏移；容器行（对象 / MAP / 分组）不可插入；
// 带值目录的元素行（field[0] / [0]）也不可插入：名字不是合法表达式片段，插入值字面量违背模板语义，循环骨架请插父行
export const buildInsertText = (node: DisplayVariableNode): { text: string; caret: number } | null => {
	if (node.elementRow) {
		return null
	}
	if (node.kind === 'SCALAR') {
		const text = `\${${node.selfExpr}}`
		return { text, caret: text.length }
	}
	if (!isList(node.kind as VariableKind)) {
		return null
	}
	const loopVar = node.loopVar || node.name
	const head = `<#list ${node.selfExpr} as ${loopVar}>`
	const tail = `</#list>`
	const text = `${head}\n  \n${tail}`
	// 光标落在循环体首行缩进之后
	return { text, caret: head.length + 3 }
}
