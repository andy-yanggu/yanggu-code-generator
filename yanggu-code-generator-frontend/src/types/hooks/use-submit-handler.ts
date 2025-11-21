import { Ref } from 'vue'

export interface SubmitOptions {
	// 控制 dialog 显示
	visible?: Ref<boolean, boolean>
	// 提交方法
	submitApi?: (dataForm: any) => Promise<any>
	// 触发事件
	emit?: any
	// 成功提示
	message?: string
	// 提示时长
	duration?: number
	// 成功回调
	onSuccess?: (res: any) => void
	// 错误回调
	onError?: (err: any) => void
}
