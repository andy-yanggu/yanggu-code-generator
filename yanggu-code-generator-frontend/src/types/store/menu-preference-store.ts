// 单个菜单项的偏好覆盖（所有字段可选，仅存储用户修改过的）
export interface MenuPreferenceItem {
	cache?: boolean
	hideMenu?: boolean
	hideTab?: boolean
}

// 偏好映射表：key = 菜单绝对路径, value = 覆盖配置
export type MenuPreferenceMap = Record<string, MenuPreferenceItem>
