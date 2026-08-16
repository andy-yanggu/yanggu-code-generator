import { resolve } from 'path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons-ng'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// 参考：https://cn.vitejs.dev/config/
export default defineConfig({
	base: './',
	resolve: {
		// 配置别名
		alias: {
			'@': resolve(import.meta.dirname, './src')
		}
	},
	plugins: [
		vue(),
		createSvgIconsPlugin({
			iconDirs: [resolve(import.meta.dirname, 'src/icons/svg')],
			symbolId: 'icon-[dir]-[name]'
		}),
		// Element Plus 组件按需引入（样式已在 main.ts 全量导入，无需按需导入）
		Components({
			resolvers: [ElementPlusResolver({ importStyle: false })]
		}),
		// Vue/VueRouter/Pinia/ElementPlus API 自动导入
		AutoImport({
			imports: [
				'vue',
				'vue-router',
				'pinia',
				'@vueuse/core',
				{
					'element-plus/es': ['ElMessage', 'ElMessageBox', 'ElNotification', 'ElLoading']
				}
			]
		})
	],
	build: {
		rolldownOptions: {
			output: {
				// 依赖与业务代码分离，大依赖独立 chunk 提升缓存命中率
				codeSplitting: {
					groups: [
						{ test: /node_modules[\\/]element-plus/, name: 'element-plus' },
						{ test: /node_modules[\\/](echarts|zrender)/, name: 'echarts' },
						{ test: /node_modules[\\/]/, name: 'vendor' }
					]
				}
			}
		}
	},
	server: {
		host: '0.0.0.0',
		port: 5000, // 端口号
		open: false // 是否自动打开浏览器
	}
})
