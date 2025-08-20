<template>
	<el-sub-menu v-if="menu.meta.type === 0" ref="rootRef" :key="'sub-menu-' + menu.path" :index="menu.path">
		<template #title>
			<menu-item-content :title="menu.meta.title" :icon="menu.meta.icon"></menu-item-content>
		</template>
		<!-- 递归渲染目录或者菜单 -->
		<template v-if="menu.children && menu.children.length > 0">
			<menu-item v-for="sub in menu.children" :key="'sub-menu-' + sub.path" :menu="sub" :ref-map="refMap"></menu-item>
		</template>
	</el-sub-menu>
	<el-menu-item v-else-if="menu.meta.type === 1 && !menu.meta.hidden" ref="rootRef" :key="'menu-item-' + menu.path" :index="menu.path">
		<menu-item-content :title="menu.meta.title" :icon="menu.meta.icon"></menu-item-content>
	</el-menu-item>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import MenuItemContent from '@/layout/sidebar/components/menu-item-content.vue'

const props = defineProps({
	menu: {
		type: Object,
		required: true
	},
	refMap: {
		type: Map,
		required: true
	}
})
const rootRef = ref()
// 添加ref引用到refMap中
onMounted(() => {
	props.refMap.set(props.menu.path, rootRef.value)
})
</script>
