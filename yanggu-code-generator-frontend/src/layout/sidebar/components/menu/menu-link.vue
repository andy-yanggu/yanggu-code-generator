<template>
	<component :is="linkType" v-bind="linkProps" class="menu-link" @click="handleClick">
		<slot></slot>
	</component>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store'
import { MenuInfo } from '@/types'

defineOptions({
	name: 'MenuLink'
})

const props = defineProps({
	menu: {
		type: Object as PropType<MenuInfo>,
		required: true
	},
	path: {
		type: String,
		required: true
	}
})

const userStore = useUserStore()

// 判断组件类型：<a> 或 <router-link>
const linkType = computed(() => {
	if (props.menu.meta.type === 4) {
		return 'a'
	}
	return 'router-link'
})

const handleClick = () => {
	userStore.setActiveMenuPath(props.path)
}

// 计算绑定属性
const linkProps = computed(() => {
	// 外链方式
	if (props.menu.meta.type === 4) {
		return { href: props.menu.meta.externalUrl, target: '_blank', rel: 'noopener' }
	} else if (props.menu.meta.type === 3) {
		// iframe 内嵌
		return { to: { path: props.path } }
	} else {
		// 普通路由
		return { to: { path: props.path } }
	}
})
</script>
<style scoped>
.menu-link {
	display: block;
	width: 100%;
	height: 100%;
	color: inherit;
	text-decoration: none;
}
</style>
