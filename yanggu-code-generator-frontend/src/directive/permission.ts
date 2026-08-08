import { type Directive } from 'vue'
import { useUserStore } from '@/store'

// 检查是否拥有权限
const checkPermission = (value: string | string[]): boolean => {
	const userStore = useUserStore()

	if (!value) {
		throw new Error('请传入权限标识')
	}

	if (typeof value === 'string') {
		return userStore.permissionList.includes(value)
	}

	if (Array.isArray(value)) {
		// 拥有数组中的任意一个权限即可
		return value.some(item => userStore.permissionList.includes(item))
	}

	throw new Error('权限标识格式不正确')
}

// 权限指令：无权限时移除 DOM 元素
export const hasPermission: Directive = {
	mounted(el: Element, binding) {
		if (!checkPermission(binding.value)) {
			el.parentNode?.removeChild(el)
		}
	},
	updated(el: Element, binding) {
		if (!checkPermission(binding.value)) {
			el.parentNode?.removeChild(el)
		}
	}
}
