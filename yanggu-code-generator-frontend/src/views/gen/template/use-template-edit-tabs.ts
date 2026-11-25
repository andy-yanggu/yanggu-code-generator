import { computed, reactive, watch } from 'vue'
import type { TabPaneName } from 'element-plus'
import type { Tree } from './tree-utils'

/**
 * 会话域：tabList / activeItemId / expandedKeys 的数据主权 + 红点侦测 + 纯数据 tab 动作
 * （use-template-tree-state 四域拆分之一）。
 *
 * 边界（见 2026-11-24 状态域拆分设计 决策 1/4）：
 * - 不出现 HTTP、不出现 ElMessageBox/ElMessage：带确认的关闭编排由编排层包装本域的纯数据动作；
 * - 节点对象经注入的 getNode 反查（来自数据域 findById），tab 元素与 treeList 节点是同一批对象引用
 *   ——红点、编辑态、保存 payload 全靠这份共享引用，本域所有变更一律原地 mutate，绝不 spread/clone。
 */

/** 会话域切片工厂：初值与复位共用同一口径（per-slice 工厂，决策 6） */
const createEditTabsSlice = () => ({
	// 标签页数据
	tabList: [] as Tree[],
	// 激活的 tab 项 ID
	activeItemId: -1,
	// 树展开键（视图态，复位链路与 tab 同属会话生命周期，随本域收编）
	expandedKeys: [] as number[]
})

export const useTemplateEditTabs = (getNode: (id: number) => Tree | undefined) => {
	const editTabs = reactive(createEditTabsSlice())

	const getElementFromTabList = (id: number): Tree | undefined => {
		if (editTabs.tabList.length === 0 || id === -1) {
			return { id: -1, templateContent: '', templateType: -1 } as Tree
		} else {
			return editTabs.tabList.find(item => item.id === id)
		}
	}

	// 激活的 tab 项数据：取值源是注入的 getNode（数据域 findById → 遍历 treeList），不是 tabList；
	// 保留树侧反查是为了「树上的内容变更也能驱动侦测」与「fullFilePath 随 parentId 变化」。
	// 占位对象回落语义与现版一致（消费方只对属性写，不对 ref 本身写，ComputedRef 即可）
	const activeTab = computed(() => {
		const treeData = getNode(editTabs.activeItemId)
		if (!treeData) {
			return { id: -1, templateContent: '', templateType: -1 } as Tree
		} else {
			return treeData
		}
	})

	// 添加tab（进行去重）
	const tabPush = (tree: Tree) => {
		if (tree.templateType === 0) {
			return
		}
		const find = editTabs.tabList.find(tab => tab.id === tree.id)
		if (find) {
			return
		}
		editTabs.tabList.push(tree)
	}

	// 树数据刷新后同步 tab：以新节点对象替换表项（保持引用同源），编辑中的项保留未保存内容
	const refreshTabData = (dataList: Tree[]) => {
		if (dataList.length === 0) {
			editTabs.tabList = []
			return
		}
		const tabLength = editTabs.tabList.length
		if (tabLength === 0) {
			return
		}
		// 保存当前的编辑状态
		const editStatusSet = new Set<number>()
		editTabs.tabList.forEach(tab => {
			if (tab.isEdited) {
				editStatusSet.add(tab.id)
			}
		})

		for (let index = 0; index < tabLength; index++) {
			const oldData = editTabs.tabList[index]
			const tabElementId = oldData.id
			const newData = dataList.find(item => item.id === tabElementId)!
			if (newData) {
				editTabs.tabList.splice(index, 1, newData)
			}
			if (editStatusSet.has(tabElementId)) {
				editTabs.tabList[index].templateContent = oldData.templateContent
				editTabs.tabList[index].isEdited = true
			}
		}
	}

	// 监听：激活 id + 内容 + 类型。源取 activeTab（treeList 反查），回调里的 tab 取 tabList 引用项：
	// 稳态下两者是同一对象（F06 SAME_REF），所以与旧版等价；
	// 但「树已换、tab 未重指向」的中间态下它们是两批对象，这份侦测必须与装载原子完成
	// （靠数据域 fetchTreeData 的 onLoaded 同步回调保证，见编排层 initData，评审 B1）
	watch(
		() => [activeTab.value.id, activeTab.value.templateContent, activeTab.value.templateType],
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
				activeTab.value.isEdited = false
				return
			}

			// 3) 只有在同一个 id 下内容发生变化才认为是编辑
			if (id === prevId && content !== prevContent) {
				tab.isEdited = true
				activeTab.value.isEdited = true
			}
		}
	)

	// ===== 纯数据关闭动作（带确认的编排在编排层，此处零弹窗零接口） =====

	// tab删除后的激活位选取（el-tabs tab-remove 契约：name 为 string | number，本域绑定 number id，接收后转回）
	const removeTab = (name: TabPaneName) => {
		const id = Number(name)
		const index = editTabs.tabList.findIndex(item => item.id === id)
		if (index <= -1) {
			return
		}
		// 删除tab
		editTabs.tabList.splice(index, 1)

		let newTabActiveName = editTabs.activeItemId
		// 删除的是否为当前tab
		if (editTabs.activeItemId === id && editTabs.tabList.length > 0) {
			// 优先尝试右侧标签
			if (index < editTabs.tabList.length) {
				newTabActiveName = editTabs.tabList[index].id
			} else {
				// 右侧无标签时选择左侧最后一个
				newTabActiveName = editTabs.tabList[editTabs.tabList.length - 1].id
			}
		}

		if (editTabs.tabList.length > 0) {
			// 设置新的激活项
			const activeTabItem = getElementFromTabList(newTabActiveName)
			if (activeTabItem) {
				editTabs.activeItemId = activeTabItem.id
			}
		}
	}

	// 关闭当前
	const closeCurrent = (item: Tree, index: number) => {
		editTabs.tabList = editTabs.tabList.filter(tab => tab.id !== item.id)

		if (editTabs.activeItemId === item.id && editTabs.tabList.length > 0) {
			let newActiveTab
			if (index < editTabs.tabList.length) {
				newActiveTab = editTabs.tabList[index]
			} else {
				newActiveTab = editTabs.tabList[editTabs.tabList.length - 1]
			}
			editTabs.activeItemId = newActiveTab.id
		}
	}

	// 关闭其他
	const closeOthers = (item: Tree) => {
		editTabs.tabList = [item]
		editTabs.activeItemId = item.id
	}

	// 关闭左侧
	const closeLeft = (item: Tree, index: number) => {
		editTabs.tabList = editTabs.tabList.slice(index)
		editTabs.activeItemId = item.id
	}

	// 关闭右侧
	const closeRight = (item: Tree, index: number) => {
		editTabs.tabList = editTabs.tabList.slice(0, index + 1)
		editTabs.activeItemId = item.id
	}

	// 关闭未修改的（直接过滤，无确认——本动作不经 handleCloseTabs，保持现版语义）
	const closeUnedited = (index: number) => {
		// 过滤掉未修改的标签页，只保留已修改的标签页
		editTabs.tabList = editTabs.tabList.filter(tab => tab.isEdited)

		// 如果当前激活的标签页仍然存在，不需要处理
		if (editTabs.tabList.some(tab => tab.id === editTabs.activeItemId)) {
			return
		}

		// 如果当前激活标签页被关闭了，需要选择新的激活标签页
		if (editTabs.tabList.length > 0) {
			// 优先选择当前位置或之前的标签页
			if (index < editTabs.tabList.length) {
				editTabs.activeItemId = editTabs.tabList[index].id
			} else {
				// 如果索引超出范围，选择最后一个标签页
				editTabs.activeItemId = editTabs.tabList[editTabs.tabList.length - 1].id
			}
		} else {
			// 如果没有剩余标签页，将激活项设为-1
			editTabs.activeItemId = -1
		}
	}

	// 关闭全部
	const closeAll = () => {
		editTabs.tabList = []
		editTabs.activeItemId = -1
	}

	// ===== 会话标记落点（保存成功/测试回刷后由编排层或写域经回调调用，同一引用落到树侧） =====

	// 保存成功后清红点并同步基线内容
	const applyEditRollback = (tabs: Tree[]) => {
		tabs.forEach(tab => {
			tab.isEdited = false
			tab.originalTemplateContent = tab.templateContent
		})
	}

	// 丢弃编辑（供关闭确认"否(N)"调用）：内容回滚到基线并清红点——
	// 与 applyEditRollback 相反，这里不动基线而是把 templateContent 还原回 originalTemplateContent。
	// tab 与 treeList 节点同一引用，就地 mutate 即可让树上红点同步熄灭；
	// 必须显式逐个回滚：watch 只观察当前激活 tab，多 tab 一起丢弃时非激活项不会自动清标记。
	const discardEdits = (tabs: Tree[]) => {
		tabs.forEach(tab => {
			tab.templateContent = tab.originalTemplateContent
			tab.isEdited = false
		})
	}

	// 测试页回刷：写库新内容为基线并清红点（现版 tempTree 三行赋值归位到会话标记通道）
	const rollbackWithContent = (tab: Tree, content: string) => {
		tab.templateContent = content
		tab.originalTemplateContent = content
		tab.isEdited = false
	}

	// 本域复位
	const reset = () => {
		Object.assign(editTabs, createEditTabsSlice())
	}

	return {
		editTabs,
		activeTab,
		getElementFromTabList,
		tabPush,
		refreshTabData,
		removeTab,
		closeCurrent,
		closeOthers,
		closeLeft,
		closeRight,
		closeUnedited,
		closeAll,
		applyEditRollback,
		discardEdits,
		rollbackWithContent,
		reset
	}
}

export type TemplateEditTabsDomain = ReturnType<typeof useTemplateEditTabs>
