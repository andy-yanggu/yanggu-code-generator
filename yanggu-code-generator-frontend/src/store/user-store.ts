import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { initReactiveObject, resetReactiveObject } from '@/utils/tool'
import { PersistenceOptions } from 'pinia-plugin-persistedstate'
import { MenuInfo, TokenInfo, UserInfo } from '@/types'

const INITIAL_USER_INFO: UserInfo = {
	username: 'admin',
	nickname: '张三',
	avatar: '',
	email: 'admin@qq.com',
	mobile: '18888888888'
}

const INITIAL_TOKEN_INFO: TokenInfo = {
	tokenName: 'satoken',
	accessToken: '',
	refreshToken: '',
	expire: 0
}

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
	// {
	// 	path: '/index2',
	// 	component: 'index/index',
	// 	name: 'Index2',
	// 	meta: {
	// 		title: '首页2',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index3',
	// 	component: 'index/index',
	// 	name: 'Index3',
	// 	meta: {
	// 		title: '首页3',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index4',
	// 	component: 'index/index',
	// 	name: 'Index4',
	// 	meta: {
	// 		title: '首页4',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index5',
	// 	component: 'index/index',
	// 	name: 'Index5',
	// 	meta: {
	// 		title: '首页5',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index6',
	// 	component: 'index/index',
	// 	name: 'Index6',
	// 	meta: {
	// 		title: '首页6',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index7',
	// 	component: 'index/index',
	// 	name: 'Index7',
	// 	meta: {
	// 		title: '首页7',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index8',
	// 	component: 'index/index',
	// 	name: 'Index8',
	// 	meta: {
	// 		title: '首页8',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index9',
	// 	component: 'index/index',
	// 	name: 'Index9',
	// 	meta: {
	// 		title: '首页9',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index10',
	// 	component: 'index/index',
	// 	name: 'Index10',
	// 	meta: {
	// 		title: '首页10',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index11',
	// 	component: 'index/index',
	// 	name: 'Index11',
	// 	meta: {
	// 		title: '首页11',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index12',
	// 	component: 'index/index',
	// 	name: 'Index12',
	// 	meta: {
	// 		title: '首页12',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index13',
	// 	component: 'index/index',
	// 	name: 'Index13',
	// 	meta: {
	// 		title: '首页13',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index14',
	// 	component: 'index/index',
	// 	name: 'Index14',
	// 	meta: {
	// 		title: '首页14',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/index15',
	// 	component: 'index/index',
	// 	name: 'Index15',
	// 	meta: {
	// 		title: '首页15',
	// 		icon: 'icon-dashboard-fill',
	// 		cache: true,
	// 		type: 1,
	// 	}
	// },
	// {
	// 	path: '/dir1',
	// 	name: 'Dir1',
	// 	meta: {
	// 		title: '目录1',
	// 		icon: 'icon-dashboard-fill',
	// 		type: 0
	// 	},
	// 	children: [
	// 		{
	// 			path: '/dir1/dir2',
	// 			name: 'Dir2',
	// 			meta: {
	// 				title: '目录2',
	// 				icon: 'icon-dashboard-fill',
	// 				type: 0
	// 			},
	// 			children: [
	// 				{
	// 					path: '/dir1/dir2/index1',
	// 					component: 'index/index',
	// 					name: 'Dir1Index1',
	// 					meta: {
	// 						title: '目录1-目录2-首页',
	// 						icon: 'icon-dashboard-fill',
	// 						cache: true,
	// 						type: 1,
	// 					}
	// 				}
	// 			]
	// 		}
	// 	]
	// },
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
	},
	{
		path: '/project-git',
		name: 'ProjectGit',
		meta: {
			title: '项目git地址',
			icon: 'icon-code',
			type: 0
		},
		children: [
			{
				path: 'github',
				name: 'ProjectGitGithub',
				meta: {
					title: 'github地址',
					icon: 'icon-github-fill',
					type: 4,
					externalUrl: 'https://github.com/andy-yanggu/yanggu-code-generator'
				}
			},
			{
				path: 'gitee',
				name: 'ProjectGitGitee',
				meta: {
					title: 'gitee地址',
					icon: 'icon-gitee-fill',
					type: 4,
					externalUrl: 'https://gitee.com/andy_yanggu/yanggu-code-generator'
				}
			}
		]
	},
	{
		path: '/gen',
		name: 'Gen',
		meta: {
			title: '代码生成器',
			icon: 'icon-appstore',
			type: 0
		},
		children: [
			{
				path: 'project',
				name: 'GenProject',
				component: 'gen/project/index',
				meta: {
					title: '项目管理',
					icon: 'icon-project',
					type: 1,
					cache: true
				}
			},
			{
				path: 'table',
				name: 'GenTable',
				component: 'gen/table/index',
				meta: {
					title: '表管理',
					icon: 'icon-table',
					type: 1,
					cache: true
				}
			},
			{
				path: 'enum',
				name: 'GenEnum',
				component: 'gen/enum/index',
				meta: {
					title: '枚举管理',
					icon: 'icon-merge-cells',
					type: 1,
					cache: true
				}
			},
			{
				path: 'template-group',
				name: 'GenTemplateGroup',
				component: 'gen/template-group/index',
				meta: {
					title: '模板组管理',
					icon: 'icon-file',
					type: 1,
					cache: true
				}
			},
			{
				path: 'datasource',
				name: 'GenDatasource',
				component: 'gen/datasource/index',
				meta: {
					title: '数据源管理',
					icon: 'icon-database',
					type: 1,
					cache: true
				}
			},
			{
				path: 'base-class',
				name: 'GenBaseClass',
				component: 'gen/base-class/index',
				meta: {
					title: '基类管理',
					icon: 'icon-cluster',
					type: 1,
					cache: true
				}
			},
			{
				path: 'field-type',
				name: 'GenFieldType',
				component: 'gen/field-type/index',
				meta: {
					title: '字段类型管理',
					icon: 'icon-menu',
					type: 1,
					cache: false
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

export const useUserStore = defineStore(
	'user',
	() => {
		// 状态
		// 是否添加路由
		const isAddRoutes = ref(false)
		// 菜单列表
		const menuList = ref<MenuInfo[]>([...sidebarConstantMenuInfoList, ...businessMenuInfoList])
		// 激活菜单
		const activeMenuPath = ref('')
		// 权限列表
		const permissionList = ref<string[]>([])
		// 角色列表
		const roleList = ref<string[]>([])
		// 登录用户信息
		// reactive每次初始化，必须传入一个全新的对象
		const userInfo = initReactiveObject(INITIAL_USER_INFO)
		// token信息
		const tokenInfo = initReactiveObject(INITIAL_TOKEN_INFO)

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
			resetReactiveObject(userInfo, INITIAL_USER_INFO)

			// 完全清空 tokenInfo
			resetReactiveObject(tokenInfo, INITIAL_TOKEN_INFO)
		}

		// 处理菜单列表中的路径。相对路径转换成绝对路径
		const processMenuList = (menus: MenuInfo[], parentPath: string = ''): MenuInfo[] => {
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
				if (menu.children && menu.children.length > 0) {
					processedMenu.children = processMenuList(menu.children, fullPath)
				}

				return processedMenu
			})
		}

		// 设置登录后的数据
		const setData = (loginVO: any) => {
			Object.assign(userInfo, loginVO.userInfo)
			Object.assign(tokenInfo, loginVO.tokenInfo)

			// 处理登录返回的菜单列表
			if (loginVO.menuList && Array.isArray(loginVO.menuList) && loginVO.menuList.length > 0) {
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
