/**
 * 图标模块统一入口
 *
 * 职责：
 *   1. 自动发现并加载所有图标模块
 *   2. 触发 injectSvg() 将 SVG 注入 DOM
 *   3. 聚合图标分类数据，供外部消费
 *   4. 处理文件变更刷新
 *
 * 文件变更由 vite.config.ts 中的 icon-module-watcher 插件监听，
 * 不依赖 Vite 的 HMR 管道。
 */

import { injectSvg } from './inject-svg'

/**
 * 从 SVG sprite 中提取所有 symbol 的 id。
 *
 * 注意：
 *
 * 不要使用：
 *
 *   /id="([^"]+)"/g
 *
 * 因为 SVG 内部未来可能出现其他 id，
 * 比如 path、clipPath 等。
 *
 * 我们真正需要的是：
 *
 *   <symbol id="xxx">
 */
const extractIcons = (svg: string) => {
	return [...svg.matchAll(/<symbol\b[^>]*\bid="([^"]+)"/g)].map(match => match[1])
}

/**
 * 自动发现所有图标模块。
 *
 * import.meta.glob 扫描当前目录下所有 .ts 文件，
 * 排除 index.ts（本文件）和 inject-svg.ts（工具模块）。
 *
 * 新增图标模块时，只需创建对应的 .ts 文件，
 * 无需修改本文件。
 */
const rawModules = import.meta.glob(['./*.ts', '!./index.ts', '!./inject-svg.ts'], { eager: true })

/**
 * 所有图标模块。
 *
 * 按每个模块导出的 sort 字段自动排序，
 * sort 值决定图标分类的展示顺序。
 */
const modules = (Object.values(rawModules) as { key: string; label: string; sort: number; svg: string }[]).sort((a, b) => a.sort - b.sort)

/** 将所有模块的 SVG 注入 DOM */
modules.forEach(m => injectSvg(m.svg, m.key))

/**
 * 所有图标分类。
 *
 * 每个模块的：
 *
 *   key
 *   label
 *
 * 来自模块自身。
 *
 * icons 则从 svg 中自动提取。
 */
export const iconCategories = modules.map(module => ({
	key: module.key,
	label: module.label,
	icons: extractIcons(module.svg)
}))

/**
 * 所有图标名称。
 *
 * 将所有分类的 icons 扁平化。
 */
export const allIconNames = iconCategories.flatMap(category => category.icons)

/**
 * 调试信息。
 *
 * 开发环境下可以很直观地看到当前到底拿到了多少图标。
 */
if (import.meta.env.DEV) {
	console.log('[icon-modules] 初始化:', {
		categoryCount: iconCategories.length,
		iconCount: allIconNames.length,
		categories: iconCategories.map(category => ({
			key: category.key,
			count: category.icons.length
		}))
	})
}
