/**
 * 环境变量统一封装
 *
 * 所有 import.meta.env 的访问集中在此文件
 * 业务代码通过 import { env } from '@/config' 访问
 */
export const env = {
	/** 是否为开发环境 */
	isDev: import.meta.env.DEV,
	/** 是否为生产环境 */
	isProd: import.meta.env.PROD,
	/** API 基础地址 */
	apiUrl: import.meta.env.VITE_API_URL as string,
	/** 应用标题 */
	appTitle: import.meta.env.VITE_APP_TITLE as string
} as const
