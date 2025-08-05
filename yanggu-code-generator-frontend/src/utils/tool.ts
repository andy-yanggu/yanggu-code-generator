import type { App, Component, Plugin } from 'vue' // 添加 Component 类型
import { useClipboard } from '@vueuse/core'
import { ElMessage } from 'element-plus/es'

const { copy, isSupported } = useClipboard()

// 复制到剪切板
export const copyToClipboard = (text: string): Promise<void> => {
	return new Promise((resolve, reject) => {
		if (isSupported) {
			copy(text)
				.then(() => {
					resolve()
				})
				.catch(err => {
					console.error('复制失败:', err)
					ElMessage.error('复制失败')
					reject()
				})
		} else {
			// 不支持时的降级处理
			console.warn('当前浏览器不支持 Clipboard API')
			ElMessage.warning('当前浏览器不支持自动复制功能')
			reject()
		}
	})
}

// 修改泛型约束为 Component
export const withInstall = <T extends Component>(component: T, alias?: string) => {
	const comp = component as any
	comp.install = (app: App) => {
		app.component(comp.__name || comp.displayName, component)
		if (alias) {
			app.config.globalProperties[alias] = component
		}
	}
	return component as T & Plugin
}
