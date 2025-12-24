import { ref } from 'vue'
import { FormInitOptions, Key } from '@/types'
import { isObject } from '@/utils/tool'

// 表单初始化
export const useInitForm = (ctxGetter?: () => Record<string, any>) => {
	// 表单组件引用
	const formRef = ref()

	// 参数归一化
	const normalizeInitOptions = (arg?: Key | FormInitOptions): FormInitOptions => {
		const initOptions: FormInitOptions = { type: 'add', ctx: ctxGetter?.() }
		// 1️⃣ 没传参数 → 新增
		if (arg === undefined) {
			initOptions.type = 'add'
		} else if (isObject(arg)) {
			// 2️⃣ 传的是对象 → 认为是 FormInitOptions
			const tempArg = arg as FormInitOptions
			if (!tempArg.type) {
				throw new Error(`参数错误，type为空: ${arg}}`)
			}
			Object.assign(initOptions, arg)
		} else if (arg) {
			// 3️⃣ 传的是 key → 默认修改
			initOptions.type = 'update'
			initOptions.id = arg
		} else {
			throw new Error(`参数错误: ${arg}}`)
		}
		return initOptions
	}

	// 初始化表单方法
	const formInitHandle = (arg?: Key | FormInitOptions) => {
		const initOptions = normalizeInitOptions(arg)
		formRef.value.init(initOptions)
	}

	return {
		formRef,
		formInitHandle
	}
}
