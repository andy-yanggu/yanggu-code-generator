import { describe, expect, it } from 'vitest'
import { computeMenuPosition } from './context-menu'

const viewport = { width: 1000, height: 800 }
const menu = { width: 80, height: 200 }

describe('computeMenuPosition', () => {
	it('空间充足时贴光标朝右下展开', () => {
		expect(computeMenuPosition({ x: 100, y: 100 }, menu, viewport)).toEqual({ left: 100, top: 100 })
	})

	it('右溢出整块水平翻到光标左侧', () => {
		const r = computeMenuPosition({ x: 950, y: 100 }, menu, viewport)
		expect(r.left).toBe(870)
		expect(r.top).toBe(100)
	})

	it('下溢出整块垂直翻到光标上方', () => {
		const r = computeMenuPosition({ x: 100, y: 750 }, menu, viewport)
		expect(r.left).toBe(100)
		expect(r.top).toBe(550)
	})

	it('右下角水平与垂直均翻转', () => {
		expect(computeMenuPosition({ x: 950, y: 750 }, menu, viewport)).toEqual({ left: 870, top: 550 })
	})

	it('菜单比视口宽时夹到 margin，不产生负坐标', () => {
		const r = computeMenuPosition({ x: 500, y: 100 }, { width: 1200, height: 200 }, viewport)
		expect(r.left).toBe(8)
		expect(r.left).toBeGreaterThanOrEqual(0)
	})

	it('菜单比视口高时垂直夹到 margin，不产生负坐标', () => {
		const r = computeMenuPosition({ x: 100, y: 500 }, { width: 80, height: 1200 }, viewport)
		expect(r.top).toBe(8)
		expect(r.top).toBeGreaterThanOrEqual(0)
	})

	it('自定义 margin 贴边时强制内缩', () => {
		const r = computeMenuPosition({ x: 0, y: 0 }, menu, viewport, 20)
		expect(r.left).toBe(20)
		expect(r.top).toBe(20)
	})
})
