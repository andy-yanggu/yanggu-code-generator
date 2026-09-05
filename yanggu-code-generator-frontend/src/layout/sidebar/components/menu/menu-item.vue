<template>
	<!-- 渲染目录 -->
	<el-sub-menu
		v-if="menu.meta.type === 0"
		:id="`menu-${menuIndexPath.replace(/\//g, '-')}`"
		ref="rootRef"
		:key="'sub-menu-' + menuIndexPath"
		:index="menuIndexPath">
		<template #title>
			<menu-item-content :title="menu.meta.title" :icon="menu.meta.icon"></menu-item-content>
		</template>
		<!-- 递归渲染目录或者菜单 -->
		<template v-if="isNotEmpty(menu.children)">
			<menu-item v-for="sub in menu.children" :key="getSubMenuKey(sub)" :menu="sub" :parent-paths="submenuParentPaths"></menu-item>
		</template>
	</el-sub-menu>
	<!-- 渲染菜单、iframe、外链 -->
	<el-menu-item
		v-else-if="menu.meta.type != 2 && !effectiveHideMenu"
		:id="`menu-${menuIndexPath.replace(/\//g, '-')}`"
		ref="rootRef"
		:key="'menu-item-' + menuIndexPath"
		:index="menuIndexPath">
		<!-- 处理内联和外链（内嵌iframe和新窗口） -->
		<menu-link :menu="menu" :path="menuIndexPath">
			<menu-item-content :title="menu.meta.title" :icon="menu.meta.icon"></menu-item-content>
		</menu-link>
	</el-menu-item>
</template>

<script setup lang="ts">
import MenuItemContent from '@/layout/sidebar/components/menu/menu-item-content.vue'
import MenuLink from '@/layout/sidebar/components/menu/menu-link.vue'
import { useMenuPreferenceStore } from '@/store'
import { MenuInfo } from '@/types'
import { isNotEmpty } from '@/utils/tool'

defineOptions({
	name: 'MenuItem'
})

const props = defineProps({
	menu: {
		type: Object as PropType<MenuInfo>,
		required: true
	},
	parentPaths: {
		type: Array as PropType<string[]>,
		default: [] as string[]
	}
})

const menuPreferenceStore = useMenuPreferenceStore()

// 计算有效的 hideMenu（用户偏好 > 服务端默认）
const effectiveHideMenu = computed(() =>
	menuPreferenceStore.getEffectiveHideMenu(props.menu.path, props.menu.meta.hideMenu ?? false)
)

// 计算菜单项的完整路径索引
const menuIndexPath = computed(() => {
	const path = props.menu.path
	// 如果已经是绝对路径，直接返回
	if (path.startsWith('/')) {
		return path
	}

	// 如果是相对路径，需要拼接父级路径
	if (props.parentPaths.length > 0) {
		const parentPath = props.parentPaths[props.parentPaths.length - 1]
		return parentPath.endsWith('/') ? `${parentPath}${path}` : `${parentPath}/${path}`
	}

	// 默认情况直接返回路径
	return path
})

// 计算子菜单的父级路径数组
const submenuParentPaths = computed(() => {
	return [...props.parentPaths, menuIndexPath.value]
})

// 获取子菜单的唯一key
const getSubMenuKey = (subMenu: MenuInfo) => {
	const prefix = subMenu.meta.type === 0 ? 'sub-menu-' : 'menu-item-'
	const path = subMenu.path
	if (path.startsWith('/')) {
		return prefix + path
	}
	const basePath = submenuParentPaths.value.join('/')
	const fullPath = basePath.endsWith('/') ? `${basePath}${path}` : `${basePath}/${path}`
	return `${prefix}${fullPath}`
}
</script>
