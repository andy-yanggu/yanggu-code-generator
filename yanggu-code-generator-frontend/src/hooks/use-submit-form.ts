import { nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { AxiosPromise } from 'axios'

// 表单模式：新增、修改、复制
export type FormType = 'add' | 'update' | 'copy'

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
	// 弹窗可见性
	const visible = ref(false)

	// 提交按钮loading状态
	const submitLoading = ref(false)

	// 表单ref
	const dataFormRef = ref()

	// 对话框标题
	const dialogTitle = (formType: FormType) => {
		switch (formType) {
			case 'add':
				return '新增'
			case 'update':
				return '修改'
			case 'copy':
				return '复制'
			default:
				return '操作'
		}
	}

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
		dialogTitle,
		init,
		submitHandle,
		submitLoading
	}
}
