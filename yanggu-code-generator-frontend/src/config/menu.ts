import { MenuInfo } from '@/types'

/**
 * 常量菜单（不依赖后端，始终显示）
 * 如：首页、个人中心等
 */
export const CONSTANT_MENUS: MenuInfo[] = [
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

/**
 * 默认业务菜单（无后端时的降级菜单）
 * 如果后端返回菜单，则使用后端菜单覆盖此配置
 */
export const DEFAULT_BUSINESS_MENUS: MenuInfo[] = [
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
