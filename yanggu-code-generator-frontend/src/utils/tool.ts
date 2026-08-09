import { App, Component, Plugin } from 'vue'
import { isEmpty, isNil, isString } from 'lodash-es'
import { Scalar } from '@/types'
import { env } from '@/config'

// 复制到剪切板
export const copyToClipboard = (text: string): Promise<void> => {
	const { copy, isSupported } = useClipboard()
	return new Promise((resolve, reject) => {
		if (isSupported) {
			copy(text)
				.then(() => {
					resolve()
				})
				.catch(err => {
					console.error('复制失败:', err)
					ElMessage.error('复制失败')
					reject(new Error('复制失败'))
				})
		} else {
			// ⚙️ 降级处理：使用 document.execCommand('copy')
			try {
				// 创建隐藏的 textarea
				const textarea = document.createElement('textarea')
				textarea.value = text
				textarea.style.position = 'fixed'
				textarea.style.top = '-9999px'
				textarea.style.left = '-9999px'
				document.body.appendChild(textarea)

				// 选中并执行复制命令
				textarea.select()
				const successful = document.execCommand('copy')
				document.body.removeChild(textarea)

				if (successful) {
					resolve()
				} else {
					ElMessage.warning('浏览器不支持自动复制，请手动复制')
					reject(new Error('execCommand copy failed'))
				}
			} catch (err) {
				console.error('复制失败（降级模式）:', err)
				ElMessage.warning('浏览器不支持自动复制，请手动复制')
				reject(err)
			}
		}
	})
}

// 全局标题（懒初始化，避免模块顶层调用 composable）
const useTitleState = createGlobalState(() => useTitle())
let titleRef: ReturnType<typeof useTitle> | null = null

const getTitleRef = () => {
	if (!titleRef) {
		titleRef = useTitleState()
	}
	return titleRef
}

// 默认标题
const originalTitle = env.appTitle

// 设置标题
export const setTitle = (newTitle: string) => {
	getTitleRef().value = newTitle + ' - ' + originalTitle
}

// 设置原始标题
export const setDefaultTitle = () => {
	getTitleRef().value = originalTitle
}

// 导出相关函数
export { isEmpty, isObject, defaultsDeep } from 'lodash-es'

/**
 * 仅适用于 Array / Object / Map / Set
 * ❌ 不可用于 number / boolean / Blob
 */
export const isNotEmpty = <T>(value: T | null | undefined | [] | ''): value is T => {
	return !isEmpty(value)
}

/**
 * isBlank，针对标量场景
 * number / boolean / Blob
 */
export const isBlank = (value: Scalar): boolean => {
	if (isNil(value)) {
		return true
	}
	if (isString(value)) {
		return value.trim() === ''
	}
	return false
}

// isNotBlank，针对标量场景
export const isNotBlank = (value: Scalar): boolean => {
	return !isBlank(value)
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
