import { nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { FORM_TYPES, FormOptions, FormType, Key } from '@/types'

// 提交表单
export const useSubmitForm = <VO>(options: FormOptions<VO>) => {
	// 弹窗可见性
	const visible = ref(false)

	// 提交按钮loading状态
	const submitLoading = ref(false)

	// 表单ref
	const dataFormRef = ref()

	// 当前表单类型
	const formType = ref<FormType>('add')

	// 对话框标题
	const dialogTitle = () => {
		switch (formType.value) {
			case 'add':
				return '新增'
			case 'update':
				return '修改'
			case 'copy':
				return '复制'
			case 'detail':
				return '详情'
			default:
				return '操作'
		}
	}

	const isFormType = (value: any): value is FormType => {
		return FORM_TYPES.includes(value)
	}

	// 重载声明
	// init() // 新增
	// init(id: Key) // 修改
	// init(type: FormType) // 新增
	// init(type: FormType, id: Key) // 修改、复制、详情

	// 初始化表单
	const init = (arg1?: FormType | Key, arg2?: Key) => {
		let type: FormType = 'add'
		let id: Key | undefined = undefined

		if (arg1 !== undefined) {
			if (isFormType(arg1)) {
				// arg1 是 FormType
				type = arg1 as FormType
				if (arg2 !== undefined) {
					id = arg2
				}
			} else {
				// arg1 是 Key → 老调用方式，默认为修改
				id = arg1
				type = 'update'
			}
		}

		formType.value = type
		visible.value = true
		options.dataForm.id = id

		nextTick(() => {
			// 清空表单校验和重置表单数据
			if (dataFormRef.value) {
				dataFormRef.value.clearValidate()
				dataFormRef.value.resetFields()
			}

			// 初始化之前调用
			options.initBefore?.()

			if (id) {
				// 调用详细接口
				options.detailApi(id).then(data => {
					// 赋值给表单数据
					Object.assign(options.dataForm, data)
					// 初始化之后调用
					options.initAfter?.()
				})
			} else {
				// 初始化之后调用
				options.initAfter?.()
			}
		})
	}

	// 表单验证并提交
	const submitHandle = () => {
		if (formType.value === 'detail') {
			// 详情模式无提交按钮
			ElMessage.warning('详情模式无法提交')
			return
		}
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
				// 提交表单
				.submitApi(options.dataForm)
				.then(data => {
					ElMessage.success({
						message: message,
						duration: options.duration || 500,
						onClose: () => {
							visible.value = false
							// 触发刷新列表事件
							options.emit?.('refreshDataList', data ?? options.dataForm)
						}
					})
				})
				.finally(() => {
					submitLoading.value = false
				})
		})
	}

	return {
		visible,
		dataFormRef,
		formType,
		dialogTitle,
		init,
		submitHandle,
		submitLoading
	}
}
