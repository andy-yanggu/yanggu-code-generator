export interface UserInfo {
	username: string

	nickname: string

	avatar: string

	email: string

	mobile: string
}

export interface TokenInfo {
	// token名称
	tokenName: string
	// 访问令牌
	accessToken: string
	// 刷新令牌
	refreshToken: string
	// 过期时间
	expire: number
}

export interface MenuInfo {
	// 名称
	name: string
	// 路由
	path: string
	// 组件路径
	component?: string
	// 元数据
	meta: {
		// 标题
		title: string
		// 图标
		icon: string
		// 类型 0-目录、1-菜单、2-按钮、3-iframe、4-外链
		type: number
		// 外链地址
		externalUrl?: string
		// 缓存
		cache?: boolean
		// 隐藏
		hidden?: boolean
	}
	// 子菜单
	children?: MenuInfo[]
}
