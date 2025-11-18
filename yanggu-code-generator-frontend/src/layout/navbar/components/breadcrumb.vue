<template>
	<el-breadcrumb separator="/">
		<el-breadcrumb-item v-for="item in breadcrumbList" :key="item.title">
			<template #default>
				<icon-text-tooltip
					:enable-icon="systemSettingStore.isOpenBreadcrumbIcon"
					:icon="item.icon"
					:title="item.title"
					:placement="'bottom'"
				></icon-text-tooltip>
			</template>
		</el-breadcrumb-item>
	</el-breadcrumb>
</template>

<script setup lang="ts">
import IconTextTooltip from '@/components/icon-text-tooltip/index.vue'
import { useSystemSettingStore } from '@/store/system-setting-store'
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

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

// 面包屑列表
const breadcrumbList = ref<Breadcrumb[]>([])

// 挂载时设置面包屑
onMounted(() => {
	setBreadcrumb(route.fullPath)
})

// 动态设置面包屑
watch(
	() => route.fullPath,
	newPath => {
		if (newPath.startsWith('/redirect')) {
			return
		}
		setBreadcrumb(newPath)
	}
)

// 设置面包屑
const setBreadcrumb = (fullPath: string) => {
	const matched: Breadcrumb[] = []
	const paths = fullPath.split('/').filter(p => p)

	let currentPath = ''
	for (const path of paths) {
		currentPath += `/${path}`
		const breadcrumb = findRouteByPath(currentPath)
		if (breadcrumb.title) {
			matched.push(breadcrumb)
		}
	}
	breadcrumbList.value = matched
}

/**
 * 查找路径对应的 meta.title
 */
const findRouteByPath = (targetPath: string): Breadcrumb => {
	// 使用 Vue Router 的路径匹配算法
	const matchedRoute = router.resolve(targetPath)

	if (matchedRoute?.meta?.title) {
		return {
			title: matchedRoute.meta.title as string,
			icon: matchedRoute.meta.icon as string
		}
	} else {
		return { title: '', icon: '' }
	}
}
</script>
<style scoped>
:deep(.el-breadcrumb__inner),
:deep(.el-breadcrumb__inner.is-link),
:deep(.el-breadcrumb__separator) {
	color: var(--el-text-color-primary) !important;
}
</style>
