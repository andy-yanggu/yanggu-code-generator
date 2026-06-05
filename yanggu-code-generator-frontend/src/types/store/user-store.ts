import { RouteMeta } from 'vue-router'

// 用户信息
export interface UserInfo {
	// 用户名
	username: string
	// 昵称
	nickname: string
	// 头像
	avatar: string
	// 邮箱
	email: string
	// 手机
	mobile: string
}
// 令牌信息
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
// 菜单信息
export interface MenuInfo {
	// 名称
	name: string
	// 路由（"/"开头表示绝对路径，否则表示相对路径）
	path: string
	// 组件路径
	component?: string
	// 元数据
	meta: RouteMeta
	// 子菜单
	children?: MenuInfo[]
}
// 登录返回数据
export interface LoginVO {
	// 用户信息
	userInfo: UserInfo
	// 令牌信息
	tokenInfo: TokenInfo
	// 菜单列表
	menuList?: MenuInfo[]
	// 权限列表
	permissionList?: string[]
	// 角色列表
	roleList?: string[]
}
