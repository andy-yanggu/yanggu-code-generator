import type { UseCrudFormOptions } from '@/types/hooks/use-crud-form'
import type { Key } from '@/types/common'
import type { FormMode } from '@/types/schema'

export const useCrudForm = (options: UseCrudFormOptions) => {
	const subject = options.subject ?? '数据'
	const labelWidth = options.labelWidth ?? '100px'

	// ====== 弹窗状态 ======
	const visible = ref(false)
	const mode = ref<FormMode>('add')
	const currentId = ref<Key>(null)

	// ====== 表单状态 ======
	const formData = reactive({ ...options.initFormData() })
	const formRef = ref()

	// ====== 提交状态 ======
	const submitLoading = ref(false)

	// 标题映射
	const titleMap: Record<string, string> = {
		add: '新增',
		update: '修改',
		detail: ''
	}

	const dialogTitle = computed(() => {
		if (mode.value === 'detail') return `${subject}详情`
		const action = titleMap[mode.value] ?? '操作'
		return `${action}${subject}`
	})

	// 打开弹窗
	const open = (openMode: FormMode, id?: Key) => {
		mode.value = openMode
		currentId.value = id ?? null
		visible.value = true

		nextTick(() => {
			// 重置表单数据
			Object.assign(formData, options.initFormData())
			formRef.value?.clearValidate()

			if (id && openMode !== 'add') {
				// 调用详情接口
				options.detailApi?.(id).then(data => {
					Object.assign(formData, data)
				})
			}
		})
	}

	// 关闭弹窗
	const close = () => {
		visible.value = false
	}

	// 提交（校验由 CrudDialog 内部处理，只有校验通过才会调用此函数）
	const submit = () => {
		if (mode.value === 'detail') {
			ElMessage.warning('详情模式无法提交')
			return
		}

		submitLoading.value = true
		options
			.submitApi(formData)
			.then(data => {
				ElMessage.success(`${subject}${titleMap[mode.value]}成功`)
				visible.value = false
				if (options.submitAfter) {
					options.submitAfter(data ?? formData)
				}
			})
			.finally(() => {
				submitLoading.value = false
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
		form: reactive({
			data: formData,
			rules: options.rules ?? {},
			labelWidth,
			formRef
		}),
		submit: reactive({
			execute: submit,
			loading: submitLoading
		})
	}
}
