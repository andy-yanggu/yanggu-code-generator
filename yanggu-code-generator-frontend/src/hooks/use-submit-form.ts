import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { AxiosPromise } from 'axios'

export interface FormOptions {
	// 提交API
	submitApi: (data: any) => AxiosPromise
	// 详情API
	detailApi: (id: number) => AxiosPromise
	// 初始化之前调用
	initBefore?: () => void
	// 表单数据
	dataForm: any
	// 初始化之后调用
	initAfter?: () => void
	// 提交之前操作
	submitBefore?: () => void
	// 触发事件
	emit: any
	// 提示信息
	message?: string
	// 提示时长
	duration?: number
}

export const useSubmitForm = (options: FormOptions) => {
	const visible = ref(false) // 弹窗可见性
	const submitLoading = ref(false) // 提交按钮loading状态
	const dataFormRef = ref() // 表单ref

	// 初始化表单
	const init = (id?: number) => {
		visible.value = true
		options.dataForm.id = null
		// 重置表单状态
		if (dataFormRef.value) {
			dataFormRef.value.resetFields()
		}

		// 初始化之前调用
		options.initBefore?.()

		if (!id) {
			options.initAfter?.()
			return
		}

		// 获取详情数据
		options.detailApi(id).then(res => {
			Object.assign(options.dataForm, res.data)

			// 获取详情之后调用
			options.initAfter?.()
		})
	}

	// 表单验证并提交
	const submitHandle = () => {
		dataFormRef.value.validate((valid: boolean) => {
			if (!valid) {
				return false
			}

			// 提交之前操作
			options.submitBefore?.()

			submitLoading.value = true

			options
				.submitApi(options.dataForm)
				.then(() => {
					ElMessage.success({
						message: options.message || '操作成功',
						duration: options.duration || 500
					})
					visible.value = false
					options.emit('refreshDataList')
				})
				.finally(() => {
					submitLoading.value = false
				})
		})
	}

	return {
		visible,
		dataFormRef,
		init,
		submitHandle,
		submitLoading
	}
}
