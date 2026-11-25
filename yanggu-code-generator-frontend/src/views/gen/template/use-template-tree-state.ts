import { computed, nextTick, type Ref } from 'vue'
import type { Action, TabPaneName } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getIcon as resolveNodeIcon, IMAGE_TYPE_LIST, joinFileName, type Tree } from './tree-utils'
import { useTemplateTreeData } from './use-template-tree-data'
import { useTemplateEditTabs } from './use-template-edit-tabs'
import { useTemplatePersistence, type TemplatePersistenceDeps } from './use-template-persistence'

/**
 * 模板树状态编排层（见 2026-11-24 状态域拆分设计，方案 A 四刀）：
 * 1. 组装三个数据域（tree-data 装载 / edit-tabs 会话 / persistence 写接口）并显式接线跨域依赖；
 * 2. 承载 UI 交互编排（8 处 ElMessageBox/ElMessage 确认链），数据域内零弹窗零接口混入；
 * 3. 对消费组件保持 facade 导出契约零变化（决策 5），state 为按域分权的聚合视图。
 *
 * 关键约束（设计文档风险表最高危项）：tabList 元素与 treeList 节点是同一批对象引用，
 * 红点、编辑态、保存确认全靠这份共享引用双向同步，任何域都不得 spread/clone 节点对象。
 * 组装必须发生在 setup 同步阶段：各域内 watch 的生命周期挂在调用者作用域上，挪进异步回调会泄漏。
 */

export const useTemplateTreeState = (templateGroupId: Ref<number>) => {
	// ===== 三域组装（存储按域分权，编排层自身不持有任何字段） =====
	const treeData = useTemplateTreeData(templateGroupId)
	const editTabs = useTemplateEditTabs(treeData.findById)

	const persistence = useTemplatePersistence({
		getActiveTab: () => editTabs.activeTab.value,
		getActiveId: () => editTabs.editTabs.activeItemId,
		applyEditRollback: editTabs.applyEditRollback,
		findNode: treeData.findById,
		rollbackWithContent: editTabs.rollbackWithContent
	} satisfies TemplatePersistenceDeps)

	// ===== 聚合视图：字段仍只有一份实例（住在各自域内），此处 getter 透传读 + setter 透传写，
	// 组件侧 state.xxx 用法零感知（v-model 与手工赋值走透传 setter） =====
	const state = {
		get treeList() {
			return treeData.treeData.treeList
		},
		set treeList(v: Tree[]) {
			treeData.treeData.treeList = v
		},
		get tabList() {
			return editTabs.editTabs.tabList
		},
		set tabList(v: Tree[]) {
			editTabs.editTabs.tabList = v
		},
		get activeItemId() {
			return editTabs.editTabs.activeItemId
		},
		set activeItemId(v: number) {
			editTabs.editTabs.activeItemId = v
		},
		get expandedKeys() {
			return editTabs.editTabs.expandedKeys
		},
		set expandedKeys(v: number[]) {
			editTabs.editTabs.expandedKeys = v
		}
	}

	// ===== 跨域派生查询（源跨 tree/tab 两域，归编排层） =====
	// 激活的 tab 项数据（会话域 computed，占位对象回落语义不变）
	const activeTabItem = editTabs.activeTab

	// 激活的 tab 项的完整路径
	const fullFilePath = computed(() => {
		// 仅用于建立依赖，显示读取
		// 当激活的tab的parentId改变时，路径可能会变化
		const _ = activeTabItem.value.parentId
		return treeData.getFullPath(activeTabItem.value.id)
	})

	// 只有模板文件（templateType 1）有编辑器可插入，目录与二进制文件无处可插
	const canInsertVariable = computed(() => activeTabItem.value.templateType === 1)

	// 获取节点的图标：绑定 IMAGE_TYPE_LIST 常量（Q3 定稿，不再经 reactive 中转）
	const getIcon = (node: { expanded?: boolean }, data: Tree): string => resolveNodeIcon(node, data, IMAGE_TYPE_LIST)

	// ===== 装载接线：数据域拉树 → 会话域刷 tab（域间不互 import，依赖图只在此处出现） =====
	// 只刷新数据，不改变其他状态。刷 tab 必须以同步回调注入数据域的装载段：
	// 「treeList 整表替换」与「tabList 重指向」之间不能出现 microtask 间隙，否则 content-watch
	// 会抢在重指向前把脏标记抹掉，未保存内容被库内原文覆盖（评审 B1，勿改回 await 之后再调）
	const initData = (): Promise<Tree[]> => treeData.fetchTreeData(editTabs.refreshTabData)

	// 供容器 init 使用：拉树数据并 buildTree，返回文件列表；DOM 相关的展开恢复/首项激活由容器编排。
	// 首开时 tabList 为空（init 先 reset），refreshTabData 本就早退，故不传回调
	const loadTreeData = (): Promise<Tree[]> => treeData.fetchTreeData()

	// 抽屉重复打开时的状态复位：各域工厂各清各家（DOM 相关复位由容器/面板各自负责）
	const reset = () => {
		editTabs.reset()
		treeData.reset()
		persistence.reset()
	}

	// ===== 交互编排（弹窗/提示唯一所在地） =====
	// 保存模板内容：写域调接口+落标记，成功提示在编排层链上追加
	const saveTemplateContent = () => {
		persistence.saveActive()?.then(() => {
			ElMessage.success({
				message: '保存成功',
				duration: 500
			})
		})
	}

	// 刷新当前模板内容（测试页回传编辑过的 id 列表 → 写域逐个 detailData 刷内容清红点）
	const refreshActiveItemContent = persistence.refreshAfterTest

	// 通用关闭处理（含未保存三态确认）
	const handleCloseTabs = (toCloseTabs: Tree[], closeTab: () => void) => {
		const dirtyTabs = toCloseTabs.filter(tab => tab.isEdited)

		if (dirtyTabs.length === 0) {
			closeTab()
		} else {
			const fileNameJoin = joinFileName(dirtyTabs)
			// 关闭确认
			const message = `${fileNameJoin}已修改未保存，是否保存后再关闭？`
			ElMessageBox.confirm(message, '提示', {
				distinguishCancelAndClose: true,
				confirmButtonText: '是(Y)',
				cancelButtonText: '否(N)',
				type: 'warning'
			})
				.then(() => {
					const dataFormList = dirtyTabs.map(tab => {
						return {
							id: tab.id,
							templateContent: tab.templateContent
						}
					})
					persistence.batchSave(dataFormList).then(() => {
						ElMessage.success('保存成功')
						editTabs.applyEditRollback(dirtyTabs)
						nextTick(() => {
							// 等待视图更新完成
							closeTab()
						})
					})
				})
				.catch((action: Action) => {
					if (action === 'cancel') {
						// 否(N)＝不保存丢弃：先把编辑内容回滚到基线并清红点，再关闭 tab，
						// 否则共享引用上的 isEdited / 未保存内容会残留（树上红点不灭、重开又变脏）。
						editTabs.discardEdits(dirtyTabs)
						nextTick(() => {
							// 等待视图更新完成
							closeTab()
							ElMessage.info(`${joinFileName(dirtyTabs)}已关闭，修改未保存`)
						})
					}
				})
		}
	}

	// ===== 带确认的关闭动作 = handleCloseTabs 包装 + 会话域纯数据动作 =====
	// tab删除（el-tabs tab-remove 契约：name 为 string | number，本组件绑定 number id，接收后转回）
	const handleTabRemove = (name: TabPaneName) => {
		const id = Number(name)
		const tab = editTabs.editTabs.tabList.find(item => item.id === id)
		if (!tab) {
			return
		}
		handleCloseTabs([tab], () => {
			editTabs.removeTab(name)
		})
	}

	// 关闭当前tab
	const closeCurrentTab = (item: Tree, index: number) => {
		handleCloseTabs([item], () => {
			editTabs.closeCurrent(item, index)
		})
	}

	// 关闭其他tab
	const closeOtherTabs = (item: Tree) => {
		const otherTabs = editTabs.editTabs.tabList.filter(tab => tab.id !== item.id)
		handleCloseTabs(otherTabs, () => {
			editTabs.closeOthers(item)
		})
	}

	// 关闭左侧tab
	const closeLeftTabs = (item: Tree, index: number) => {
		const leftTabs = editTabs.editTabs.tabList.slice(0, index)
		handleCloseTabs(leftTabs, () => {
			editTabs.closeLeft(item, index)
		})
	}

	// 关闭右侧tab
	const closeRightTabs = (item: Tree, index: number) => {
		const rightTabs = editTabs.editTabs.tabList.slice(index + 1)
		handleCloseTabs(rightTabs, () => {
			editTabs.closeRight(item, index)
		})
	}

	// 关闭未修改的tab（直接过滤，无确认）
	const closeUneditedTabs = (index: number) => {
		editTabs.closeUnedited(index)
	}

	// 关闭全部tab
	const closeAllTabs = () => {
		handleCloseTabs(editTabs.editTabs.tabList, () => {
			editTabs.closeAll()
		})
	}

	// 关闭抽屉方法。确认保存修改；done 由容器注入（改写容器持有的 visible ref）
	const handleClose = (done: () => void) => {
		const dirtyTabs: Tree[] = []
		// 递归遍历treeList，找出isEdited为true的节点
		const findAndClose = (nodes: Tree[]) => {
			for (const node of nodes) {
				if (node.isEdited) {
					dirtyTabs.push(node)
				}
				if (node.children) {
					findAndClose(node.children)
				}
			}
		}
		findAndClose(treeData.treeData.treeList)
		if (dirtyTabs.length === 0) {
			done()
			return
		}
		const fileNameJoin = joinFileName(dirtyTabs)
		const message = `${fileNameJoin}已修改未保存，是否保存后再关闭？`
		ElMessageBox.confirm(message, '提示', {
			distinguishCancelAndClose: true,
			confirmButtonText: '是(Y)',
			cancelButtonText: '否(N)',
			type: 'warning'
		})
			.then(() => {
				// 保存修改后关闭
				const dataFormList = dirtyTabs.map(tab => {
					return {
						id: tab.id,
						templateContent: tab.templateContent
					}
				})
				persistence.batchSave(dataFormList).then(() => {
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
					// 抽屉关闭后重开会 init 重拉整树，脏自然被抹掉，此处仅同步提示“未保存”
					ElMessage.info(`${fileNameJoin}已关闭，修改未保存`)
				}
			})
	}

	return {
		state,
		submitLoading: persistence.submitLoading,
		activeTabItem,
		fullFilePath,
		canInsertVariable,
		getIcon,
		getElementFromTabList: editTabs.getElementFromTabList,
		tabPush: editTabs.tabPush,
		refreshTabData: editTabs.refreshTabData,
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
