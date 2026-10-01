import type { UseCrudFormOptions } from '@/types/hooks/use-crud-form'
import type { Key } from '@/types/common'
import type { FormMode, FormItemRule } from '@/types/schema'

export const useCrudForm = <DTO = any, VO = any>(options: UseCrudFormOptions<DTO, VO>) => {
	const subject = options.subject ?? '数据'
	const labelWidth = options.labelWidth ?? '100px'

	// 合并规则：schema 字段级 ruleList + 顶层 rules（顶层优先覆盖）
	const mergedRules = computed<Record<string, FormItemRule[]>>(() => {
		const result: Record<string, FormItemRule[]> = {}
		for (const field of options.formSchema) {
			if (field.ruleList?.length) {
				result[field.prop] = [...field.ruleList]
			}
		}
		if (options.rules) {
			for (const [key, val] of Object.entries(options.rules)) {
				result[key] = val
			}
		}
		return result
	})

	// ====== 弹窗状态 ======
	const visible = ref(false)
	const mode = ref<FormMode>('add')
	const currentId = ref<Key>(null)

	// ====== 表单状态 ======
	const formData = reactive({ ...(options.initFormData() as object) }) as any
	const formRef = ref()

	// ====== 提交状态 ======
	const submitLoading = ref(false)

	// 标题映射
	const titleMap: Record<string, string> = {
		add: '新增',
		update: '修改',
		detail: '详情'
	}

	// 计算弹窗标题
	const dialogTitle = computed(() => {
		// 完全自定义标题（优先级最高）
		if (options.title) return options.title
		if (mode.value === 'detail') return `${subject}详情`
		const action = titleMap[mode.value] ?? '操作'
		return `${action}${subject}`
	})

	// 计算成功消息
	const getSuccessMessage = () => {
		if (options.successMessage) {
			return typeof options.successMessage === 'function' ? options.successMessage(mode.value) : options.successMessage
		}
		return `${subject}${titleMap[mode.value]}成功`
	}

	// 打开弹窗
	const open = (openMode: FormMode, id?: Key, ctx?: any) => {
		mode.value = openMode
		currentId.value = id ?? null

		// 打开前回调
		options.beforeOpen?.(ctx)

		visible.value = true

		nextTick(() => {
			// 重置表单数据（支持上下文参数）
			Object.assign(formData, options.initFormData(ctx))
			formRef.value?.clearValidate()

			// 打开后回调
			options.afterOpen?.()

			if (id && openMode !== 'add') {
				// 调用详情接口
				options
					.detailApi?.(id)
					.then(data => {
						// 赋值前回调（可预处理数据）
						options.beforeDataAssign?.(data)
						Object.assign(formData, data)
					})
					.catch(error => {
						if (options.onDetailError) {
							options.onDetailError(error)
						}
					})
			}
		})
	}

	// 关闭弹窗
	const close = () => {
		visible.value = false
	}

	// 提交（内部校验 + 调接口）
	const submit = () => {
		if (mode.value === 'detail') {
			ElMessage.warning('详情模式无法提交')
			return
		}

		if (submitLoading.value) {
			ElMessage.warning('请勿重复提交')
			return
		}

		// 表单校验
		formRef.value?.validate((valid: boolean) => {
			if (!valid) return

			// 提交前回调
			options.beforeSubmit?.()

			submitLoading.value = true
			options
				.submitApi(formData as DTO)
				.then(data => {
					ElMessage.success(getSuccessMessage())
					visible.value = false
					if (options.afterSubmit) {
						options.afterSubmit(data ?? formData)
					}
				})
				.catch(error => {
					if (options.onSubmitError) {
						options.onSubmitError(error)
					}
				})
				.finally(() => {
					submitLoading.value = false
				})
		})
	}

	// ====== 返回值 ======
	return {
		dialog: reactive({
			visible,
			mode,
			title: dialogTitle,
			open,
			close
		}),
		form: shallowReactive({
			data: formData,
			rules: mergedRules,
			labelWidth,
			formRef
		}),
		submit: reactive({
			execute: submit,
			loading: submitLoading
		})
	}
}
