import { ElMessage, ElMessageBox } from 'element-plus'

export interface SwitchOption<T = any> {
	value: T
	text: string
	isActive: boolean
}

export interface SwitchState {
	field: string
	states: SwitchOption[]
	confirmText?: (newText: string) => string
	successText?: (oldText: string, newText: string) => string
}

export const useSwitchState = (options: SwitchState) => {
	const { field, states } = options
	if (options.states.length !== 2) {
		throw new Error('Switch options must contain exactly two items')
	}

	const activeState = states.find(s => s.isActive)!
	const inactiveState = states.find(s => !s.isActive)!

	const textMap = Object.fromEntries(states.map(s => [s.value, s.text]))

	const confirmMessage = options.confirmText ?? ((newText: string) => `确定要修改为「${newText}」吗？`)

	const successMessage = options.successText ?? ((_: string, newText: string) => `修改为「${newText}」成功`)

	const getOldValue = (newValue: any) => {
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

export const useSwitchChangeHandler = (
	switchState: ReturnType<typeof useSwitchState>,
	apiFn: (newValue: any, row: any) => Promise<any>,
	afterSuccess?: () => void
) => {
	return async (newValue: any, row: any) => {
		const { field, text, getOldValue } = switchState

		const oldValue = getOldValue(newValue)
		const oldText = text[oldValue]
		const newText = text[newValue]

		try {
			await ElMessageBox.confirm(switchState.confirmMessage(newText), '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})

			await apiFn(newValue, row)

			ElMessage.success(switchState.successMessage(oldText, newText))

			afterSuccess?.()
		} catch {
			row[field] = oldValue
		}
	}
}
