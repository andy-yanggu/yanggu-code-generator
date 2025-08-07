import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'

export interface UserInfo {
	username: string

	nickname: string

	avatar: string

	email: string

	mobile: string
}

export interface TokenInfo {
	// 访问令牌
	accessToken: string
	// 刷新令牌
	refreshToken: string
	// 过期时间
	expire: number
}

export interface MenuInfo {
	// 路径
	path: string
	// 名称
	name: string
	// 组件路径
	component?: string
	// 元数据
	meta: {
		// 标题
		title: string
		// 图标
		icon: string
		// 缓存
		cache: boolean
		// 类型 0-目录、1-菜单、2-按钮
		type: number
		// 隐藏
		hidden: boolean
	}
	// 子菜单
	children?: MenuInfo[]
}

const INITIAL_USER_INFO: UserInfo = {
	username: '',
	nickname: '',
	avatar: '',
	email: '',
	mobile: ''
}

const INITIAL_TOKEN_INFO: TokenInfo = {
	accessToken: '',
	refreshToken: '',
	expire: 0
}

export const menuRoutes: MenuInfo[] = [
]

export const useUserStore = defineStore(
	'user',
	() => {
		// 状态
		// 是否登录
		const isLogin = ref(false)
		// 是否添加路由
		const isAddRoutes = ref(false)
		// 菜单列表
		const menuList = ref<MenuInfo[]>([...menuRoutes])
		// 权限列表
		const permissionList = ref<string[]>([])
		// 登录用户信息
		// 这里必须使用解构的方式，不然一直使用一个对象
		// reactive每次初始化，必须传入一个全新的对象
		const userInfo = reactive<UserInfo>({ ...INITIAL_USER_INFO })
		// token信息
		const tokenInfo = reactive<TokenInfo>({ ...INITIAL_TOKEN_INFO })

		// 计算属性

		// actions
		// 设置添加路由的标志
		const setAddRouteFlag = () => {
			isAddRoutes.value = true
		}

		// 清空数据
		const clearAll = () => {
			menuList.value = []
			permissionList.value = []
			// 完全清空 userInfo
			Object.assign(userInfo, INITIAL_USER_INFO)

			// 完全清空 tokenInfo
			Object.assign(tokenInfo, INITIAL_TOKEN_INFO)
			isLogin.value = false
		}

		// 设置登录后的数据
		const setData = (loginVO: any) => {
			Object.assign(userInfo, loginVO.userInfo)
			Object.assign(tokenInfo, loginVO.tokenInfo)
			menuList.value = loginVO.menuList
			permissionList.value = loginVO.permissionList
			isLogin.value = true
		}

		return {
			isLogin,
			isAddRoutes,
			menuList,
			permissionList,
			userInfo,
			tokenInfo,
			clearAll,
			setData,
			setAddRouteFlag
		}
	},
	{
		persist: {
			key: 'userStore',
			omit: ['isAddRoutes'],
			storage: localStorage
		}
	}
)
