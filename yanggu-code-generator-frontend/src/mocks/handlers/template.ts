/**
 * 模板组/模板树 Mock Handlers（Docker MySQL 不可用期间的纯前端联调通道）
 *
 * 覆盖 tree-v2 全链路：组分页、树装载、变量目录、内容单存/批存、详情回刷、节点增删改。
 * 树数据模块级持有：updateContent 写回内存节点，重开抽屉能看到保存结果，行为贴近真实后端。
 */

import { http, ok, okVoid } from './_shared'

// ===== 模板树数据（嵌套结构，后端 /gen/template/tree 即返回 children 树） =====
interface MockTreeNode {
	id: number
	parentId: number
	templateGroupId: number
	templateName: string
	fileName: string
	templateDesc: string
	templateType: number
	templateContent: string
	binaryOriginalFileName: string
	conditionExpression: string
	children?: MockTreeNode[]
}

const makeNode = (partial: Partial<MockTreeNode> & { id: number; fileName: string; templateType: number }): MockTreeNode => ({
	parentId: 0,
	templateGroupId: 100,
	templateName: partial.fileName,
	templateDesc: '',
	templateContent: '',
	binaryOriginalFileName: '',
	conditionExpression: '',
	...partial
})

const mockTree: MockTreeNode[] = [
	makeNode({
		id: 1001,
		fileName: 'backend',
		templateType: 0,
		children: [
			makeNode({
				id: 1002,
				parentId: 1001,
				fileName: 'controller',
				templateType: 0,
				children: [
					makeNode({
						id: 1003,
						parentId: 1002,
						templateName: 'Controller模板',
						fileName: '${className}Controller.java.ftl',
						templateType: 1,
						templateContent: 'package ${packageName}.controller;\n\nimport org.springframework.web.bind.annotation.*;\n\n/**\n * ${tableComment}\n */\n@RestController\n@RequestMapping("${pathName}")\npublic class ${className}Controller {\n\n    // TODO: ${tableComment}接口\n}\n'
					})
				]
			}),
			makeNode({
				id: 1004,
				parentId: 1001,
				templateName: 'Service模板',
				fileName: '${className}Service.java.ftl',
				templateType: 1,
				templateContent: 'package ${packageName}.service;\n\n/**\n * ${tableComment} Service\n */\npublic class ${className}Service {\n}\n'
			}),
			makeNode({
				id: 1005,
				parentId: 1001,
				templateName: '图片资源',
				fileName: 'logo.png',
				templateType: 2,
				binaryOriginalFileName: 'logo.png',
				templateContent: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI4MCIgaGVpZ2h0PSI4MCI+PHJlY3Qgd2lkdGg9IjgwIiBoZWlnaHQ9IjgwIiBmaWxsPSIjNDA5RUZGIi8+PC9zdmc+'
			}),
			makeNode({
				id: 1006,
				parentId: 1001,
				templateName: '依赖包',
				fileName: 'app.jar',
				templateType: 2,
				binaryOriginalFileName: 'app.jar'
			})
		]
	}),
	makeNode({
		id: 1007,
		templateName: 'pom模板',
		fileName: 'pom.xml.ftl',
		templateType: 1,
		templateContent: '<project>\n    <groupId>${packageName}</groupId>\n    <artifactId>${projectName}</artifactId>\n</project>\n'
	})
]

// 递归写回内容（模拟 updateContent 持久化）
const writeBackContent = (nodes: MockTreeNode[], id: number, content: string): boolean => {
	for (const node of nodes) {
		if (node.id === id) {
			node.templateContent = content
			return true
		}
		if (node.children && writeBackContent(node.children, id, content)) {
			return true
		}
	}
	return false
}

const findNodeById = (nodes: MockTreeNode[], id: number): MockTreeNode | undefined => {
	for (const node of nodes) {
		if (node.id === id) {
			return node
		}
		const found = node.children ? findNodeById(node.children, id) : undefined
		if (found) {
			return found
		}
	}
	return undefined
}

// ===== 变量目录（结构目录，value 缺省） =====
const makeVar = (name: string, javaType: string, kind: string, desc: string, children: any[] = []): any => ({
	name,
	path: name,
	javaType,
	kind,
	desc,
	loopVar: '',
	nullable: false,
	children
})

const mockVariableCatalog = [
	makeVar('packageName', 'String', 'SCALAR', '包名'),
	makeVar('className', 'String', 'SCALAR', '类名'),
	makeVar('tableComment', 'String', 'SCALAR', '表描述'),
	makeVar('fieldList', 'List<FieldVO>', 'MODEL_LIST', '字段列表', [makeVar('fieldName', 'String', 'SCALAR', '字段名')]),
	makeVar('projectConfig', 'Map', 'MAP', '项目配置', [])
]

// ===== 模板组分页 =====
const mockGroupRecords = [
	{ id: 100, groupName: 'Mock单体java项目模板', type: 1, groupDesc: 'mock：表模板组', groupPropertyCount: 3, createTime: '2026-11-25 10:00:00', updateTime: '2026-11-25 10:00:00' },
	{ id: 101, groupName: 'Mock项目模板组', type: 0, groupDesc: 'mock：项目模板组', groupPropertyCount: 1, createTime: '2026-11-25 10:00:00', updateTime: '2026-11-25 10:00:00' }
]

// ===== 保存类请求体 =====
// _shared 的 http 是 Proxy 包装（返回类型退化为 (url, ...args: any[])），丢掉了 msw 的请求体泛型，
// 所以只能对 json() 结果做显式断言（默认返回类型是不含业务字段的 DefaultBodyType）
type ContentPayload = Pick<MockTreeNode, 'id' | 'templateContent'>
const readJson = async <T>(request: Request): Promise<T> => (await request.json()) as unknown as T

export const templateHandlers = [
	// 模板组分页
	http.post('/gen/templateGroup/page', () => ok({ records: mockGroupRecords, total: mockGroupRecords.length, pageNum: 1, pageSize: 10 })),

	// 树数据
	http.get('/gen/template/tree', () => ok(mockTree)),

	// 变量目录
	http.get('/gen/template/variableCatalog', () => ok(mockVariableCatalog)),

	// 详情（测试回刷链路）
	http.get('/gen/template/detail', ({ request }) => {
		const id = Number(new URL(request.url).searchParams.get('id'))
		return ok(findNodeById(mockTree, id) ?? null)
	}),

	// 单文件保存：写回内存节点
	http.put('/gen/template/updateContent', async ({ request }) => {
		const { id, templateContent } = await readJson<ContentPayload>(request)
		writeBackContent(mockTree, id, templateContent)
		return okVoid()
	}),

	// 批量保存（关闭确认链）
	http.put('/gen/template/updateContentBatch', async ({ request }) => {
		const list = await readJson<ContentPayload[]>(request)
		list.forEach(item => writeBackContent(mockTree, item.id, item.templateContent))
		return okVoid()
	}),

	// 拖拽改父级 / 节点增删改：只返回成功，不改 mockTree（前端本地状态会自行变更）。
	// 注意：本组占位接口不能用于验证“节点操作后刷新”类场景（刷新会把旧数据拉回来），
	// 拿它当拖拽/增删改的取证依据会得出假阳性
	http.put('/gen/template/updateParent', () => okVoid()),
	http.post('/gen/template/add', () => okVoid()),
	http.put('/gen/template/update', () => okVoid()),
	http.delete('/gen/template/delete', () => okVoid()),
	http.delete('/gen/template/deleteList', () => okVoid())
]
