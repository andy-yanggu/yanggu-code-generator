import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { SubmitOptions } from '@/types'

// 提交处理
export const useSubmitHandler = (options: SubmitOptions) => {
	// 提交按钮loading状态
	const submitLoading = ref(false)

	const defaultOptions: SubmitOptions = {
		successMessage: '提交成功',
		successDuration: 500,
		errorMessage: '提交失败，请稍后重试',
		errorDuration: 1000
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
				message: options.successMessage,
				duration: options.successDuration
			})
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
		} catch (err) {
			// 用户自定义错误处理
			if (options.onError) {
				options.onError?.(err)
			} else {
				// 默认错误处理
				if (options.errorMessage) {
					ElMessage.error({
						message: options.errorMessage,
						duration: options.errorDuration
					})
				}
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
