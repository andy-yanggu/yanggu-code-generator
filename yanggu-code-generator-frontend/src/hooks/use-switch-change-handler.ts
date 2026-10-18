import type { SwitchUpdateConfig } from '@/types'

export const useSwitchChangeHandler = <T = any>(config: SwitchUpdateConfig<T>) => {
	const { switchField, confirmFieldText, confirmField, states, apiFn, afterSuccess, confirmText, successText } = config

	if (states.length !== 2) {
		throw new Error('Switch states must contain exactly two items')
	}

	const activeState = states.find(s => s.isActive)!
	const inactiveState = states.find(s => !s.isActive)!

	// Map 的 key 类型没有 string|number|symbol 约束，可以容纳 boolean 等任意开关值；
	// 若写成 Record<T, string>，TS 会要求 T extends string|number|symbol，
	// 而开关字段常见的是 boolean 或 0/1，加约束反而会让所有调用方报 TS2345。
	const textMap = new Map<T, string>(states.map(s => [s.value, s.text] as [T, string]))

	const getOldValue = (newValue: T): T => (newValue === activeState.value ? inactiveState.value : activeState.value)

	const getConfirmMessage =
		confirmText ??
		((confirmValue: string, confirmFieldText: string, newText: string) =>
			`确认要将【${confirmValue}】的【${confirmFieldText}】修改为【${newText}】吗？`)

	const getSuccessMessage =
		successText ??
		((confirmValue: string, confirmFieldText: string, _: string, newText: string) =>
			`【${confirmValue}】的【${confirmFieldText}】修改为【${newText}】成功`)

	const switchSubmitLoading = ref(false)

	const switchSubmitHandler = (newValue: T, row: any) => {
		const oldValue = getOldValue(newValue)
		// states 只有两项且已校验，正常不会取不到；?? '' 只为消除 undefined 传入文案函数的可能
		const oldText = textMap.get(oldValue) ?? ''
		const newText = textMap.get(newValue) ?? ''
		const confirmValue: string = String(row[confirmField])

		ElMessageBox.confirm(getConfirmMessage(confirmValue, confirmFieldText, newText), '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'primary'
		})
			.then(() => {
				switchSubmitLoading.value = true
				return apiFn(newValue, row)
			})
			.then(() => {
				ElMessage.success(getSuccessMessage(confirmValue, confirmFieldText, oldText, newText))
				afterSuccess?.()
			})
			.catch(error => {
				if (error === 'cancel') {
					ElMessage.info('已取消修改')
				}
				// 用户取消 or 接口失败 → 回滚
				row[switchField] = oldValue
			})
			.finally(() => {
				switchSubmitLoading.value = false
			})
	}

	return {
		activeValue: activeState.value,
		inactiveValue: inactiveState.value,
		activeText: activeState.text,
		inactiveText: inactiveState.text,
		switchSubmitLoading,
		switchSubmitHandler
	}
}
