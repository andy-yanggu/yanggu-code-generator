import { ElMessage } from 'element-plus'
import { FormInitOptions, FormOptions, FormType, Key } from '@/types'
import { defaultsDeep, isNotBlank } from '@/utils/tool'

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
	const defaultFormOptions = (): FormOptions<VO> => ({
		submitApi: (_: VO) => Promise.resolve(),
		detailApi: (_: Key) => Promise.resolve({} as VO),
		initFormData: () => ({}) as VO,
		titleSubject: '',
		fullTitle: '',
		dataForm: {} as VO & { id?: Key },
		duration: 1500
	})

	// 合并默认值：用默认值填充 options 中缺失的属性，不覆盖已有属性
	defaultsDeep(options, defaultFormOptions())

	const titleMap: Record<string, string> = {
		add: '新增',
		update: '修改',
		copy: '复制',
		detail: '详情',
		operate: '操作'
	}

	// 对话框标题
	const dialogTitle = () => {
		// ✅ 1. 特殊场景，完全自定义
		if (isNotBlank(options.fullTitle)) {
			return options.fullTitle
		}
		// ✅ 2. 通用规则：动作 + 名词
		const action = titleMap[formType.value] ?? '操作'

		if (options.titleSubject) {
			// 特例：详情更符合中文习惯
			if (formType.value === 'detail') {
				return `${options.titleSubject}详情`
			}
			return `${action}${options.titleSubject}`
		}

		// ✅ 3. 兜底
		return action
	}

	// 获取提示消息
	const getMessage = () => {
		return options.message || `${options.titleSubject}${titleMap[formType.value]}成功`
	}

	// 初始化表单
	const init = (initOptions: FormInitOptions) => {
		const { type, id, ctx = {} } = initOptions

		formType.value = type
		visible.value = true

		nextTick(() => {
			// 重置表单数据
			Object.assign(options.dataForm, options.initFormData(ctx))
			options.dataForm.id = id

			// 重置表单验证
			dataFormRef.value.clearValidate()

			// 初始化之前调用
			options.initBefore?.()

			if (id) {
				// 调用详情接口
				options.detailApi(id).then(data => {
					options.dataAssignBefore?.(data)
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

			options
				// 提交表单
				.submitApi(options.dataForm)
				.then(data => {
					ElMessage.success({
						// 提示消息
						message: getMessage(),
						// 提示时长
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
