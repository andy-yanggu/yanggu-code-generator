/**
 * Store 统一导出
 *
 * 使用方式：
 * import { useUserStore, useAppStore, useSystemSettingStore } from '@/store'
 */

export { useUserStore } from '@/store/user-store'
export { useAppStore } from '@/store/app-store'
export { useSystemSettingStore } from '@/store/system-setting-store'

// 如果需要，也可以导出常量
export { menuExpandWidthList, menuFoldWidthList } from '@/store/system-setting-store'
