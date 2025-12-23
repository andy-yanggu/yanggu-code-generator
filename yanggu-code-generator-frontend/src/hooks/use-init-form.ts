import { ref } from 'vue'
import { FormInitOptions } from '@/types'

// 表单初始化
export const useInitForm = (ctxGetter?: () => Record<string, any>) => {
	// 表单组件引用
	const formRef = ref()

	// 初始化表单方法
	const formInitHandle = (initOptions: FormInitOptions) => {
		const resolvedCtx = initOptions.ctx ?? ctxGetter?.()

		formRef.value.init({
			...initOptions,
			ctx: resolvedCtx
		})
	}

	return {
		formRef,
		formInitHandle
	}
}
