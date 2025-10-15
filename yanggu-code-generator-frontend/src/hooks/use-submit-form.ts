import { nextTick, ref } from 'vue'
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

		nextTick(() => {
			// 清空表单校验和重置表单数据
			if (dataFormRef.value) {
				dataFormRef.value.clearValidate()
				dataFormRef.value.resetFields()
			}

			// 初始化之前调用
			options.initBefore?.()

			if (id) {
				options.detailApi(id).then(data => {
					Object.assign(options.dataForm, data)
					options.initAfter?.()
				})
			} else {
				options.initAfter?.()
			}
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

			// 提示消息
			const message = options.message || (options.dataForm.id ? '修改成功' : '新增成功')
			options
				.submitApi(options.dataForm)
				.then(data => {
					ElMessage.success({
						message: message,
						duration: options.duration || 500
					})
					visible.value = false
					options.emit('refreshDataList', data)
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
