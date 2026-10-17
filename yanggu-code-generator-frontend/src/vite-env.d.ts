/// <reference types="vite/client" />

interface ImportMetaEnv {
	/** 应用标题 */
	readonly VITE_APP_TITLE: string
	/** 后端 API 基础路径 */
	readonly VITE_API_URL: string
	/** 是否启用 MSW Mock */
	readonly VITE_ENABLE_MOCK: string
}

interface ImportMeta {
	readonly env: ImportMetaEnv
}
