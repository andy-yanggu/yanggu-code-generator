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
	// 路由
	path?: string
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
				path: '/test-external/iframe',
				name: 'TestExternalIframe',
				meta: {
					title: 'java官网（iframe）',
					icon: 'icon-java',
					type: 3,
					externalUrl: 'http://localhost:8888/code-generator/project/detail?id=14',
					cache: true
				}
			},
			{
				path: '/test-external/iframe2',
				name: 'TestExternalIframe2',
				meta: {
					title: 'doc网站（iframe）',
					icon: 'icon-Batchfolding',
					type: 3,
					externalUrl: 'http://localhost:8888/code-generator/project/detail?id=19',
					cache: true
				}
			},
			{
				path: '/test-external/new-window',
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
				path: '/project-git/github',
				name: 'ProjectGitGithub',
				meta: {
					title: 'github地址',
					icon: 'icon-github-fill',
					type: 4,
					externalUrl: 'https://github.com/andy-yanggu/yanggu-code-generator'
				}
			},
			{
				path: '/project-git/gitee',
				name: 'ProjectGitGitee',
				meta: {
					title: 'gitee地址',
					icon: 'icon-gitee-fill-round',
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
				path: '/gen/project',
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
				path: '/gen/table',
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
				path: '/gen/enum',
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
				path: '/gen/template-group',
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
				path: '/gen/datasource',
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
				path: '/gen/base-class',
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
				path: '/gen/field-type',
				name: 'GenFieldType',
				component: 'gen/field-type/index',
				meta: {
					title: '字段类型管理',
					icon: 'icon-menu',
					type: 1,
					cache: true
				}
			}
		]
	}
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
		const menuList = ref<MenuInfo[]>([...sidebarConstantMenuInfoList, ...businessMenuInfoList])
		// 激活菜单
		const activeMenuPath = ref<string>('')
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
			menuList.value = [...sidebarConstantMenuInfoList, ...loginVO.menuList]
			permissionList.value = loginVO.permissionList
			isLogin.value = true
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
		persist: {
			key: 'userStore',
			omit: ['isAddRoutes'],
			storage: localStorage
		}
	}
)
