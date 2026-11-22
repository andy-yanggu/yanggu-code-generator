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
	// 自身可用表达式，如 table.field.attrName；即 ${} 内该写的内容
	selfExpr: string
	// 子级表达式前缀：集合用循环变量名，其余同 selfExpr
	expr: string
	// el-tree 的 node-key
	uid: string
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

// 装饰目录：自顶向下合成表达式，集合的子级以循环变量名为前缀
export const decorateVariables = (nodeList: VariableNode[], parentExpr = '', uidPrefix = ''): DisplayVariableNode[] => {
	return nodeList.map((node, index) => {
		const uid = uidPrefix ? `${uidPrefix}-${index}` : `${index}`
		const selfExpr = appendName(parentExpr, node.name)
		const expr = isList(node.kind) ? node.loopVar || node.name : selfExpr
		// 标量集合的骨架文本已含 ${i}，再挂一行「String」无信息量，故不展开
		const children = node.kind === 'SCALAR_LIST' ? [] : decorateVariables(node.children, expr, uid)
		if (node.kind === 'MAP' && children.length === 0) {
			children.push(makeEmptyMapNode(uid))
		}
		return { ...node, selfExpr, expr, uid, children }
	})
}

// 生成面板数据：标量收进一个虚拟分组，容器变量各自成组
export const buildVariableTree = (nodeList: VariableNode[]): DisplayVariableNode[] => {
	const decorated = decorateVariables(nodeList)
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
export const countVariables = (nodeList: VariableNode[]): number => {
	return nodeList.reduce((sum, node) => {
		const childCount = node.kind === 'SCALAR_LIST' ? 0 : countVariables(node.children)
		return sum + 1 + childCount
	}, 0)
}

export const canInsert = (kind: DisplayKind): boolean => kind === 'SCALAR' || isList(kind as VariableKind)

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

// 点击插入的文本与光标偏移；容器行（对象 / MAP / 分组）不可插入
export const buildInsertText = (node: DisplayVariableNode): { text: string; caret: number } | null => {
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
