/**
 * 路由相关类型定义
 */

/**
 * 路由元数据类型
 * 用于标签栏、面包屑、缓存等功能
 */
export interface RouteMetaData {
	/** 路径 */
	path: string
	/** 完整路径 */
	fullPath: string
	/** 组件名称 */
	name: string
	/** 标题 */
	title: string
	/** 图标 */
	icon: string
	/** 缓存 */
	cache: boolean
	/** 隐藏 */
	hidden: boolean
	/** 类型 */
	type: RouteType
	/** 外链地址 */
	externalUrl?: string
}

/**
 * 路由类型枚举
 */
export enum RouteType {
	/** 目录 */
	DIRECTORY = 0,
	/** 菜单 */
	MENU = 1,
	/** 按钮 */
	BUTTON = 2,
	/** iframe */
	IFRAME = 3,
	/** 外链 */
	EXTERNAL = 4
}
