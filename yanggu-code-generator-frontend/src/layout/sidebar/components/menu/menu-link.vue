<template>
	<component :is="linkType" v-bind="linkProps" @click="handleClick">
		<slot></slot>
	</component>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import { useUserStore } from '@/store'
import { MenuInfo } from '@/types'

defineOptions({
	name: 'MenuLink'
})

const props = defineProps({
	menu: {
		type: Object as PropType<MenuInfo>,
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

const handleClick = (e: Event) => {
	// 外链点击阻止 el-menu router 跳转
	if (props.menu.meta.type === 4) {
		e.stopPropagation() // 阻止事件冒泡
	}
	userStore.setActiveMenuPath(props.menu.path!)
}

// 计算绑定属性
const linkProps = computed(() => {
	// 外链方式
	if (props.menu.meta.type === 4) {
		return { href: props.menu.meta.externalUrl, target: '_blank', rel: 'noopener' }
	} else if (props.menu.meta.type === 3) {
		// iframe 内嵌
		return { to: { path: props.menu.path } }
	} else {
		// 普通路由
		return { to: { path: props.menu.path } }
	}
})
</script>
