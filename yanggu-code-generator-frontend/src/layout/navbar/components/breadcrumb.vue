<template>
	<el-breadcrumb separator="/">
		<el-breadcrumb-item v-for="item in breadcrumbList" :key="item.title">
			<template #default>
				<icon-text-tooltip :enable-icon="systemSettingStore.toolbar.isOpenBreadcrumbIcon" :icon="item.icon" :title="item.title" placement="bottom">
				</icon-text-tooltip>
			</template>
		</el-breadcrumb-item>
	</el-breadcrumb>
</template>

<script setup lang="ts">
import IconTextTooltip from '@/components/icon-text-tooltip/index.vue'
import { useMenuPreferenceStore, useSystemSettingStore } from '@/store'

defineOptions({
	name: 'Breadcrumb'
})

// 面包屑
interface Breadcrumb {
	// 标题
	title: string
	// 图标
	icon: string
}

const route = useRoute()
const router = useRouter()
const systemSettingStore = useSystemSettingStore()
const menuPreferenceStore = useMenuPreferenceStore()

// 面包屑列表：computed 同时依赖 route.path 与 preferenceMap
// → 路由切换、title 偏好变更时都会自动重算，无需再监听 titleChangeBus
const breadcrumbList = computed<Breadcrumb[]>(() => {
	// /redirect 是刷新页面用的过渡路由，剥掉前缀直接展示目标路径，避免闪烁
	const basePath = route.path.replace(/^\/redirect/, '')
	const matched: Breadcrumb[] = []

	let currentPath = ''
	for (const seg of basePath.split('/').filter(p => p)) {
		currentPath += `/${seg}`
		// 使用 Vue Router 的路径匹配算法解析 meta
		const resolved = router.resolve(currentPath)
		if (resolved?.meta?.title) {
			// 有效标题：用户偏好 > 服务端默认
			const { title } = menuPreferenceStore.getEffective(currentPath, { title: resolved.meta.title as string })
			matched.push({ title, icon: resolved.meta.icon as string })
		}
	}
	return matched
})
</script>
<style scoped>
:deep(.el-breadcrumb__inner),
:deep(.el-breadcrumb__inner.is-link),
:deep(.el-breadcrumb__separator) {
	color: var(--el-text-color-primary) !important;
}
</style>
