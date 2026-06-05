import { defineStore } from 'pinia'
import { computed, reactive, ref } from 'vue'
import { isNotEmpty } from '@/utils/tool'
import { processMenuList } from '@/utils/menu'
import { PersistenceOptions } from 'pinia-plugin-persistedstate'
import { LoginVO, MenuInfo, TokenInfo, UserInfo } from '@/types'
import { CONSTANT_MENUS, DEFAULT_BUSINESS_MENUS } from '@/config'

const initialUserInfo = (): UserInfo => ({
	username: 'admin',
	nickname: '张三',
	avatar: '',
	email: 'admin@qq.com',
	mobile: '18888888888'
})

const initialTokenInfo = (): TokenInfo => ({
	tokenName: 'satoken',
	accessToken: '',
	refreshToken: '',
	expire: 0
})

// 持久化配置
const getPersistConfig = () => {
	const key = 'userStore'
	if (import.meta.env.PROD) {
		return {
			key,
			omit: ['isAddRoutes'],
			storage: localStorage
		} as PersistenceOptions
	} else {
		return {
			key,
			pick: ['userInfo', 'tokenInfo', 'activeMenuPath'],
			storage: localStorage
		} as PersistenceOptions
	}
}

export const useUserStore = defineStore(
	'user',
	() => {
		// 状态
		// 是否添加路由
		const isAddRoutes = ref(false)
		// 菜单列表：常量菜单 + 开发测试菜单 + 默认业务菜单
		const menuList = ref<MenuInfo[]>(processMenuList([...CONSTANT_MENUS, ...DEFAULT_BUSINESS_MENUS]))
		// 激活菜单
		const activeMenuPath = ref('')
		// 权限列表
		const permissionList = ref([] as string[])
		// 角色列表
		const roleList = ref([] as string[])
		// 登录用户信息
		// reactive每次初始化，必须传入一个全新的对象
		const userInfo = reactive(initialUserInfo())
		// token信息
		const tokenInfo = reactive(initialTokenInfo())

		// 计算属性
		const isLogin = computed(() => {
			return tokenInfo.accessToken !== ''
		})

		// actions
		// 设置添加路由的标志
		const setAddRouteFlag = () => {
			isAddRoutes.value = true
		}

		// 清空数据
		const clearAll = () => {
			menuList.value = []
			roleList.value = []
			permissionList.value = []
			// 完全清空 userInfo
			Object.assign(userInfo, initialUserInfo())

			// 完全清空 tokenInfo
			Object.assign(tokenInfo, initialTokenInfo())
		}

		// 设置登录后的数据
		const setData = (loginVO: LoginVO) => {
			Object.assign(userInfo, loginVO.userInfo)
			Object.assign(tokenInfo, loginVO.tokenInfo)

			// 合并策略：常量菜单 + 开发测试菜单 + 后端菜单
			const backendMenus = isNotEmpty(loginVO.menuList) ? loginVO.menuList : DEFAULT_BUSINESS_MENUS

			menuList.value = processMenuList([...CONSTANT_MENUS, ...backendMenus])

			permissionList.value = loginVO.permissionList ?? []
			roleList.value = loginVO.roleList ?? []
		}

		// 设置激活的菜单
		const setActiveMenuPath = (path: string) => {
			activeMenuPath.value = path
		}

		return {
			isLogin,
			isAddRoutes,
			menuList,
			activeMenuPath,
			permissionList,
			userInfo,
			tokenInfo,
			clearAll,
			setData,
			setAddRouteFlag,
			setActiveMenuPath
		}
	},
	{
		persist: getPersistConfig()
	}
)
