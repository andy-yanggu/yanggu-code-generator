import type { UseCrudFormOptions, UseCrudFormReturn } from '@/types/hooks/use-crud-form'
import type { Key } from '@/types/common'
import type { FormItemRule, FormMode } from '@/types/schema'
import { normalizeFormField } from '@/types/schema'
import { DEFAULT_FORM_LABEL_WIDTH, DEFAULT_PRIMARY_KEY, DEFAULT_SUBJECT } from '@/config/crud'
import { toValue } from 'vue'

export const useCrudForm = <DTO = any, VO = any>(options: UseCrudFormOptions<DTO, VO>): UseCrudFormReturn<DTO> => {
	const primaryKey = options.primaryKey ?? DEFAULT_PRIMARY_KEY
	const subject = options.subject ?? DEFAULT_SUBJECT
	const labelWidth = options.labelWidth ?? DEFAULT_FORM_LABEL_WIDTH

	// 归一化表单字段（补全 key/prop，一次性处理）
	const normalizedFormSchema = (options.formSchema ?? []).map(normalizeFormField)
	if (import.meta.env.DEV && normalizedFormSchema.length === 0) {
		console.warn('[crud] useCrudForm 收到空的 formSchema，弹窗将不渲染任何字段')
	}

	// 合并规则：schema 字段级 ruleList + 顶层 rules（顶层优先覆盖）
	const mergedRules = computed<Record<string, FormItemRule[]>>(() => {
		const result: Record<string, FormItemRule[]> = {}
		for (const field of normalizedFormSchema) {
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

	/**
	 * 重置表单数据。
	 *
	 * 不能只用 Object.assign —— 它只覆盖同名 key、从不删除多余 key。
	 * 详情接口返回的 VO 通常比 initFormData 的 DTO 多字段（createTime / createBy / 关联名称等），
	 * 这些字段会残留在 formData 里，下次「新增」时被一起提交给后端，造成脏数据入库。
	 */
	const assignFormData = (source: any) => {
		for (const key of Object.keys(formData)) {
			if (!(key in source)) {
				delete formData[key]
			}
		}
		Object.assign(formData, source)
	}

	// ====== 提交状态 ======
	const submitLoading = ref(false)
	// 详情加载中 / 加载失败：两种状态下表单都是 initFormData 的空值，必须挡住提交
	const detailLoading = ref(false)
	const detailFailed = ref(false)

	// 标题映射
	const titleMap: Record<string, string> = {
		add: '新增',
		update: '修改',
		copy: '复制',
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

	// 详情请求序号：每次 open / close 递增，用于丢弃迟到的响应
	let detailRequestSeq = 0

	// 加载详情并填充表单
	// seq 为发起时的序号：只有它仍等于 detailRequestSeq 才允许写表单，
	// 否则说明期间已切到别的记录/新增模式，这个响应已经过期
	const loadDetail = (id: Key, openMode: FormMode, seq: number) => {
		if (!options.detailApi) return

		detailLoading.value = true
		detailFailed.value = false
		options
			.detailApi(id)
			.then(data => {
				if (seq !== detailRequestSeq) return
				// 赋值前回调（可预处理数据）
				options.beforeDataAssign?.(data)
				assignFormData(data)
				// 复制模式：清除主键，作为新记录提交
				if (openMode === 'copy') {
					formData[primaryKey] = null
				}
				options.afterDataAssign?.(data)
			})
			.catch(error => {
				if (seq !== detailRequestSeq) return
				detailFailed.value = true
				options.onDetailError?.(error)
			})
			.finally(() => {
				// 旧请求结束时不能撤掉新请求的遮罩
				if (seq !== detailRequestSeq) return
				detailLoading.value = false
			})
	}

	// 打开弹窗
	const open = (openMode: FormMode, id?: Key) => {
		mode.value = openMode
		currentId.value = id ?? null

		// 使飞行中的详情请求作废：它若在本次打开之后才返回，
		// 会把上一条记录的数据写进当前表单（修改 A → 关闭 → 新增，就变成新增一条 A 的副本）
		detailRequestSeq += 1
		const seq = detailRequestSeq

		// 上一次详情加载留下的状态必须在这里清掉。
		// detailFailed 只有 loadDetail 内部会重置，而 loadDetail 仅在「有 id 且非 add」时调用：
		// 某次修改的详情接口失败后再点「新增」，true 会一直残留，
		// submit() 里那道闸门就会永久拦住新增，反复开关弹窗也无法恢复。
		// detailLoading 一并重置：若上一次 loadDetail 仍在飞行中，add 模式不会重新发起请求，
		// 遮罩和「数据加载中」的提交拦截会一直挂到那个旧请求结束。
		detailLoading.value = false
		detailFailed.value = false

		// 从 formContext 读取上下文（支持 computed / ref / reactive，toValue 自动解包）
		const ctx = toValue(options.formContext)

		// 打开前回调
		options.beforeOpen?.(ctx)

		// 必须在 visible = true 之前重置：nextTick 的回调在 DOM flush 之后才执行，
		// 放里面会让弹窗先用上一次的 formData 渲染一帧，用户看到旧数据闪一下
		assignFormData(options.initFormData(ctx))

		visible.value = true

		nextTick(() => {
			// clearValidate 依赖 el-form 实例，必须等 DOM 挂载后调用
			formRef.value?.clearValidate()

			// 打开后回调
			options.afterOpen?.()

			if (id && openMode !== 'add') {
				// 调用详情接口
				loadDetail(id, openMode, seq)
			}
		})
	}

	// 关闭弹窗
	const close = () => {
		// 关闭后到达的详情响应不应再写入表单；遮罩一并撤掉，
		// 否则 detailLoading 会一直停在 true（旧请求的 finally 已被序号守卫拦住）
		detailRequestSeq += 1
		detailLoading.value = false
		visible.value = false
	}

	// 提交（内部校验 + 调接口）
	const submit = () => {
		if (mode.value === 'detail') {
			ElMessage.warning('详情模式无法提交')
			return
		}

		// 详情未加载完成 / 加载失败时，表单里全是 initFormData 的空值，
		// 此时提交会把后端那条记录的字段全部清空（UI 遮罩可被绕过，这道代码闸门不能省）
		if (detailLoading.value) {
			ElMessage.warning('数据加载中，请稍候')
			return
		}

		if (detailFailed.value) {
			ElMessage.warning('详情加载失败，请关闭弹窗后重试')
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
		formSchema: normalizedFormSchema,
		dialog: reactive({
			visible,
			mode,
			title: dialogTitle,
			detailLoading,
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
