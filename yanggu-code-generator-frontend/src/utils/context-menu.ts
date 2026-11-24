/**
 * 右键菜单定位纯函数（flip 优先 + clamp 兜底）。
 * 见设计文档 2026-11-16-context-menu-abstraction-design.md「四角 / 四边边界」。
 */

export interface Size {
	width: number
	height: number
}

export interface Point {
	x: number
	y: number
}

export interface Rect {
	left: number
	top: number
}

const clamp = (value: number, min: number, max: number): number => Math.min(Math.max(value, min), max)

/**
 * 依据光标、菜单实测尺寸与视口尺寸计算菜单左上角坐标。
 *
 * - 水平：默认贴光标朝右展开；右溢出则整块翻到光标左侧；仍溢出（菜单比视口宽）则夹到 [margin, vw-w-margin]。
 * - 垂直：默认贴光标朝下展开；下溢出则整块翻到光标上方；仍溢出则夹到 [margin, vh-h-margin]。
 * - `Math.max(margin, ...)` 保证「菜单尺寸 > 视口」时上界不小于下界，clamp 结果恒 ≥ margin，不产生负坐标。
 *
 * @param margin 距视口边缘的安全间距，默认 8
 */
export const computeMenuPosition = (cursor: Point, menu: Size, viewport: Size, margin = 8): Rect => {
	let left = cursor.x
	if (left + menu.width > viewport.width - margin) {
		left = cursor.x - menu.width
	}
	left = clamp(left, margin, Math.max(margin, viewport.width - menu.width - margin))

	let top = cursor.y
	if (top + menu.height > viewport.height - margin) {
		top = cursor.y - menu.height
	}
	top = clamp(top, margin, Math.max(margin, viewport.height - menu.height - margin))

	return { left, top }
}
