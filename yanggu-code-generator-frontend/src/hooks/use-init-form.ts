import { FormInitOptions, Key } from '@/types'
import { defaultsDeep, isNotBlank, isObject } from '@/utils/tool'

// 表单初始化
export const useInitForm = (ctxGetter?: () => Record<string, any>) => {
	// 表单组件引用
	const formRef = ref()

	// 参数归一化
	const normalizeInitOptions = (arg?: Key | FormInitOptions): FormInitOptions => {
		// 1️. 没传参数 → 新增
		if (arg === undefined) {
			return { type: 'add', ctx: ctxGetter?.() }
		}

		// 2️. 传的是对象 → 认为是 FormInitOptions
		if (isObject(arg)) {
			const formOptions = arg as FormInitOptions
			if (!formOptions.type) {
				throw new Error(`参数错误，type为空: ${JSON.stringify(arg)}`)
			}
			return defaultsDeep(formOptions, { type: 'add', ctx: ctxGetter?.() })
		}

		// 3️. 传的是 key → 默认修改
		if (isNotBlank(arg)) {
			return { type: 'update', id: arg, ctx: ctxGetter?.() }
		}

		// 4️. 其他情况 → 抛出错误
		throw new Error(`参数错误: ${arg}`)
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
