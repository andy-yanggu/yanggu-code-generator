import { computed, nextTick, reactive, ref, watch, type Ref } from 'vue'
import type { Action, TabPaneName } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { genTemplateApi } from '@/api'
import { isEmpty } from '@/utils/tool'
import { buildFileList, buildTree, findByIdFromTree, getFullPathById, getIcon as resolveNodeIcon, joinFileName, type Tree } from './tree-utils'

/**
 * 模板树核心状态与动作（无 DOM 依赖）。
 *
 * 关键约束（设计文档风险表最高危项）：tabList 元素与 treeList 节点是同一批对象引用
 * （buildFileList 从 treeList 里挑出来的），红点、编辑态、保存确认全靠这份共享引用双向同步。
 * 因此本模块所有变更一律原地 mutate，绝不 spread/clone，搬运时逐条对照 v1 tree.vue。
 */

/**
 * 核心状态的无参初始化对象工厂：reactive 初值与 reset 复位共用同一口径，
 * 后续新增字段只需改工厂一处，避免初始化与重置两处漂移
 */
const createTreeState = () => ({
	// 树形节点数据
	treeList: [] as Tree[],
	// 激活的 tab 项 ID
	activeItemId: -1,
	// 树展开键：随工厂进 state，reset 整表覆盖时一并清空
	expandedKeys: [] as number[],
	// 标签页数据
	tabList: [] as Tree[]
})

export const useTemplateTreeState = (templateGroupId: Ref<number>) => {
	// 树数据 + tab + 激活项 + 展开键（visible 已上移容器，state 不再承载抽屉显隐）
	const state = reactive(createTreeState())

	const imageTypeList = ref(['png', 'jpg', 'jpeg', 'gif', 'svg', 'bmp', 'git', 'ico'])
	const submitLoading = ref(false)

	// 激活的 tab 项数据
	const activeTabItem: Ref<Tree> = computed(() => {
		const treeData = findByIdFromTree(state.activeItemId, state.treeList)
		if (!treeData) {
			return { id: -1, templateContent: '', templateType: -1 } as Tree
		} else {
			return treeData
		}
	})

	// 激活的 tab 项的完整路径
	const fullFilePath = computed(() => {
		// 仅用于建立依赖，显示读取
		// 当激活的tab的parentId改变时，路径可能会变化
		const _ = activeTabItem.value.parentId
		return getFullPathById(activeTabItem.value.id, state.treeList)
	})

	// 只有模板文件（templateType 1）有编辑器可插入，目录与二进制文件无处可插
	const canInsertVariable = computed(() => activeTabItem.value.templateType === 1)

	// 获取节点的图标：绑定组件内的图片扩展名清单
	const getIcon = (node: { expanded?: boolean }, data: Tree): string => resolveNodeIcon(node, data, imageTypeList.value)

	const getElementFromTabList = (id: number): Tree | undefined => {
		if (state.tabList.length === 0 || id === -1) {
			return { id: -1, templateContent: '', templateType: -1 } as Tree
		} else {
			return state.tabList.find(item => item.id === id)
		}
	}

	// 添加tab（进行去重）
	const tabPush = (tree: Tree) => {
		if (tree.templateType === 0) {
			return
		}
		const find = state.tabList.find(tab => tab.id === tree.id)
		if (find) {
			return
		}
		state.tabList.push(tree)
	}

	const refreshTabData = (dataList: Tree[]) => {
		if (dataList.length === 0) {
			state.tabList = []
			return
		}
		const tabLength = state.tabList.length
		if (tabLength === 0) {
			return
		}
		// 保存当前的编辑状态
		const editStatusSet = new Set<number>()
		state.tabList.forEach(tab => {
			if (tab.isEdited) {
				editStatusSet.add(tab.id)
			}
		})

		for (let index = 0; index < tabLength; index++) {
			const oldData = state.tabList[index]
			const tabElementId = oldData.id
			const newData = dataList.find(item => item.id === tabElementId)!
			if (newData) {
				state.tabList.splice(index, 1, newData)
			}
			if (editStatusSet.has(tabElementId)) {
				state.tabList[index].templateContent = oldData.templateContent
				state.tabList[index].isEdited = true
			}
		}
	}

	// 监听：激活 id + 内容 + 类型
	watch(
		() => [activeTabItem.value.id, activeTabItem.value.templateContent, activeTabItem.value.templateType],
		([id, content, type], [prevId, prevContent]) => {
			// 1) 只处理可编辑的文本模板
			if (type !== 1) {
				return
			}

			const tab = getElementFromTabList(id as number)
			if (!tab) {
				return
			}

			// 2) 如果当前内容 == 原始内容 → 取消编辑状态
			if (content === tab.originalTemplateContent) {
				tab.isEdited = false
				activeTabItem.value.isEdited = false
				return
			}

			// 3) 只有在同一个 id 下内容发生变化才认为是编辑
			if (id === prevId && content !== prevContent) {
				tab.isEdited = true
				activeTabItem.value.isEdited = true
			}
		}
	)

	// 只刷新数据，不改变其他状态
	const initData = async () => {
		const data = await genTemplateApi.treeData(templateGroupId.value)
		state.treeList = data
		buildTree(state.treeList)
		const templateContentList = buildFileList(data)
		// 更新一下tab中的数据，已新加载数据为准
		refreshTabData(templateContentList)
	}

	// 供容器 init 使用：拉树数据并 buildTree，返回文件列表；DOM 相关的展开恢复/首项激活由容器编排
	const loadTreeData = async (): Promise<Tree[]> => {
		const data = await genTemplateApi.treeData(templateGroupId.value)
		state.treeList = data
		buildTree(state.treeList)
		return buildFileList(data)
	}

	// 抽屉重复打开时的状态复位：用工厂产物整表覆盖 reactive，保证与初值同口径（DOM 相关复位由容器/面板各自负责）
	const reset = () => {
		Object.assign(state, createTreeState())
	}

	// 保存模板内容
	const saveTemplateContent = () => {
		if (!activeTabItem.value.isEdited) {
			return
		}
		submitLoading.value = true
		const dataForm = {
			id: state.activeItemId,
			templateContent: activeTabItem.value.templateContent
		}
		genTemplateApi
			.updateContent(dataForm)
			.then(() => {
				ElMessage.success({
					message: '保存成功',
					duration: 500
				})
				// 修改编辑状态
				activeTabItem.value.isEdited = false
				activeTabItem.value.originalTemplateContent = activeTabItem.value.templateContent
			})
			.finally(() => {
				submitLoading.value = false
			})
	}

	// 刷新当前模板内容（测试页回传编辑过的 id 列表 → 逐个 detailData 刷内容清红点）
	const refreshActiveItemContent = (editTemplateIdList: number[]) => {
		if (isEmpty(editTemplateIdList)) {
			return
		}
		editTemplateIdList.forEach(tempId => {
			const queryForm = {
				id: tempId
			}
			genTemplateApi.detailData(queryForm).then(detailData => {
				const tempTree = findByIdFromTree(tempId, state.treeList)
				if (tempTree) {
					tempTree.templateContent = detailData.templateContent
					tempTree.originalTemplateContent = detailData.templateContent
					tempTree.isEdited = false
				}
			})
		})
	}

	// 通用关闭处理（含未保存三态确认）
	const handleCloseTabs = (toCloseTabs: Tree[], closeTab: () => void) => {
		const editTabs = toCloseTabs.filter(tab => tab.isEdited)

		if (editTabs.length === 0) {
			closeTab()
		} else {
			const fileNameJoin = joinFileName(editTabs)
			// 关闭确认
			const message = `${fileNameJoin}已修改未保存，是否保存后再关闭？`
			ElMessageBox.confirm(message, '提示', {
				distinguishCancelAndClose: true,
				confirmButtonText: '是(Y)',
				cancelButtonText: '否(N)',
				type: 'warning'
			})
				.then(() => {
					const dataFormList = editTabs.map(tab => {
						return {
							id: tab.id,
							templateContent: tab.templateContent
						}
					})
					genTemplateApi.updateContentList(dataFormList).then(() => {
						ElMessage.success('保存成功')
						editTabs.forEach(tab => {
							tab.isEdited = false
							tab.originalTemplateContent = tab.templateContent
						})
						nextTick(() => {
							// 等待视图更新完成
							closeTab()
						})
					})
				})
				.catch((action: Action) => {
					if (action === 'cancel') {
						nextTick(() => {
							// 等待视图更新完成
							closeTab()
						})
					}
				})
		}
	}

	// tab删除（el-tabs tab-remove 契约：name 为 string | number，本组件绑定 number id，接收后转回）
	const handleTabRemove = (name: TabPaneName) => {
		const id = Number(name)
		// 找到要删除的 tab
		const index = state.tabList.findIndex(item => item.id === id)
		if (index <= -1) {
			return
		}

		const tab = state.tabList[index]

		// 调用通用关闭逻辑
		handleCloseTabs([tab], () => {
			// 删除tab
			state.tabList.splice(index, 1)

			let newTabActiveName = state.activeItemId
			// 删除的是否为当前tab
			if (state.activeItemId === id && state.tabList.length > 0) {
				// 优先尝试右侧标签
				if (index < state.tabList.length) {
					newTabActiveName = state.tabList[index].id
				} else {
					// 右侧无标签时选择左侧最后一个
					newTabActiveName = state.tabList[state.tabList.length - 1].id
				}
			}

			if (state.tabList.length > 0) {
				// 设置新的激活项
				const activeTab = getElementFromTabList(newTabActiveName)
				if (activeTab) {
					state.activeItemId = activeTab.id
				}
			}
		})
	}

	// 关闭当前tab
	const closeCurrentTab = (item: Tree, index: number) => {
		handleCloseTabs([item], () => {
			state.tabList = state.tabList.filter(tab => tab.id !== item.id)

			if (state.activeItemId === item.id && state.tabList.length > 0) {
				let newActiveTab
				if (index < state.tabList.length) {
					newActiveTab = state.tabList[index]
				} else {
					newActiveTab = state.tabList[state.tabList.length - 1]
				}
				state.activeItemId = newActiveTab.id
			}
		})
	}

	// 关闭其他tab
	const closeOtherTabs = (item: Tree) => {
		const otherTabs = state.tabList.filter(tab => tab.id !== item.id)
		handleCloseTabs(otherTabs, () => {
			state.tabList = [item]
			state.activeItemId = item.id
		})
	}

	// 关闭左侧tab
	const closeLeftTabs = (item: Tree, index: number) => {
		const leftTabs = state.tabList.slice(0, index)
		handleCloseTabs(leftTabs, () => {
			state.tabList = state.tabList.slice(index)
			state.activeItemId = item.id
		})
	}

	// 关闭右侧tab
	const closeRightTabs = (item: Tree, index: number) => {
		const rightTabs = state.tabList.slice(index + 1)
		handleCloseTabs(rightTabs, () => {
			state.tabList = state.tabList.slice(0, index + 1)
			state.activeItemId = item.id
		})
	}

	// 关闭未修改的tab（直接过滤，无确认）
	const closeUneditedTabs = (index: number) => {
		// 过滤掉未修改的标签页，只保留已修改的标签页
		state.tabList = state.tabList.filter(tab => tab.isEdited)

		// 如果当前激活的标签页仍然存在，不需要处理
		if (state.tabList.some(tab => tab.id === state.activeItemId)) {
			return
		}

		// 如果当前激活标签页被关闭了，需要选择新的激活标签页
		if (state.tabList.length > 0) {
			// 优先选择当前位置或之前的标签页
			if (index < state.tabList.length) {
				state.activeItemId = state.tabList[index].id
			} else {
				// 如果索引超出范围，选择最后一个标签页
				state.activeItemId = state.tabList[state.tabList.length - 1].id
			}
		} else {
			// 如果没有剩余标签页，将激活项设为-1
			state.activeItemId = -1
		}
	}

	// 关闭全部tab
	const closeAllTabs = () => {
		handleCloseTabs(state.tabList, () => {
			state.tabList = []
			state.activeItemId = -1
		})
	}

	// 关闭抽屉方法。确认保存修改；done 由容器注入（改写容器持有的 visible ref）
	const handleClose = (done: () => void) => {
		const editTabs: Tree[] = []
		// 递归遍历treeList，找出isEdited为true的节点
		const findAndClose = (nodes: Tree[]) => {
			for (const node of nodes) {
				if (node.isEdited) {
					editTabs.push(node)
				}
				if (node.children) {
					findAndClose(node.children)
				}
			}
		}
		findAndClose(state.treeList)
		if (editTabs.length === 0) {
			done()
			return
		}
		const fileNameJoin = joinFileName(editTabs)
		const message = `${fileNameJoin}已修改未保存，是否保存后再关闭？`
		ElMessageBox.confirm(message, '提示', {
			distinguishCancelAndClose: true,
			confirmButtonText: '是(Y)',
			cancelButtonText: '否(N)',
			type: 'warning'
		})
			.then(() => {
				// 保存修改后关闭
				const dataFormList = editTabs.map(tab => {
					return {
						id: tab.id,
						templateContent: tab.templateContent
					}
				})
				genTemplateApi.updateContentList(dataFormList).then(() => {
					ElMessage.success({
						message: '保存成功',
						duration: 500,
						onClose: () => {
							done()
						}
					})
				})
			})
			.catch((action: Action) => {
				// 丢弃修改直接关闭
				if (action === 'cancel') {
					done()
				}
			})
	}

	return {
		state,
		imageTypeList,
		submitLoading,
		activeTabItem,
		fullFilePath,
		canInsertVariable,
		getIcon,
		getElementFromTabList,
		tabPush,
		refreshTabData,
		initData,
		loadTreeData,
		reset,
		saveTemplateContent,
		refreshActiveItemContent,
		handleCloseTabs,
		handleTabRemove,
		closeCurrentTab,
		closeOtherTabs,
		closeLeftTabs,
		closeRightTabs,
		closeUneditedTabs,
		closeAllTabs,
		handleClose
	}
}

export type TemplateTreeState = ReturnType<typeof useTemplateTreeState>
