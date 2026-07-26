/**
 * 路由配置文件
 * 集中管理路由相关的常量配置
 */

import { env } from '@/config'

/**
 * 路由白名单（无需登录即可访问的路由名称）
 */
export const ROUTE_WHITE_LIST: readonly string[] = ['AuthLogin', 'AuthRegister'] as const

/**
 * 默认布局名称
 */
export const DEFAULT_LAYOUT_NAME = 'Layout'

/**
 * 默认首页路径
 */
export const DEFAULT_HOME_PATH = '/index'

/**
 * 进度条延迟时间（毫秒）
 */
export const PROGRESS_DELAY = 500

/**
 * 是否需要登录校验
 * 开发环境：false（不需要登录，方便调试）
 * 生产环境：true（需要登录，保证安全）
 */
export const ENABLE_AUTH_CHECK = !env.isDev
