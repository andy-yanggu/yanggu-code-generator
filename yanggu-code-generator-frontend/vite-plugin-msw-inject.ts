import type { Plugin } from 'vite'

/**
 * MSW 零侵入注入插件
 *
 * 原理：在 Vite 加载 main.ts 时，通过 transform 钩子自动注入 MSW 启动代码。
 * ESM 规范保证 import 声明先被提升执行，因此 await startMock() 不会阻塞依赖加载，
 * 只会阻塞 createApp/mount，确保 MSW 在 app 发出第一个请求前就绪。
 *
 * 启用条件：开发环境 + VITE_ENABLE_MOCK=true
 */
export function mswInjectPlugin(): Plugin {
	let enableMock = false

	return {
		name: 'yanggu-msw-inject',

		configResolved(config) {
			enableMock = config.env.VITE_ENABLE_MOCK === 'true'
		},

		transform(code, id) {
			// 仅在开发环境 + mock 启用时 + main.ts 入口生效
			if (process.env.NODE_ENV === 'production' || !enableMock) return
			if (!id.endsWith('/src/main.ts')) return

			// 注入虚拟模块导入 + await 阻塞
			// ESM 规范：import 声明被提升先执行，然后才执行模块体
			// 所以 await 不会阻塞依赖加载，只会阻塞 createApp/mount
			const injection = "import __startMock from 'virtual:mock-setup'\nawait __startMock()\n"

			return {
				code: injection + code,
				map: null
			}
		},

		// 注册虚拟模块
		resolveId(id) {
			if (id === 'virtual:mock-setup') return 'virtual:mock-setup'
		},

		load(id) {
			if (id === 'virtual:mock-setup') {
				return `export default async function startMock() {
	const { worker } = await import('@/mocks/browser')
	await worker.start({
		onUnhandledRequest: 'bypass'
	})
}`
			}
		}
	}
}
