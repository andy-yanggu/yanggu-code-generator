import { ElMessage, ElMessageBox } from 'element-plus'
import { ref } from 'vue'
import type { SwitchUpdateConfig } from '@/types'

export const useSwitchChangeHandler = <T = any>(config: SwitchUpdateConfig<T>) => {
	const { switchField, confirmFieldText, confirmField, states, apiFn, afterSuccess, confirmText, successText } = config

	if (states.length !== 2) {
		throw new Error('Switch states must contain exactly two items')
	}

	const activeState = states.find(s => s.isActive)!
	const inactiveState = states.find(s => !s.isActive)!

	const textMap = Object.fromEntries(states.map(s => [s.value, s.text])) as Record<T, string>

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
		const oldText = textMap[oldValue]
		const newText = textMap[newValue]
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
			.catch(() => {
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
