import { computed, ref, watch, type Ref } from 'vue'
import { useElementSize } from '@vueuse/core'

/**
 * 变量面板宽度 px 体系（数值沿用拆分前的单体 tree.vue，该文件已删除，边界口径保持现状）。
 * 见设计文档 2026-11-24 F25；v2 容器与 test.vue（T4）共同消费本 composable。
 *
 * 面板宽度用 px 自持（拖拽时 EP 会回写 px）；两块 panel 的尺寸一律按实测容器宽度算出 px 后断言，
 * 且编辑区也带 min，避免 EP 归一化分支在容器宽度变化时触发等比缩放（评审 I2）。
 */

// 面板区间 [240, 560]；编辑区下限 320；首次展开取容器 28%
const PANEL_MIN = 240
const PANEL_MAX = 560
const EDITOR_FLOOR = 320
const FIRST_EXPAND_RATIO = 0.28
const PANEL_DEFAULT_SIZE = 320

/** 编辑区下限：容器窄到放不下「320 + 面板下界 240」时退让，优先保住面板的 240（复审 I2 ②）；未测得宽度时回退 320 */
export const computeEditorFloor = (width: number): number => {
	if (!width) {
		return EDITOR_FLOOR
	}
	return Math.max(0, Math.min(EDITOR_FLOOR, width - PANEL_MIN))
}

/** 面板夹紧区间 [240, min(560, 容器 - 编辑区下限)]：与两块 panel 上声明的 min/max 完全等价，使 EP 拖拽 clamp 结果等于自算结果 */
export const computeClampedPanelSize = (size: number, width: number, editorFloor: number): number => {
	if (!width) {
		return size
	}
	const upper = Math.max(PANEL_MIN, Math.min(PANEL_MAX, width - editorFloor))
	return Math.min(Math.max(size, PANEL_MIN), upper)
}

/** 收起时第二块已卸载，第一块必须铺满整个容器；展开时直接取容器减去面板，两块之和恒等于容器，不留归一化余量 */
export const computeEditorPanelSize = (width: number, visible: boolean, clampedSize: number): number | undefined => {
	if (!width) {
		return undefined
	}
	return visible ? Math.max(0, width - clampedSize) : width
}

/** 首次展开按规格取容器 28% */
export const computeInitialPanelSize = (width: number): number => Math.round(width * FIRST_EXPAND_RATIO)

/**
 * 持有变量面板的宽度状态与派生尺寸。
 *
 * @param containerRef 编辑区分栏容器（el-splitter）实例 ref，内部用 useElementSize 实测宽度
 * @param visible 变量面板是否展开
 */
export const useVariablePanelSize = (containerRef: Ref<any>, visible: Ref<boolean>) => {
	const { width } = useElementSize(containerRef)
	const panelSize = ref(PANEL_DEFAULT_SIZE)
	let panelSizeInitialized = false

	const editorFloor = computed(() => computeEditorFloor(width.value))
	const clampedSize = computed(() => computeClampedPanelSize(panelSize.value, width.value, editorFloor.value))
	const editorPanelSize = computed(() => computeEditorPanelSize(width.value, visible.value, clampedSize.value))

	// 首次展开按容器 28% 初始化，之后保留用户拖拽宽度
	watch(visible, next => {
		if (next && !panelSizeInitialized && width.value) {
			panelSizeInitialized = true
			panelSize.value = computeInitialPanelSize(width.value)
		}
	})

	// 抽屉重复打开时复位「首次展开」标记（tree-v2.vue 与 test.vue 的 init 均调用）
	const resetInit = (): void => {
		panelSizeInitialized = false
	}

	return { panelSize, editorFloor, clampedSize, editorPanelSize, resetInit }
}
