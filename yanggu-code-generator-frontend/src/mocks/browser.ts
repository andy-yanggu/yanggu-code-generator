/**
 * MSW Browser Worker 初始化
 *
 * 由 Vite 插件 vite-plugin-msw-inject.ts 通过虚拟模块动态导入。
 * worker.start() 注册 Service Worker，拦截匹配 handler 的网络请求。
 */

import { setupWorker } from 'msw/browser'
import { handlers } from './handlers'

export const worker = setupWorker(...handlers)
