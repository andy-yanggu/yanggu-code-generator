import { reactive, type Ref } from 'vue'
import { genTemplateApi } from '@/api'
import { buildFileList, buildTree, findByIdFromTree, getFullPathById, type Tree } from './tree-utils'

/**
 * 数据域：treeList 的数据主权 + 树读接口装载（use-template-tree-state 四域拆分之一）。
 *
 * 边界（见 2026-11-24 状态域拆分设计 决策 1/2）：
 * - 只允许出现树的读接口（treeData），不认识 tab / 弹窗 / 写接口；
 * - 装载产物（文件列表）返回给上层，由编排层接线到会话域 refreshTabData，本域不反向依赖 tab；
 * - 接线需要即时生效时走本域的 onLoaded 同步回调（只认泛化函数，不认 tab），原因见 fetchTreeData 注释。
 */

/** 数据域切片工厂：初值与复位共用同一口径（per-slice 工厂，决策 6） */
const createTreeDataSlice = () => ({
	// 树形节点数据
	treeList: [] as Tree[]
})

export const useTemplateTreeData = (templateGroupId: Ref<number>) => {
	const treeData = reactive(createTreeDataSlice())

	// 拉树数据并 buildTree，返回文件列表（模板文件+二进制文件）；不触碰 tab、不做 DOM 编排。
	// onLoaded 是跨域接线的原子性钩子，必须在「换树 → 同步 tab」的同一同步段内调完，
	// 不能留给调用方 await 之后再调：activeTab 走 treeList 反查，treeList 一换，会话域的
	// content-watch 就会抢在 tab 重指向之前 flush，把旧 tab 项的 isEdited 抹掉，
	// refreshTabData 随之收集不到脏项，未保存内容被库内原文覆盖（见 2026-11-25 评审 B1）
	const fetchTreeData = async (onLoaded?: (files: Tree[]) => void): Promise<Tree[]> => {
		const data = await genTemplateApi.treeData(templateGroupId.value)
		treeData.treeList = data
		buildTree(treeData.treeList)
		const files = buildFileList(data)
		onLoaded?.(files)
		return files
	}

	// 节点反查/路径能力：显式注入给会话域与编排层，避免外部直接摸 treeList
	const findById = (id: number): Tree | undefined => findByIdFromTree(id, treeData.treeList)

	const getFullPath = (id: number): string => getFullPathById(id, treeData.treeList)

	// 本域复位：整表覆盖回工厂产物（引用替换，deep reactive 照常追踪）
	const reset = () => {
		Object.assign(treeData, createTreeDataSlice())
	}

	return { treeData, fetchTreeData, findById, getFullPath, reset }
}

export type TemplateTreeDataDomain = ReturnType<typeof useTemplateTreeData>
