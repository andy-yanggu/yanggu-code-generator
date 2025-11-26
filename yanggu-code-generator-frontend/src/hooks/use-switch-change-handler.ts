import { ElMessage, ElMessageBox } from 'element-plus'
import { SwitchState, SwitchStateResult, SwitchUpdateConfig } from '@/types'

// 开关状态切换
export const useSwitchState = <T = any>(options: SwitchState<T>): SwitchStateResult<T> => {
	const { field, states } = options
	if (options.states.length !== 2) {
		throw new Error('Switch options must contain exactly two items')
	}

	const activeState = states.find(s => s.isActive)!
	const inactiveState = states.find(s => !s.isActive)!

	const textMap = Object.fromEntries(states.map(s => [s.value, s.text]))

	const confirmMessage = options.confirmText ?? ((newText: string) => `确定要修改为【${newText}】吗？`)

	const successMessage = options.successText ?? ((_: string, newText: string) => `修改为【${newText}】成功`)

	const getOldValue = (newValue: T) => {
		return newValue === activeState.value ? inactiveState.value : activeState.value
	}

	return {
		field,
		activeValue: activeState.value,
		inactiveValue: inactiveState.value,
		activeText: activeState.text,
		inactiveText: inactiveState.text,
		text: textMap,
		getOldValue,
		confirmMessage,
		successMessage
	}
}

export const useSwitchChangeHandler = <T = any>(switchUpdateConfig: SwitchUpdateConfig<T>) => {
	return async (newValue: T, row: any) => {
		const { switchState, apiFn, afterSuccess } = switchUpdateConfig
		const { field, text, getOldValue } = switchState

		const oldValue = getOldValue(newValue)
		const oldText = text[oldValue]
		const newText = text[newValue]

		try {
			await ElMessageBox.confirm(switchState.confirmMessage(newText), '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'primary'
			})

			await apiFn(newValue, row)

			ElMessage.success({
				message: switchState.successMessage(oldText, newText),
				onClose: () => {
					if (afterSuccess) {
						afterSuccess()
					}
				}
			})
		} catch {
			row[field] = oldValue
		}
	}
}
