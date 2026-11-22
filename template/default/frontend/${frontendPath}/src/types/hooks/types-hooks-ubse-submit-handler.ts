import { Ref } from 'vue'

export interface SubmitOptions {
	// 控制 dialog 显示
	visible?: Ref<boolean, boolean>
	// 提交方法
	submitApi?: (dataForm: any) => Promise<any>
	// 触发事件
	emit?: any
	// 成功提示
	successMessage?: string
	// 成功提示时长
	successDuration?: number
	// 错误提示
	errorMessage?: string
	// 错误提示时长
	errorDuration?: number
	// 成功回调
	onSuccess?: (res: any) => void
	// 错误回调
	onError?: (err: any) => void
}
