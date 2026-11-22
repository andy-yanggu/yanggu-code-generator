// 标签数据
export interface NavbarTag {
	// 完整路径
	fullPath: string
	// 组件名称
	name: string
	// 标题
	title: string
	// 图标
	icon: string
	// 是否固定
	pinned?: boolean
	// 固定时间（用于排序）
	pinnedAt?: number | null
}

// iframe数据
export interface IframeInfo {
	// 路由名称
	name: string
	// 路由地址
	src: string
	// 全路径
	fullPath: string
}

// 布局大小
export type LayOutSize = 'large' | 'default' | 'small'
