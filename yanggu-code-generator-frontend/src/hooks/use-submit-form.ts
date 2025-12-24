import { nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { FormInitOptions, FormOptions, FormType, Key } from '@/types'

// 提交表单
export const useSubmitForm = <VO extends { id?: Key }>(options: FormOptions<VO>) => {
	// 弹窗可见性
	const visible = ref(false)

	// 提交按钮loading状态
	const submitLoading = ref(false)

	// 表单ref
	const dataFormRef = ref()

	// 当前表单类型
	const formType = ref<FormType>('add')

	// 默认值
	const defaultFormOptions: FormOptions<VO> = {
		submitApi: (_: VO) => Promise.resolve(),
		detailApi: (_: Key) => Promise.resolve({} as VO),
		initFormData: () => ({}) as VO,
		dataForm: {} as VO & { id?: Key },
		duration: 500
	} as const

	// 合并默认值
	Object.assign(options, Object.fromEntries(Object.entries(defaultFormOptions).filter(([key]) => !Object.hasOwn(options, key))))

	// 表单字段进行初始化
	if (!options.dataForm) {
		Object.assign(options.dataForm, options.initFormData())
	}

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

	// 获取提示消息
	const getMessage = () => {
		switch (formType.value) {
			case 'add':
				return '新增成功'
			case 'update':
				return '修改成功'
			case 'copy':
				return '复制成功'
			default:
				return '操作成功'
		}
	}

	// 初始化表单
	const init = (initOptions: FormInitOptions) => {
		const { type, id, ctx = {} } = initOptions

		formType.value = type
		visible.value = true

		formType.value = type
		visible.value = true
		options.dataForm.id = id

		nextTick(() => {
			// 清空表单校验和重置表单数据
			dataFormRef.value.clearValidate()
			dataFormRef.value.resetFields()
			Object.assign(options.dataForm, options.initFormData(ctx))

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
			const message = options.message || getMessage()
			options
				// 提交表单
				.submitApi(options.dataForm)
				.then(data => {
					ElMessage.success({
						message,
						duration: options.duration
					})
					visible.value = false
					if (options.submitAfter) {
						// 提交成功后调用
						options.submitAfter(data ?? options.dataForm)
					} else {
						// 默认为刷新列表
						options.emit?.('refreshDataList', data ?? options.dataForm)
					}
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
