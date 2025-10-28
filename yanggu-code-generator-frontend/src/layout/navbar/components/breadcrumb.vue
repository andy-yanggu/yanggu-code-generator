<template>
	<el-breadcrumb separator="/">
		<el-breadcrumb-item v-for="item in breadcrumbList" :key="item.title">
			<template #default>
				<span style="display: flex; align-items: center; gap: 5px">
					<svg-icon v-if="item.icon && systemSettingStore.isOpenBreadcrumbIcon" :icon="item.icon"></svg-icon>
					<el-tooltip :content="item.title" :disabled="!breadcrumbTitleRecord[item.title]" placement="top">
						<el-text :ref="el => (breadcrumbTitleRefs[item.title] = el)" class="breadcrumb-title">
							{{ item.title }}
						</el-text>
					</el-tooltip>
				</span>
			</template>
		</el-breadcrumb-item>
	</el-breadcrumb>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/svg-icon/index.vue'
import { useSystemSettingStore } from '@/store/system-setting-store'
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
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
const breadcrumbTitleRecord: Record<string, boolean> = reactive({})
const breadcrumbTitleRefs: Record<string, any> = reactive({})

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

// 检测面包屑文字是否过长
onMounted(() => {
	nextTick(() => {
		Object.entries(breadcrumbTitleRefs).forEach(([title, el]) => {
			const element = el.$el
			breadcrumbTitleRecord[title] = element.scrollWidth > element.offsetWidth
		})
	})
})

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
<style scoped lang="scss">
.breadcrumb-title {
	max-width: 100px; /* 控制最大宽度 */
	display: inline-block;
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
	vertical-align: middle;
	color: inherit;
}
</style>
