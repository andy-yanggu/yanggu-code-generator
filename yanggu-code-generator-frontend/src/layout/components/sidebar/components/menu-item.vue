<template>
	<el-sub-menu v-if="menu.children?.length > 0" :key="'sub-' + menu.path" :index="menu.path">
		<template #title>
			<svg-icon :icon="menu.meta.icon"></svg-icon>
			<span>{{ menu.meta.title }}</span>
		</template>
		<menu-item v-for="sub in menu.children" :key="sub.path" :menu="sub"></menu-item>
	</el-sub-menu>
	<el-menu-item v-else :key="'item-' + menu.path" :index="menu.path">
		<svg-icon :icon="menu.meta.icon"></svg-icon>
		<span>{{ menu.meta.title }}</span>
	</el-menu-item>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/svg-icon/src/svg-icon.vue'

defineProps({
	menu: {
		type: Object,
		required: true
	}
})
</script>
