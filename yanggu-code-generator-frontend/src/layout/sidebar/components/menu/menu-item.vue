<template>
	<!-- 渲染目录 -->
	<el-sub-menu v-if="menu.meta.type === 0" ref="rootRef" :key="'sub-menu-' + menu.path" :index="menu.path">
		<template #title>
			<menu-item-content :title="menu.meta.title" :icon="menu.meta.icon"></menu-item-content>
		</template>
		<!-- 递归渲染目录或者菜单 -->
		<template v-if="menu.children && menu.children.length > 0">
			<menu-item
				v-for="sub in menu.children"
				:key="'sub-menu-' + sub.path"
				:menu="sub"
				:ref-map="refMap"
				@update:active-path="updateActivePath"
			></menu-item>
		</template>
	</el-sub-menu>
	<!-- 渲染菜单、iframe、外链 -->
	<el-menu-item v-else-if="menu.meta.type != 2 && !menu.meta.hidden" ref="rootRef" :key="'menu-item-' + menu.path" :index="menu.path">
		<!-- 处理内联和外链（内嵌iframe和新窗口） -->
		<menu-link :menu="menu" @update:active-path="updateActivePath">
			<menu-item-content :title="menu.meta.title" :icon="menu.meta.icon"></menu-item-content>
		</menu-link>
	</el-menu-item>
</template>

<script setup lang="ts">
import { onMounted, PropType, ref } from 'vue'
import MenuItemContent from '@/layout/sidebar/components/menu/menu-item-content.vue'
import MenuLink from '@/layout/sidebar/components/menu/menu-link.vue'
import { MenuInfo } from '@/store/user-store'

const emitHandler = defineEmits(['update:activePath'])

const props = defineProps({
	menu: {
		type: Object as PropType<MenuInfo>,
		required: true
	},
	refMap: {
		type: Map,
		required: true
	}
})

const updateActivePath = (path: string) => {
	emitHandler('update:activePath', path)
}
const rootRef = ref()
// 添加ref引用到refMap中
onMounted(() => {
	props.refMap.set(props.menu.path, rootRef.value)
})
</script>
