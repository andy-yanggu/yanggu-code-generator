import { resolve } from 'path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// 参考：https://cn.vitejs.dev/config/
export default defineConfig({
	base: './',
	resolve: {
		// 配置别名
		alias: {
			'@': resolve(__dirname, './src'),
			'vue-i18n': 'vue-i18n/dist/vue-i18n.cjs.js'
		}
	},
	plugins: [
		vue(),
		createSvgIconsPlugin({
			iconDirs: [resolve(__dirname, 'src/icons/svg')],
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
					'element-plus/es': ['ElMessage', 'ElMessageBox', 'ElNotification', 'ElMessageBox', 'ElLoading']
				}
			]
		})
	],
	server: {
		host: '0.0.0.0',
		port: 5000, // 端口号
		open: false // 是否自动打开浏览器
	}
})
