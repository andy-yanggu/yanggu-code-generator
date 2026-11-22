import { defineStore } from 'pinia'
import { computed, reactive, ref } from 'vue'
import { isNotEmpty } from '@/utils/tool'
import { PersistenceOptions } from 'pinia-plugin-persistedstate'
import { MenuInfo, TokenInfo, UserInfo } from '@/types'

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

// 定义需要在侧边栏显示的常量菜单
const sidebarConstantMenuInfoList: MenuInfo[] = [
	{
		path: '/index',
		component: 'index/index',
		name: 'Index',
		meta: {
			title: '首页',
			icon: 'icon-home',
			cache: true,
			type: 1
		}
	}
	// 可以添加其他需要在侧边栏显示的常量菜单
]

// 业务菜单
const businessMenuInfoList: MenuInfo[] = [
	{
		path: '/test-echarts',
		component: 'test-echarts/index',
		name: 'TestEcharts',
		meta: {
			title: '测试echarts',
			icon: 'icon-areachart',
			type: 1,
			cache: true
		}
	},
	{
		path: '/icon-search',
		component: 'icon-search/index',
		name: 'IconSearch',
		meta: {
			title: '图标搜索',
			icon: 'icon-search',
			type: 1,
			cache: true
		}
	},
	{
		path: '/test-external',
		name: 'TestExternal',
		meta: {
			title: '外链测试',
			icon: 'icon-link',
			type: 0
		},
		children: [
			{
				path: 'iframe',
				name: 'TestExternalIframeJava',
				meta: {
					title: 'java官网（iframe）',
					icon: 'icon-java',
					type: 3,
					externalUrl: 'https://www.java.com',
					cache: true
				}
			},
			{
				path: 'iframe2',
				name: 'TestExternalIframeVite',
				meta: {
					title: 'vite官网（iframe）（不缓存）',
					icon: 'icon-Batchfolding',
					type: 3,
					externalUrl: 'https://cn.vitejs.dev/',
					cache: false
				}
			},
			{
				path: 'iframe3',
				name: 'TestExternalIframeElementPlus',
				meta: {
					title: 'element-plus',
					icon: 'icon-java',
					type: 3,
					externalUrl: 'https://cn.element-plus.org/zh-CN/guide/design.html',
					cache: true
				}
			},
			{
				path: 'new-window',
				name: 'TestExternalNewWindow',
				meta: {
					title: '阿里巴巴（新窗口）',
					icon: 'icon-alibaba',
					type: 4,
					externalUrl: 'https://www.alibaba.com'
				}
			}
		]
	}
]

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

// 根据路径查找菜单
export const findMenuByPath = (menuList: MenuInfo[], targetPath: string): MenuInfo | null => {
	for (const menu of menuList) {
		if (menu.path === targetPath) {
			return menu
		}
		if (isNotEmpty(menu.children)) {
			const found = findMenuByPath(menu.children!, targetPath)
			if (found) {
				return found
			}
		}
	}
	return null
}

// 处理菜单列表中的路径。相对路径转换成绝对路径
export const processMenuList = (menus: MenuInfo[], parentPath: string = ''): MenuInfo[] => {
	return menus.map(menu => {
		// 处理路径拼接逻辑
		let fullPath: string
		if (menu.path?.startsWith('/')) {
			// 绝对路径保持不变
			fullPath = menu.path
		} else {
			// 相对路径，需要拼接父路径
			const cleanParentPath = parentPath.replace(/\/$/, '') // 移除父路径末尾的斜杠
			fullPath = cleanParentPath + '/' + (menu.path || '')
		}

		const processedMenu: MenuInfo = {
			...menu,
			path: fullPath
		}

		// 递归处理子菜单
		if (isNotEmpty(menu.children)) {
			processedMenu.children = processMenuList(menu.children!, fullPath)
		}

		return processedMenu
	})
}

export const useUserStore = defineStore(
	'user',
	() => {
		// 状态
		// 是否添加路由
		const isAddRoutes = ref(false)
		// 菜单列表
		const menuList = ref(processMenuList([...sidebarConstantMenuInfoList, ...businessMenuInfoList]) as MenuInfo[])
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
		const setData = (loginVO: any) => {
			Object.assign(userInfo, loginVO.userInfo)
			Object.assign(tokenInfo, loginVO.tokenInfo)

			// 处理登录返回的菜单列表
			if (isNotEmpty(loginVO.menuList)) {
				menuList.value = processMenuList(loginVO.menuList)
			} else {
				menuList.value = []
			}
			permissionList.value = loginVO.permissionList
			roleList.value = loginVO.roleList
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
