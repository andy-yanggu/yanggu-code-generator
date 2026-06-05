import { isNotEmpty } from '@/utils/tool'
import { MenuInfo } from '@/types'

/**
 * 根据路径查找菜单
 *
 * @param menuList 菜单列表
 * @param targetPath 目标路径
 * @returns 找到的菜单项，未找到返回 null
 */
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

/**
 * 处理菜单列表中的路径，将相对路径转换成绝对路径
 *
 * @param menus 菜单列表
 * @param parentPath 父级路径
 * @returns 处理后的菜单列表
 */
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
			processedMenu.children = processMenuList(menu.children, fullPath)
		}

		return processedMenu
	})
}
