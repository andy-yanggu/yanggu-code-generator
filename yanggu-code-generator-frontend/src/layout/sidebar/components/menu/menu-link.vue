<template>
	<component :is="linkType" v-bind="linkProps">
		<slot></slot>
	</component>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import { MenuInfo } from '@/store/user-store'

const props = defineProps({
	menu: {
		type: Object as PropType<MenuInfo>,
		required: true
	}
})

// 判断组件类型：<a> 或 <router-link>
const linkType = computed(() => {
	if (props.menu.meta.type === 4) {
		return 'a'
	}
	return 'router-link'
})

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
		return { to: props.menu.path }
	}
})
</script>
