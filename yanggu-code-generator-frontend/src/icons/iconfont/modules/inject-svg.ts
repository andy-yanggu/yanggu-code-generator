/**
 * SVG 图标注入工具
 *
 * 将 SVG sprite 字符串注入到 DOM 中。
 *
 * 特点：
 * 1. 支持 SSR 环境
 * 2. 同一个 moduleName 只保留一个 SVG sprite
 * 3. 支持 Vite HMR 重复执行时自动替换旧 SVG
 */

export const injectSvg = (svgContent: string, moduleName?: string) => {
	// SSR 环境没有 document
	if (typeof document === 'undefined') {
		return
	}

	/**
	 * 每个图标模块对应一个独立的 SVG 容器。
	 *
	 * 例如：
	 *
	 * arrows
	 *   ↓
	 * svg-sprite-arrows
	 */
	const containerId = moduleName ? `svg-sprite-${moduleName}` : null

	/**
	 * HMR 时模块会重新执行。
	 *
	 * 先删除旧的 SVG sprite，
	 * 再插入新的 SVG sprite。
	 */
	if (containerId) {
		const oldSvg = document.getElementById(containerId)

		if (oldSvg) {
			oldSvg.remove()
		}
	}

	/**
	 * 通过临时容器解析 SVG 字符串。
	 */
	const container = document.createElement('div')

	container.innerHTML = svgContent

	const svg = container.querySelector('svg')

	if (!svg) {
		console.warn(`[SVG] 未找到 SVG 根节点: ${moduleName ?? 'unknown'}`)
		return
	}

	/**
	 * SVG sprite 本身不应该参与页面布局。
	 */
	svg.setAttribute('aria-hidden', 'true')

	Object.assign(svg.style, {
		position: 'absolute',
		width: '0',
		height: '0',
		overflow: 'hidden'
	})

	/**
	 * 给当前模块的 sprite 设置唯一 ID。
	 */
	if (containerId) {
		svg.id = containerId
	}

	/**
	 * 插入到 body 最前面。
	 *
	 * 这样可以保证 SVG sprite 不影响正常页面布局。
	 */
	if (document.body.firstChild) {
		document.body.insertBefore(svg, document.body.firstChild)
	} else {
		document.body.appendChild(svg)
	}

	console.log(`[SVG] sprite injected: ${moduleName ?? 'anonymous'}`)
}
