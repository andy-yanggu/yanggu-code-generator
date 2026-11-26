import { describe, expect, it } from 'vitest'
import { buildFileList, buildTree, collectDirtyFiles, findByIdFromTree, getFullPathById, getIcon, joinFileName, type Tree } from './tree-utils'

// 构造树节点的最小工厂：只必填 id/fileName/templateType，其余给合理默认，便于各用例覆写
const makeNode = (over: Partial<Tree> & { id: number; fileName: string; templateType: number }): Tree => ({
	templateName: over.fileName,
	parentId: 0,
	...over
}) as Tree

// 一份典型的嵌套样例树，覆盖目录/模板文件/二进制图片/二进制非文件
const buildFixture = (): Tree[] => [
	makeNode({ id: 1, fileName: 'root', templateType: 0, children: [makeNode({ id: 2, fileName: 'a.ftl', templateType: 1, parentId: 1, templateContent: 'contentA' }), makeNode({ id: 3, fileName: 'sub', templateType: 0, parentId: 1, children: [makeNode({ id: 4, fileName: 'b.ftl', templateType: 1, parentId: 3, templateContent: 'contentB' }), makeNode({ id: 5, fileName: 'logo', templateType: 2, parentId: 3, binaryOriginalFileName: 'logo.png' })] })] }),
	makeNode({ id: 6, fileName: 'top.ftl', templateType: 1, templateContent: 'contentTop' }),
	makeNode({ id: 7, fileName: 'lib', templateType: 2, binaryOriginalFileName: 'app.jar' })
]

describe('findByIdFromTree', () => {
	it('能查找到深层嵌套节点', () => {
		const found = findByIdFromTree(4, buildFixture())
		expect(found?.fileName).toBe('b.ftl')
	})

	it('能查找到根层级节点', () => {
		const found = findByIdFromTree(6, buildFixture())
		expect(found?.fileName).toBe('top.ftl')
	})

	it('id 不存在时返回 undefined', () => {
		expect(findByIdFromTree(999, buildFixture())).toBeUndefined()
	})

	it('空树返回 undefined', () => {
		expect(findByIdFromTree(1, [])).toBeUndefined()
	})
})

describe('getFullPathById', () => {
	it('根层级文件路径只含自身名称', () => {
		expect(getFullPathById(6, buildFixture())).toBe('top.ftl')
	})

	it('深层节点按祖先顺序以斜杠拼接', () => {
		expect(getFullPathById(4, buildFixture())).toBe('root/sub/b.ftl')
	})

	it('id 不存在返回空字符串', () => {
		expect(getFullPathById(999, buildFixture())).toBe('')
	})

	it('支持传入父级前缀', () => {
		expect(getFullPathById(6, buildFixture(), 'base')).toBe('base/top.ftl')
	})
})

describe('buildTree', () => {
	it('为模板文件缓存原始内容', () => {
		const tree = buildFixture()
		buildTree(tree)
		expect(findByIdFromTree(2, tree)?.originalTemplateContent).toBe('contentA')
		expect(findByIdFromTree(4, tree)?.originalTemplateContent).toBe('contentB')
		expect(findByIdFromTree(6, tree)?.originalTemplateContent).toBe('contentTop')
	})

	it('目录与二进制文件不写入原始内容', () => {
		const tree = buildFixture()
		buildTree(tree)
		expect(findByIdFromTree(1, tree)?.originalTemplateContent).toBeUndefined()
		expect(findByIdFromTree(5, tree)?.originalTemplateContent).toBeUndefined()
		expect(findByIdFromTree(7, tree)?.originalTemplateContent).toBeUndefined()
	})

	it('空数组不报错', () => {
		expect(() => buildTree([])).not.toThrow()
	})
})

describe('buildFileList', () => {
	it('收集所有非目录节点并拍平，按深度优先顺序', () => {
		const list = buildFileList(buildFixture())
		expect(list.map(item => item.id)).toEqual([2, 4, 5, 6, 7])
	})

	it('全是目录时返回空数组', () => {
		const list = buildFileList([makeNode({ id: 1, fileName: 'root', templateType: 0, children: [makeNode({ id: 2, fileName: 'sub', templateType: 0, parentId: 1 })] })])
		expect(list).toEqual([])
	})

	it('空树返回空数组', () => {
		expect(buildFileList([])).toEqual([])
	})
})

describe('joinFileName', () => {
	it('1 个文件', () => {
		expect(joinFileName([makeNode({ id: 1, fileName: 'A', templateType: 1 })])).toBe('A模板文件')
	})

	it('2-3 个文件用顿号连接并追加模板文件后缀', () => {
		const tabs = [makeNode({ id: 1, fileName: 'A', templateType: 1 }), makeNode({ id: 2, fileName: 'B', templateType: 1 })]
		expect(joinFileName(tabs)).toBe('A、B模板文件')
	})

	it('超过 3 个折叠为「前三个...等N个模板文件」', () => {
		const tabs = ['A', 'B', 'C', 'D', 'E'].map((name, i) => makeNode({ id: i + 1, fileName: name, templateType: 1 }))
		expect(joinFileName(tabs)).toBe('A、B、C...等5个模板文件')
	})

	it('空数组返回裸的模板文件文案（锁定当前行为）', () => {
		expect(joinFileName([])).toBe('模板文件')
	})
})

describe('collectDirtyFiles', () => {
	// 样例树：1=根目录(子:2脏文件, 3子目录(子:4脏文件, 5脏二进制)), 6=根层级脏文件, 7=根层级干净二进制；8=干净文件
	const buildDirtyFixture = (): Tree[] => [
		makeNode({ id: 1, fileName: 'root', templateType: 0, isEdited: false, children: [makeNode({ id: 2, fileName: 'a.ftl', templateType: 1, parentId: 1, isEdited: true }), makeNode({ id: 3, fileName: 'sub', templateType: 0, parentId: 1, children: [makeNode({ id: 4, fileName: 'b.ftl', templateType: 1, parentId: 3, isEdited: true }), makeNode({ id: 5, fileName: 'logo', templateType: 2, parentId: 3, isEdited: true })] })] }),
		makeNode({ id: 6, fileName: 'top.ftl', templateType: 1, isEdited: true }),
		makeNode({ id: 7, fileName: 'lib', templateType: 2, binaryOriginalFileName: 'app.jar' }),
		makeNode({ id: 8, fileName: 'clean.ftl', templateType: 1, isEdited: false })
	]

	const makeFinder = (treeList: Tree[]) => (id: number) => findByIdFromTree(id, treeList)

	it('勾选目录连带后代脏文件，排除干净文件与二进制', () => {
		const tree = buildDirtyFixture()
		const result = collectDirtyFiles([1], makeFinder(tree))
		// id5 是脏二进制（不会真脏，防御性排除），只应收到后代脏文件 2、4
		expect(result.map(item => item.id)).toEqual([2, 4])
	})

	it('勾选文件节点直接入集合（仅脏文件）', () => {
		const tree = buildDirtyFixture()
		const result = collectDirtyFiles([6, 8], makeFinder(tree))
		expect(result.map(item => item.id)).toEqual([6])
	})

	it('父目录与子文件同时勾选按 id 去重', () => {
		const tree = buildDirtyFixture()
		const result = collectDirtyFiles([1, 2, 4], makeFinder(tree))
		expect(result.map(item => item.id)).toEqual([2, 4])
	})

	it('stale key（已不在树上）静默过滤不报错', () => {
		const tree = buildDirtyFixture()
		const result = collectDirtyFiles([1, 999], makeFinder(tree))
		expect(result.map(item => item.id)).toEqual([2, 4])
	})

	it('全部干净返回空数组', () => {
		const tree = buildDirtyFixture()
		expect(collectDirtyFiles([7, 8], makeFinder(tree))).toEqual([])
	})

	it('空勾选返回空数组', () => {
		const tree = buildDirtyFixture()
		expect(collectDirtyFiles([], makeFinder(tree))).toEqual([])
	})

	it('返回的是原节点引用（回滚靠共享引用双向同步，禁止 clone）', () => {
		const tree = buildDirtyFixture()
		const result = collectDirtyFiles([1], makeFinder(tree))
		expect(result[0]).toBe(findByIdFromTree(2, tree))
	})
})

describe('getIcon', () => {
	const imageTypeList = ['png', 'jpg', 'jpeg', 'gif', 'svg', 'bmp', 'ico']

	it('目录展开态用打开文件夹图标', () => {
		expect(getIcon({ expanded: true }, makeNode({ id: 1, fileName: 'd', templateType: 0 }), imageTypeList)).toBe('icon-folder-open')
	})

	it('目录收起态用关闭文件夹图标', () => {
		expect(getIcon({ expanded: false }, makeNode({ id: 1, fileName: 'd', templateType: 0 }), imageTypeList)).toBe('icon-folder')
	})

	it('模板文件用文本图标', () => {
		expect(getIcon({ expanded: false }, makeNode({ id: 1, fileName: 'a.ftl', templateType: 1 }), imageTypeList)).toBe('icon-file-text')
	})

	it('二进制图片按扩展名命中用图片图标', () => {
		expect(getIcon({ expanded: false }, makeNode({ id: 1, fileName: 'logo', templateType: 2, binaryOriginalFileName: 'logo.png' }), imageTypeList)).toBe('icon-file-image')
	})

	it('非图片二进制用未知图标', () => {
		expect(getIcon({ expanded: false }, makeNode({ id: 1, fileName: 'lib', templateType: 2, binaryOriginalFileName: 'app.jar' }), imageTypeList)).toBe('icon-file-unknown')
	})

	it('二进制缺少原始文件名时降级为未知图标', () => {
		expect(getIcon({ expanded: false }, makeNode({ id: 1, fileName: 'x', templateType: 2 }), imageTypeList)).toBe('icon-file-unknown')
	})

	it('未知类型兜底为未知图标', () => {
		expect(getIcon({ expanded: false }, makeNode({ id: 1, fileName: 'x', templateType: 9 }), imageTypeList)).toBe('icon-file-unknown')
	})
})
