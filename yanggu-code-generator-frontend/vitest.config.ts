import { resolve } from 'path'
import { defineConfig } from 'vitest/config'

// 单元测试配置：与 vite.config.ts 分离，避免把 msw 注入、组件自动导入等构建期插件拖进测试运行器。
// 当前用例为纯函数（node 环境足够）；后续若要测 .vue 组件，可在对应测试文件顶部加
// `// @vitest-environment jsdom`，或在此设 test.environment 为 'jsdom' 并按需引入 @vitejs/plugin-vue。
export default defineConfig({
	resolve: {
		alias: {
			'@': resolve(import.meta.dirname, './src')
		}
	},
	test: {
		globals: false,
		environment: 'node',
		include: ['src/**/*.{test,spec}.ts'],
		reporters: ['default']
	}
})
