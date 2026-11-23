import { describe, expect, it } from 'vitest'
import { buildVariableTree, canInsert, countVariables, decorateVariables, kindTag, matchKeyword, buildInsertText } from './variable-utils'
import type { VariableNode, VariableKind } from '@/types'

// 最小变量节点工厂：只覆盖被测字段
const makeNode = (name: string, kind: VariableKind, extra: Partial<VariableNode> = {}): VariableNode => ({
	name,
	path: name,
	javaType: kind === 'MAP' ? 'Map<String, Object>' : 'String',
	kind,
	desc: '',
	loopVar: '',
	nullable: false,
	children: [],
	...extra
})

const tableModel: VariableNode = makeNode('tableModelList', 'MODEL_LIST', {
	path: 'tableModelList',
	loopVar: 'table',
	children: [
		makeNode('className', 'SCALAR', { path: 'tableModelList.className' }),
		makeNode('fieldList', 'MODEL_LIST', {
			path: 'tableModelList.fieldList',
			loopVar: 'field',
			children: [makeNode('attrName', 'SCALAR', { path: 'tableModelList.fieldList.attrName' })]
		})
	]
})

describe('decorateVariables', () => {
	it('集合节点的 expr 是循环变量名，selfExpr 是可用路径', () => {
		const [node] = decorateVariables([tableModel])

		expect(node.selfExpr).toBe('tableModelList')
		expect(node.expr).toBe('table')
		// 孙层用父级循环变量名作前缀
		const fieldList = node.children[1]
		expect(fieldList.expr).toBe('field')
		expect(fieldList.children[0].selfExpr).toBe('field.attrName')
	})

	it('标量集合不展开子行', () => {
		const [node] = decorateVariables([makeNode('importList', 'SCALAR_LIST', { loopVar: 'i', children: [makeNode('String', 'SCALAR')] })])

		expect(node.children).toEqual([])
	})

	it('非法标识符的 MAP key 降级为下标写法', () => {
		const [node] = decorateVariables([
			makeNode('tableTemplatePropertyData', 'MAP', {
				children: [makeNode('api-prefix', 'SCALAR', { path: 'tableTemplatePropertyData.api-prefix' })]
			})
		])

		expect(node.children[0].selfExpr).toBe("tableTemplatePropertyData['api-prefix']")
	})

	// 钉住字符集：下划线 / 数字 / $ 属于合法标识符，只有非法首字符才降级
	it('合法但非纯字母的 key 仍走点号写法', () => {
		const [node] = decorateVariables([
			makeNode('propMap', 'MAP', {
				children: [
					makeNode('use_swagger2', 'SCALAR', { path: 'propMap.use_swagger2' }),
					makeNode('$ref', 'SCALAR', { path: 'propMap.$ref' }),
					makeNode('2fa', 'SCALAR', { path: 'propMap.2fa' })
				]
			})
		])

		expect(node.children[0].selfExpr).toBe('propMap.use_swagger2')
		expect(node.children[1].selfExpr).toBe('propMap.$ref')
		expect(node.children[2].selfExpr).toBe("propMap['2fa']")
	})

	it('无属性的 MAP 节点补一条不可插入的说明行', () => {
		const [node] = decorateVariables([makeNode('tableTemplatePropertyData', 'MAP')])

		expect(node.children.length).toBe(1)
		expect(node.children[0].kind).toBe('GROUP')
		expect(node.children[0].name).toBe('无子变量（未配置属性或属于其他模板组）')
		expect(canInsert(node.children[0])).toBe(false)
		// 说明行不计入面板标题的总项数
		expect(countVariables([makeNode('tableTemplatePropertyData', 'MAP')])).toBe(1)
	})
})

// 测试页带值目录：valueMode 两点差异 + 向后兼容
describe('decorateVariables valueMode', () => {
	// 带值目录的 MODEL_LIST 子级是后端合成的真实元素行（name=field[0]，kind=OBJECT）
	const valueFieldList = makeNode('fieldList', 'MODEL_LIST', {
		loopVar: 'field',
		value: '共 2 条',
		children: [
			makeNode('field[0]', 'OBJECT', {
				path: 'fieldList[0]',
				children: [makeNode('attrName', 'SCALAR', { path: 'fieldList[0].attrName', value: 'id' })]
			}),
			makeNode('field[1]', 'OBJECT', {
				path: 'fieldList[1]',
				children: [makeNode('attrName', 'SCALAR', { path: 'fieldList[1].attrName', value: 'name' })]
			})
		]
	})

	it('valueMode=true 时标量集合展开真实元素', () => {
		const importList = makeNode('importList', 'SCALAR_LIST', {
			loopVar: 'i',
			value: '共 2 条',
			children: [makeNode('[0]', 'SCALAR', { path: 'importList[0]', value: 'a' }), makeNode('[1]', 'SCALAR', { path: 'importList[1]', value: 'b' })]
		})
		const [node] = decorateVariables([importList], '', '', true)

		expect(node.children.length).toBe(2)
		expect(node.children[0].value).toBe('a')
		expect(node.children[1].value).toBe('b')
	})

	it('valueMode=true 时元素子字段以循环变量名为前缀，不产生下标垃圾表达式', () => {
		const [node] = decorateVariables([valueFieldList], '', '', true)

		expect(node.value).toBe('共 2 条')
		// 元素行本体：OBJECT 本就不可插入，且 valueMode 下整行被 elementRow 标记双重禁用
		expect(canInsert(node.children[0])).toBe(false)
		expect(node.children[0].children[0].selfExpr).toBe('field.attrName')
		expect(node.children[1].children[0].selfExpr).toBe('field.attrName')
		// 插入的是父列表行的循环骨架，语义与编辑器一致
		expect(buildInsertText(node)!.text).toBe('<#list fieldList as field>\n  \n</#list>')
	})

	it('value 字段在结构目录上为 undefined，向后兼容', () => {
		const [node] = decorateVariables([valueFieldList])

		expect(node.value).toBe('共 2 条')
		// 默认 valueMode=false：SCALAR_LIST 仍不展开，与旧基线一致
		const [collapsed] = decorateVariables([
			makeNode('importList', 'SCALAR_LIST', { loopVar: 'i', children: [makeNode('String', 'SCALAR')] })
		])
		expect(collapsed.children).toEqual([])
		// 旧基线口径：列表行自身 1 + 两个元素行 + 各自 1 个子字段
		expect(countVariables([valueFieldList])).toBe(3 + 2)
	})

	// 决策 B：带值目录的收起态徽标把真实元素一并算进去，数字比编辑器大不少
	it('valueMode 下标量集合元素计入总项数', () => {
		const importList = makeNode('importList', 'SCALAR_LIST', {
			loopVar: 'i',
			children: [makeNode('[0]', 'SCALAR', { value: 'a' }), makeNode('[1]', 'SCALAR', { value: 'b' })]
		})

		// 编辑器口径：只统计父行那一条插入行
		expect(countVariables([importList])).toBe(1)
		// 测试页口径：父行 + 2 个真实元素
		expect(countVariables([importList], true)).toBe(3)
	})
})

describe('buildVariableTree', () => {
	it('顶层标量归入标量变量组，容器各自成组', () => {
		const treeData = buildVariableTree([makeNode('projectName', 'SCALAR'), makeNode('className', 'SCALAR'), tableModel])

		expect(treeData[0].kind).toBe('GROUP')
		expect(treeData[0].name).toBe('标量变量')
		expect(treeData[0].children.length).toBe(2)
		expect(treeData[1].name).toBe('tableModelList')
		expect(treeData[1].kind).toBe('MODEL_LIST')
	})
})

describe('buildInsertText', () => {
	it('标量插入 ${}', () => {
		const [node] = decorateVariables([makeNode('projectName', 'SCALAR')])

		expect(buildInsertText(node)).toEqual({ text: '${projectName}', caret: 14 })
	})

	it('集合插入循环骨架且光标落在循环体内', () => {
		const [node] = decorateVariables([makeNode('fieldList', 'MODEL_LIST', { loopVar: 'field' })])
		const result = buildInsertText(node)!

		expect(result.text).toBe('<#list fieldList as field>\n  \n</#list>')
		// 紧跟换行与两空格缩进之后
		expect(result.caret).toBe(result.text.indexOf('\n') + 3)
	})

	it('对象与 MAP 容器行不可插入', () => {
		const [obj] = decorateVariables([makeNode('entityBaseClass', 'OBJECT')])
		const [map] = decorateVariables([makeNode('tableTemplatePropertyData', 'MAP')])

		expect(buildInsertText(obj)).toBeNull()
		expect(buildInsertText(map)).toBeNull()
		expect(canInsert(obj)).toBe(false)
		expect(canInsert(map)).toBe(false)
		expect(canInsert({ kind: 'SCALAR_LIST' })).toBe(true)
	})

	// 带值目录的元素行：SCALAR 型 `[0]` 行也不可插入（插值字面量违背模板语义）
	it('valueMode 元素行不显示插入', () => {
		const importList = makeNode('importList', 'SCALAR_LIST', {
			loopVar: 'i',
			children: [makeNode('[0]', 'SCALAR', { path: 'importList[0]', value: 'a' })]
		})
		const [node] = decorateVariables([importList], '', '', true)
		const row = node.children[0]

		expect(row.elementRow).toBe(true)
		expect(row.selfExpr).toBe('i[0]')
		expect(canInsert(row)).toBe(false)
		expect(buildInsertText(row)).toBeNull()
		// 父行仍可插循环骨架
		expect(canInsert(node)).toBe(true)
	})
})

describe('辅助函数', () => {
	it('总项数递归统计，不含虚拟分组', () => {
		expect(countVariables([tableModel])).toBe(4)
	})

	// 独立于上面的用例：只有带子节点的 SCALAR_LIST 能证明「子行不计入」这条规则生效
	it('标量集合的虚拟子行不计入总项数', () => {
		const importList = makeNode('importList', 'SCALAR_LIST', { loopVar: 'i', children: [makeNode('String', 'SCALAR')] })

		expect(countVariables([importList])).toBe(1)
	})

	it('kind 映射语义色标签', () => {
		expect(kindTag('SCALAR')).toEqual({ label: '标量', type: 'info' })
		expect(kindTag('MODEL_LIST')).toEqual({ label: '列表', type: 'primary' })
		expect(kindTag('OBJECT')).toEqual({ label: '对象', type: 'warning' })
		expect(kindTag('MAP')).toEqual({ label: '属性', type: 'success' })
		expect(kindTag('GROUP')).toBeNull()
	})

	it('搜索命中变量名、说明与表达式', () => {
		const [node] = decorateVariables([makeNode('attrName', 'SCALAR', { desc: '属性名称' })])

		expect(matchKeyword(node, 'attr')).toBe(true)
		expect(matchKeyword(node, '属性名')).toBe(true)
		expect(matchKeyword(node, 'zzz')).toBe(false)
		expect(matchKeyword(node, '')).toBe(true)
	})
})
