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
