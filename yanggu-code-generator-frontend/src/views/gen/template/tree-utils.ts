// 模板树的纯工具函数：不依赖任何响应式状态，纯输入 -> 输出，便于复用与单测

// 模板树节点
export interface Tree {
	// 主键ID
	id: number
	// 父级ID
	parentId: number
	// 模板名称
	templateName: string
	// 文件名称
	fileName: string
	// 描述
	templateDesc?: string
	// 模板类型（0-目录，1-模板文件，2-二进制文件）
	templateType: number
	// 原始内容（用于恢复）；归属域=edit-tabs（会话标记，随编辑侦测/保存回滚变化），见 2026-11-24 状态域拆分设计
	originalTemplateContent?: string
	// 模板内容
	templateContent?: string
	// 二进制原始文件名
	binaryOriginalFileName?: string
	// 子节点列表
	children?: Tree[]
	// 新增编辑状态属性；归属域=edit-tabs（会话标记），禁止在数据装载链路赋值
	isEdited?: boolean // 标记是否已编辑但未保存
}

// 图片扩展名清单：从不运行期变更，状态域拆分（Q3）后由 reactive 提为常量
export const IMAGE_TYPE_LIST = ['png', 'jpg', 'jpeg', 'gif', 'svg', 'bmp', 'git', 'ico']

// 根据 id 递归查找树节点
export const findByIdFromTree = (id: number, treeList: Tree[]): Tree | undefined => {
	for (const tree of treeList) {
		if (tree.id === id) {
			return tree
		}
		if (tree.children && tree.children.length > 0) {
			const tempTree = findByIdFromTree(id, tree.children!)
			if (tempTree) {
				return tempTree
			}
		}
	}
	return undefined
}

// 递归查找并拼接路径
export const getFullPathById = (id: number, nodes: Tree[], parentPath = ''): string => {
	for (const node of nodes) {
		const currentPath = parentPath ? `${parentPath}/${node.fileName}` : node.fileName
		if (node.id === id) {
			return currentPath
		}
		if (node.children?.length) {
			const result = getFullPathById(id, node.children, currentPath)
			if (result) {
				return result
			}
		}
	}
	return ''
}

// 构建树：为模板文件缓存原始内容，用于后续恢复编辑状态
export const buildTree = (treeList: Tree[]) => {
	treeList.forEach(treeItem => {
		if (treeItem.templateType === 1) {
			treeItem.originalTemplateContent = treeItem.templateContent
		}
		if (treeItem.children && treeItem.children.length > 0) {
			buildTree(treeItem.children)
		}
	})
}

// 找到所有的模板文件和二进制文件
export const buildFileList = (treeList: Tree[]) => {
	const templateContentList: Tree[] = []
	treeList.forEach(treeItem => {
		// 找到模板文件和二进制文件
		if (treeItem.templateType != 0) {
			templateContentList.push(treeItem)
		} else if (treeItem.children && treeItem.children.length > 0) {
			templateContentList.push(...buildFileList(treeItem.children))
		}
	})
	return templateContentList
}

// 回滚-勾选脏文件收集：每个勾选 key 经 findById 反查（stale key 静默过滤），
// 目录递归收集后代脏文件、文件节点直接判定；只收模板文件（1），目录/二进制（2）无编辑通道天然不可达。
// 返回原节点引用（tabList 与 treeList 共享引用，红点双向同步靠此同一性，禁止 clone）；Map 按 id 去重保持首次插入顺序
export const collectDirtyFiles = (checkedKeys: number[], findById: (id: number) => Tree | undefined): Tree[] => {
	const map = new Map<number, Tree>()
	const walk = (node: Tree) => {
		if (node.templateType !== 0) {
			if (node.templateType === 1 && node.isEdited) {
				map.set(node.id, node)
			}
		} else {
			node.children?.forEach(walk)
		}
	}
	checkedKeys.forEach(key => {
		const node = findById(key)
		if (node) {
			walk(node)
		}
	})
	return [...map.values()]
}

// 拼接文件名提示语：超过 3 个折叠为「前 3 个...等 N 个模板文件」
export const joinFileName = (editTabs: Tree[]): string => {
	if (editTabs.length > 3) {
		return (
			editTabs
				.slice(0, 3)
				.map(tab => tab.fileName)
				.join('、') + `...等${editTabs.length}个模板文件`
		)
	} else {
		return editTabs.map(tab => tab.fileName).join('、') + '模板文件'
	}
}

// 获取节点的图标：目录区分展开/收起，二进制按扩展名区分图片/未知
// imageTypeList 由调用方注入（v2 侧传 IMAGE_TYPE_LIST 常量），保持本模块无状态
export const getIcon = (node: { expanded?: boolean }, data: Tree, imageTypeList: string[]): string => {
	if (data.templateType === 0) {
		if (node.expanded) {
			return 'icon-folder-open'
		} else {
			return 'icon-folder'
		}
	} else if (data.templateType === 1) {
		return 'icon-file-text'
	} else if (data.templateType === 2) {
		const some = imageTypeList.some(item => data.binaryOriginalFileName?.endsWith(item))
		if (some) {
			return 'icon-file-image'
		} else {
			return 'icon-file-unknown'
		}
	} else {
		return 'icon-file-unknown'
	}
}
