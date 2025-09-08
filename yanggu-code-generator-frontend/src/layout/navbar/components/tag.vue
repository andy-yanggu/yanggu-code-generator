<template>
	<!-- 防止tag过多添加滚动条 -->
	<el-scrollbar ref="scrollbarRef" class="tag-scrollbar">
		<div class="tag-wrapper">
			<!-- 标签栏 -->
			<el-tag
				v-for="(tag, index) in appStore.tagsList"
				:ref="el => (tagRefs[tag.fullPath] = el)"
				:key="tag.fullPath"
				size="default"
				:effect="tag.fullPath === route.fullPath ? 'dark' : 'plain'"
				:closable="tag.fullPath != '/index' || appStore.tagLength > 1"
				@click="handleClick(index, tag)"
				@close="handleClose(index, tag)"
				@contextmenu.prevent="showTagMenu($event, tag, index)"
			>
				<template #default>
					<span style="display: inline-flex; align-items: center; gap: 5px">
						<svg-icon v-if="tag.icon && systemSettingStore.isOpenTagIcon" :icon="tag.icon"></svg-icon>
						{{ tag.title }}
					</span>
				</template>
			</el-tag>

			<!-- 右键菜单 -->
			<div v-if="tagMenuVisible" class="tag-context-menu" :style="menuPosition">
				<tag-menu
					:current-menu-tag="currentMenuTag"
					:current-menu-tag-index="currentMenuTagIndex"
					@close-current-tag="closeCurrentTag()"
					@refresh-current-tag="refreshCurrentTag()"
					@close-other-tags="closeOtherTags()"
					@close-left-tag="closeLeftTag()"
					@close-right-tag="closeRightTag()"
					@close-all-tags="closeAllTags()"
					@open-new-window="openNewWindow()"
				></tag-menu>
			</div>
		</div>
	</el-scrollbar>
</template>

<script setup lang="ts">
import { NavbarTag, useAppStore } from '@/store/app-store'
import { useRoute, useRouter } from 'vue-router'
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import TagMenu from '@/layout/navbar/components/tag-menu.vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import Sortable from 'sortablejs'
import { usePageRefresher } from '@/hooks/use-refuresh-current-page'
import { useSystemSettingStore } from '@/store/system-setting-store'

const route = useRoute()
const router = useRouter()
const tagMenuVisible = ref(false)
const menuPosition = ref({
	left: '0px',
	top: '0px'
})
const currentMenuTag = ref<NavbarTag>({} as NavbarTag)
const currentMenuTagIndex = ref(0)
const appStore = useAppStore()
const systemSettingStore = useSystemSettingStore()
const tagRefs: Record<string, HTMLElement | null> = {}
const scrollbarRef = ref()

onMounted(() => {
	// 点击页面任意位置关闭右键菜单
	document.addEventListener('click', closeTagMenu)
	scrollToTag(route.fullPath)
})

// 监控路由变化，实现滚动条自动滚动
watch(
	() => route.fullPath,
	(newPath, _) => {
		nextTick(() => {
			scrollToTag(newPath)
		})
	}
)

const scrollToTag = (fullPath: string) => {
	const tagEl = tagRefs[fullPath]
	const scrollWrapper = scrollbarRef.value?.wrapRef

	const tagDom = tagEl?.$el || tagEl?.el || tagEl
	if (!tagDom || !(tagDom instanceof HTMLElement) || !scrollWrapper) {
		return
	}

	tagDom.scrollIntoView({
		behavior: 'smooth',
		inline: 'center',
		block: 'nearest'
	})
}

// 实现标签页的拖拽效果
onMounted(() => {
	nextTick(() => {
		const el = document.querySelector('.tag-wrapper') as HTMLElement
		const scrollWrapper = scrollbarRef.value?.wrapRef as HTMLElement

		if (el && scrollWrapper) {
			new Sortable(el, {
				animation: 200,
				scroll: scrollWrapper, // ✅ 指定真正的滚动容器
				direction: 'horizontal', // ✅ 明确水平拖拽
				scrollSensitivity: 30,
				scrollSpeed: 30,
				onEnd: evt => {
					const { oldIndex, newIndex } = evt
					if (oldIndex !== null && newIndex !== null && oldIndex !== newIndex) {
						const newTags = [...appStore.tagsList]
						const movedTag = newTags.splice(oldIndex!, 1)[0]
						newTags.splice(newIndex!, 0, movedTag)
						appStore.addAllTags(newTags)
					}
				}
			})
		} else {
			console.error('未找到.tag-wrapper或scrollWrapper元素')
		}
	})
})

// 组件卸载时移除事件监听
onUnmounted(() => {
	document.removeEventListener('click', closeTagMenu)
})

// 处理点击标签
const handleClick = (_: number, tag: NavbarTag) => {
	router.push(tag.fullPath)
}

// 处理关闭单个标签
const handleClose = (index: number, tag: NavbarTag) => {
	// 新增：首页保护逻辑（当只有一个标签且是首页时不允许关闭）
	if (appStore.tagLength === 1 && tag.fullPath === '/index') {
		return // 直接返回，不执行关闭操作
	}

	// 删除对应的缓存组件和标签
	deleteCacheAndTag([tag])

	// 判断当前标签页是否为当前路由
	if (tag.fullPath === route.fullPath) {
		let to = '/index' // 默认跳转首页

		// 关闭后如果还有标签页
		if (appStore.tagLength > 0) {
			// 优先尝试右侧标签
			if (index < appStore.tagLength) {
				to = appStore.tagsList[index].fullPath
			} else if (index > 0) {
				// 右侧无标签时选择左侧
				to = appStore.tagsList[index - 1].fullPath
			}
		}
		// 跳转到对应路由
		router.push(to)
	}
}

// 显示标签右键菜单
const showTagMenu = (e: MouseEvent, tag: NavbarTag, index: number) => {
	e.preventDefault()
	currentMenuTag.value = tag
	currentMenuTagIndex.value = index

	const x = e.clientX
	const y = e.clientY

	const menuWidth = 80
	const menuHeight = 200
	const windowWidth = window.innerWidth
	const windowHeight = window.innerHeight

	menuPosition.value = {
		left: `${Math.min(x, windowWidth - menuWidth)}px`,
		top: `${Math.min(y, windowHeight - menuHeight)}px`
	}

	tagMenuVisible.value = true
}

// 关闭标签菜单
const closeTagMenu = () => {
	tagMenuVisible.value = false
}

// 刷新当前标签页
const refreshCurrentTag = () => {
	if (route.fullPath != currentMenuTag.value.fullPath) {
		router.push(currentMenuTag.value.fullPath)
		setTimeout(() => refreshPage(), 10)
	} else {
		refreshPage()
	}
	closeTagMenu()
}

// 关闭当前标签
const closeCurrentTag = () => {
	handleClose(currentMenuTagIndex.value, currentMenuTag.value)
	closeTagMenu()
}

// 关闭其他标签
const closeOtherTags = () => {
	const currentPath = currentMenuTag.value.fullPath
	// 保留当前标签，关闭其他所有标签
	const deleteTagList = appStore.tagsList.filter(tag => tag.fullPath !== currentPath)
	deleteCacheAndTag(deleteTagList)
	router.push(currentPath)
	closeTagMenu()
}

// 关闭所有标签
const closeAllTags = () => {
	deleteCacheAndTag(appStore.tagsList.filter(item => item.fullPath !== '/index'))
	closeTagMenu()
	// 回到首页
	if (route.fullPath !== '/index') {
		router.push('/index')
	}
}

// 关闭左侧标签
const closeLeftTag = () => {
	// 激活的标签索引
	const activeTagIndex = appStore.tagsList.findIndex(tag => tag.fullPath === route.fullPath)
	const deleteTagList = appStore.tagsList.filter((_, index) => index < currentMenuTagIndex.value)
	deleteCacheAndTag(deleteTagList)
	// 检查当前激活的标签是否在被关闭的标签中
	if (activeTagIndex < currentMenuTagIndex.value) {
		// 当前激活标签在被关闭范围内，切换到右键点击的标签
		router.push(currentMenuTag.value.fullPath)
	}
	closeTagMenu()
}

// 关闭右侧标签
const closeRightTag = () => {
	// 激活的标签索引
	const activeTagIndex = appStore.tagsList.findIndex(tag => tag.fullPath === route.fullPath)
	// 保留当前标签及其左侧所有标签
	const deleteTagList = appStore.tagsList.filter((_, index) => index > currentMenuTagIndex.value)
	deleteCacheAndTag(deleteTagList)
	// 检查当前激活的标签是否在被关闭的标签中
	if (activeTagIndex > currentMenuTagIndex.value) {
		// 当前激活标签在被关闭范围内，切换到右键点击的标签
		router.push(currentMenuTag.value.fullPath)
	}
	closeTagMenu()
}

// 删除缓存和标签
const deleteCacheAndTag = (tagList: NavbarTag[]) => {
	if (!tagList || tagList.length === 0) {
		return
	}
	const nameList = tagList.map(item => item.name)

	// 删除缓存
	appStore.removeCacheComponentList(nameList)
	appStore.removeIframeCacheList(nameList)

	// 删除标签
	appStore.removeTagList(tagList)
}

// 打开新窗口
const openNewWindow = () => {
	const resolve = router.resolve(currentMenuTag.value.fullPath)
	// 构造完整 URL
	const fullUrl = window.location.origin + resolve.href
	window.open(fullUrl, '_blank')
}

const { refreshPage } = usePageRefresher()
</script>

<style scoped>
.tag-scrollbar {
	max-height: 50px; /* 根据需要调整高度 */
	overflow: hidden;
	margin-bottom: 5px;
}
.tag-wrapper {
	/* 保持原有样式不变 */
	margin-right: 5px;
	display: inline-flex;
	align-items: center;
	gap: 8px;
	position: relative;
	white-space: nowrap;
	padding-right: 20px;
}

/* 使用深度选择器覆盖 el-tag 的默认样式 */
:deep(.tag-wrapper .el-tag) {
	cursor: pointer;
}
.tag-context-menu {
	position: fixed;
	align-items: center;
	background: white;
	border: 1px solid #ddd;
	z-index: 1000;
	min-width: 100px;
}
</style>
