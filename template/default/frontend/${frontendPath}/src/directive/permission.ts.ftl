import { DirectiveBinding } from 'vue'
import { useUserStore } from '@/store/user-store'

export function hasPermission(el: Element, binding: DirectiveBinding) {
	const userStore = useUserStore()
	const { value } = binding

	if (!value) {
		throw new Error('请传入权限标识')
	}
	if (typeof value == 'string') {
		//没有权限就移除当前元素
		if (!userStore.permissionList.includes(value)) {
			el.parentNode?.removeChild(el)
		}
		return
	}
	if (Array.isArray(value)) {
		//用户权限是否拥有数组中的任意一个权限
		const hasPerm = value.some(item => userStore.permissionList.includes(item))
		if (!hasPerm) {
			el.parentNode?.removeChild(el)
		}
		return
	}

	throw new Error('权限标识格式不正确')
}
