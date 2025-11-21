import { nextTick, ref } from 'vue'
import { FormType, Key } from '@/types'

// 简单表单
export const useInitForm = () => {
	// 表单组件引用
	const formRef = ref()

	// 初始化表单方法
	const formInitHandle = (id?: Key) => {
		formRef.value.init(id)
	}

	return {
		formRef,
		formInitHandle
	}
}

// 复杂表单（新增、修改、复制）
export const useComplexForm = () => {
	// 表单组件引用
	const formRef = ref()

	// 表单模式
	const dialogMode = ref<FormType>('add')

	// 表单初始化函数
	const formInitHandle = (formType: FormType, id?: Key) => {
		// 设置模式
		dialogMode.value = formType
		// 调用原始函数
		nextTick(() => {
			formRef.value.init(id)
		})
	}
	return {
		formRef,
		dialogMode,
		formInitHandle
	}
}
