import type { PersistenceOptions } from 'pinia-plugin-persistedstate'
import type { ColumnConfig } from '@/types/hooks/column-config'

// 持久化配置
const getPersistConfig = () => {
	return {
		key: 'tableSettingsStore',
		storage: localStorage
	} as PersistenceOptions
}

export const useTableSettingsStore = defineStore(
	'table-settings',
	() => {
		/** 各表格的列设置：tableKey -> ColumnConfig[] */
		const settingsMap = ref<Record<string, ColumnConfig[]>>({})

		/** 获取指定表格的已保存列设置 */
		const getColumns = (tableKey: string): ColumnConfig[] | undefined => {
			return settingsMap.value[tableKey]
		}

		/** 保存列设置 */
		const saveColumns = (tableKey: string, columns: ColumnConfig[]) => {
			settingsMap.value[tableKey] = columns
		}

		/** 重置指定表格的列设置（删除持久化数据） */
		const resetColumns = (tableKey: string) => {
			delete settingsMap.value[tableKey]
		}

		return {
			settingsMap,
			getColumns,
			saveColumns,
			resetColumns
		}
	},
	{
		persist: getPersistConfig()
	}
)
