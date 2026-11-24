import { describe, expect, it } from 'vitest'
import { computeClampedPanelSize, computeEditorFloor, computeEditorPanelSize, computeInitialPanelSize } from './use-variable-panel-size'

describe('computeEditorFloor', () => {
	it('未测得宽度（0）回退 320', () => {
		expect(computeEditorFloor(0)).toBe(320)
	})

	it('常规宽度取固定下限 320', () => {
		expect(computeEditorFloor(1000)).toBe(320)
	})

	it('窄容器退让：宽度 500 时编辑区下限降到 260（优先保面板 240）', () => {
		expect(computeEditorFloor(500)).toBe(260)
	})

	it('极窄容器不为负：宽度 200 时夹到 0', () => {
		expect(computeEditorFloor(200)).toBe(0)
	})
})

describe('computeClampedPanelSize', () => {
	it('未测得宽度时原样返回', () => {
		expect(computeClampedPanelSize(320, 0, 320)).toBe(320)
	})

	it('区间内保持用户宽度', () => {
		expect(computeClampedPanelSize(320, 1000, 320)).toBe(320)
	})

	it('超过上界 560 夹到 560', () => {
		expect(computeClampedPanelSize(600, 1000, 320)).toBe(560)
	})

	it('低于下界 240 夹到 240', () => {
		expect(computeClampedPanelSize(100, 1000, 320)).toBe(240)
	})

	it('窄容器上界随编辑区退让收窄', () => {
		// width 500、editorFloor 260 → upper = max(240, min(560, 240)) = 240
		expect(computeClampedPanelSize(400, 500, 260)).toBe(240)
	})
})

describe('computeEditorPanelSize', () => {
	it('未测得宽度返回 undefined', () => {
		expect(computeEditorPanelSize(0, true, 320)).toBeUndefined()
	})

	it('收起时铺满整个容器', () => {
		expect(computeEditorPanelSize(1000, false, 320)).toBe(1000)
	})

	it('展开时取容器减去面板', () => {
		expect(computeEditorPanelSize(1000, true, 320)).toBe(680)
	})
})

describe('computeInitialPanelSize', () => {
	it('首次展开取容器 28% 并四舍五入', () => {
		expect(computeInitialPanelSize(1000)).toBe(280)
		expect(computeInitialPanelSize(900)).toBe(252)
	})
})
