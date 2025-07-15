import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { AxiosPromise } from 'axios'

export interface FormOptions {
	// 提交API
	submitApi: (data: any) => AxiosPromise
	// 详情API
	detailApi: (id: number) => AxiosPromise
	// 初始化之前调用
	initBefore?: () => void
	// 初始表单数据
	initFormData: any
	// 初始化之后调用
	initAfter?: () => void
	// 触发事件
	emit: any
	// 提示信息
	message?: string
	// 提示时长
	duration?: number
}

export const useSubmitForm = (options: FormOptions) => {
	const { submitApi, detailApi, initBefore = () => {}, initFormData, initAfter = () => {}, emit, message = '操作成功', duration = 500 } = options

	const visible = ref(false) // 弹窗可见性
	const dataForm = reactive({ ...initFormData }) // 表单数据
	const dataFormRef = ref() // 表单ref

	// 初始化表单
	const init = (id?: number) => {
		visible.value = true
		dataForm.id = null
		// 重置表单状态
		if (dataFormRef.value) {
			dataFormRef.value.resetFields()
		}

		// 初始化之前调用
		initBefore()

		if (!id) {
			return
		}

		// 获取详情数据
		detailApi(id).then(res => {
			Object.assign(dataForm, res.data)

			// 获取详情之后调用
			initAfter()
		})
	}

	// 表单验证并提交
	const submitHandle = () => {
		dataFormRef.value.validate((valid: boolean) => {
			if (!valid) {
				return false
			}

			submitApi(dataForm).then(() => {
				ElMessage.success({
					message: message,
					duration: duration,
					onClose: () => {
						visible.value = false
						emit('refreshDataList')
					}
				})
			})
		})
	}

	return {
		visible,
		dataForm,
		dataFormRef,
		init,
		submitHandle
	}
}
