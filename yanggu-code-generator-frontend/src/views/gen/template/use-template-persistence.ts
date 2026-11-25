import { ref } from 'vue'
import { genTemplateApi } from '@/api'
import { isEmpty } from '@/utils/tool'
import type { Tree } from './tree-utils'

/**
 * 持久化域：模板内容写接口的唯一收口 + 测试回刷链路 + submitLoading
 * （use-template-tree-state 四域拆分之一）。
 *
 * 边界（见 2026-11-24 状态域拆分设计 决策 2）：
 * - 全部写接口（updateContent / updateContentList / detailData 回刷）只许出现在本文件；
 * - 不出现弹窗与消息提示：成功提示由编排层在 promise 链上追加，保持本域可独立测试；
 * - 保存成功后的红点落标记经注入回调（决策 3 通道①），本域不 import 会话域的 reactive。
 */

export interface TemplatePersistenceDeps {
	// 当前激活 tab（保存 payload 数据源，编排层注入会话域 getter）
	getActiveTab: () => Tree
	// 当前激活 id（保持现版 updateContent 的入参取法）
	getActiveId: () => number
	// 保存成功后清红点 + 同步基线（会话域 applyEditRollback）
	applyEditRollback: (tabs: Tree[]) => void
	// 树节点反查（数据域 findById，测试回刷按 id 定位节点）
	findNode: (id: number) => Tree | undefined
	// 测试回刷落点：写新内容 + 同步基线 + 清红点（会话域 rollbackWithContent）
	rollbackWithContent: (tab: Tree, content: string) => void
}

export const useTemplatePersistence = (deps: TemplatePersistenceDeps) => {
	const submitLoading = ref(false)

	// 保存当前激活模板内容（现版 saveTemplateContent 的守卫与 finally 复位口径不变）；
	// 返回 promise 供编排层在链上追加成功提示，守卫未过返回 undefined
	const saveActive = () => {
		const active = deps.getActiveTab()
		if (!active.isEdited) {
			return
		}
		submitLoading.value = true
		const dataForm = {
			id: deps.getActiveId(),
			templateContent: active.templateContent
		}
		return genTemplateApi
			.updateContent(dataForm)
			.then(() => {
				// 修改编辑状态（tab 与树节点同一引用，落会话标记经回调）
				deps.applyEditRollback([active])
			})
			.finally(() => {
				submitLoading.value = false
			})
	}

	// 批量保存（updateContentList 唯一收口，供 handleCloseTabs / handleClose 确认链调用）
	const batchSave = (dataFormList: { id: number; templateContent?: string }[]): Promise<unknown> => {
		return genTemplateApi.updateContentList(dataFormList)
	}

	// 测试页回传编辑过的 id 列表 → 逐个 detailData 刷内容清红点（现版 refreshActiveItemContent 原样，
	// 反查与落标记均经注入回调，本域不直接摸 treeList / 会话域）
	const refreshAfterTest = (editTemplateIdList: number[]) => {
		if (isEmpty(editTemplateIdList)) {
			return
		}
		editTemplateIdList.forEach(tempId => {
			const queryForm = {
				id: tempId
			}
			genTemplateApi.detailData(queryForm).then(detailData => {
				const tempTree = deps.findNode(tempId)
				if (tempTree) {
					deps.rollbackWithContent(tempTree, detailData.templateContent)
				}
			})
		})
	}

	// 本域复位：额外清 submitLoading（现版 reset 不触）——抽屉关闭时若有 in-flight 保存，
	// 不清会把加载态带到下一次打开，属安全方向的对齐（已在设计文档偏差登记）
	const reset = () => {
		submitLoading.value = false
	}

	return { submitLoading, saveActive, batchSave, refreshAfterTest, reset }
}

export type TemplatePersistenceDomain = ReturnType<typeof useTemplatePersistence>
