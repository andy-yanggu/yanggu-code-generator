import { Ref, ref } from 'vue'
import { ElMessage } from 'element-plus'

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

export const useSubmitHandler = (options: SubmitOptions) => {
	const submitLoading = ref(false)

	const defaultOptions: SubmitOptions = {
		message: '提交成功',
		duration: 500
	}

	// 合并默认值
	Object.assign(defaultOptions, Object.fromEntries(Object.entries(defaultOptions).filter(([key]) => !Object.hasOwn(options, key))))

	// 提交方法
	const submitHandle = async (dataForm?: any) => {
		if (submitLoading.value) {
			ElMessage.warning('请勿重复提交')
			return
		}

		if (!options.submitApi) {
			ElMessage.warning('未配置提交接口')
			return
		}

		submitLoading.value = true
		try {
			const res = await options.submitApi(dataForm)

			ElMessage.success({
				message: options.message,
				duration: options.duration,
				onClose: () => {
					// 关闭弹窗
					if (options.visible) {
						options.visible.value = false
					}
					// 执行成功回调函数
					if (options.onSuccess) {
						options.onSuccess?.(res)
					} else {
						// 默认刷新数据列表
						options.emit?.('refreshDataList', res)
					}
				}
			})
		} catch (err) {
			// 用户自定义错误处理
			if (options.onError) {
				options.onError?.(err)
			} else {
				// 默认错误处理
				ElMessage.error({
					message: '提交失败，请稍后重试',
					duration: 1000
				})
			}
		} finally {
			submitLoading.value = false
		}
	}

	return {
		submitLoading,
		submitHandle
	}
}
