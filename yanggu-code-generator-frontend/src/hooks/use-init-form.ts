import { ref } from 'vue'
import { FormType, Key } from '@/types'

// 表单初始化
export const useInitForm = () => {
	// 表单组件引用
	const formRef = ref()

	// 初始化表单方法
	const formInitHandle = (arg1?: FormType | Key, arg2?: Key) => {
		formRef.value.init(arg1, arg2)
	}

	return {
		formRef,
		formInitHandle
	}
}
