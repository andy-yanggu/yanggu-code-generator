<template>
	<el-tabs ref="tagWrapperRef" v-model="tagStore.activeTabPath" type="card" @tab-click="handleTabClick" @tab-remove="handleTabRemove">
		<!-- 标签 -->
		<el-tab-pane
			v-for="tag in tagStore.tagList"
			:key="tag.fullPath"
			:name="tag.fullPath"
			:closable="!tag.pinned && (tag.fullPath != defaultMenu || tagStore.tagLength > 1)">
			<template #label>
				<icon-text-tooltip :enable-icon="systemSettingStore.tag.isOpenTagIcon" :icon="tag.icon" :title="tag.title"></icon-text-tooltip>
				<!-- 固定标签显示 pin icon -->
				<svg-icon v-if="tag.pinned" icon="icon-pin" class="pin-icon" @click.stop="togglePin(tag)"></svg-icon>
			</template>
		</el-tab-pane>
	</el-tabs>

	<!-- 右键菜单 -->
	<div v-if="tagMenuVisible" class="tag-context-menu" :style="menuPosition">
		<tag-menu
			:default-menu="defaultMenu"
			:current-menu-tag="currentMenuTag"
			:current-menu-tag-index="currentMenuTagIndex"
			@close-current-tag="closeCurrentTag()"
			@refresh-current-tag="refreshCurrentTag()"
			@close-other-tags="closeOtherTags()"
			@close-left-tag="closeLeftTag()"
			@close-right-tag="closeRightTag()"
			@close-all-tags="closeAllTags()"
			@open-new-window="openNewWindow()"
			@full-screen="fullScreen()"
			@toggle-pin="togglePin(currentMenuTag)"></tag-menu>
	</div>
</template>

<script setup lang="ts">
import TagMenu from '@/layout/navbar/components/tag-menu.vue'
import Sortable from 'sortablejs'
import { usePageRefresher } from '@/hooks'
import { useAppStore, useCacheStore, useSystemSettingStore, useTagStore } from '@/store'
import { NavbarTag } from '@/types'
import IconTextTooltip from '@/components/icon-text-tooltip/index.vue'
import { TabPaneName, TabsPaneContext } from 'element-plus'
import { isEmpty, isNotBlank } from '@/utils/tool'
import SvgIcon from '@/components/svg-icon/index.vue'

defineOptions({
	name: 'TagBar'
})

const route = useRoute()
const router = useRouter()
const tagMenuVisible = ref(false)
const menuPosition = reactive({
	left: '0px',
	top: '0px'
})
const currentMenuTag = reactive({} as NavbarTag)
const currentMenuTagIndex = ref(0)
const appStore = useAppStore()
const tagStore = useTagStore()
const cacheStore = useCacheStore()
const systemSettingStore = useSystemSettingStore()
const tagWrapperRef = ref()

watch(
	() => route.fullPath,
	() => (tagStore.activeTabPath = route.fullPath),
	{ immediate: true }
)

// 默认菜单
const defaultMenu = computed(() => systemSettingStore.menu.menuDefault)

// 标签页元素
const tabNavEl = computed(() => tagWrapperRef.value?.tabNavRef?.tabListRef)

const draggedTagPath = ref('')
const sortableInstance = ref<Sortable | null>(null)

const tabActive = useDebounceFn(() => {
	// console.log('判断函数被执行了', draggedTagPath.value)
	if (isNotBlank(draggedTagPath.value)) {
		router.push(draggedTagPath.value)
		draggedTagPath.value = ''
	}
}, 500)

// 初始化标签页拖拽
const initSortable = () => {
	const el = tabNavEl.value
	if (!el) {
		return
	}

	// 先销毁旧实例，避免重复绑定
	if (sortableInstance.value) {
		sortableInstance.value.destroy()
		sortableInstance.value = null
	}

	sortableInstance.value = new Sortable(el, {
		animation: 200,
		handle: '.el-tabs__item',

		onStart: evt => {
			const items = Array.from(evt.from.querySelectorAll('.el-tabs__item'))

			const draggedIndex = items.indexOf(evt.item)
			if (draggedIndex === -1) {
				return
			}

			const fullPath = evt.item.id.replace(/^tab-/, '')

			// 激活被拖拽的标签页
			if (systemSettingStore.tag.isOpenTagDragActivated && tagStore.activeTabPath !== fullPath) {
				// console.log('item:', evt.item.id, 'fullPath', fullPath)
				draggedTagPath.value = fullPath
			}
		},

		// 固定标签之间拖拽，非固定标签页之前拖拽，不能互相拖拽
		onMove: evt => {
			const items = Array.from(evt.from.querySelectorAll('.el-tabs__item'))

			const draggedIndex = items.indexOf(evt.dragged)
			const targetIndex = items.indexOf(evt.related)
			const tags = tagStore.tagList
			const draggedTag = tags[draggedIndex]

			const pinnedCount = tags.filter(t => t.pinned).length

			// 固定 → 禁止拖出固定区
			if (draggedTag.pinned && targetIndex >= pinnedCount) {
				return false
			}

			// 普通 → 禁止拖入固定区
			if (!draggedTag.pinned && targetIndex < pinnedCount) {
				return false
			}

			tabActive()

			return true
		},

		onEnd: evt => {
			const oldIndex = evt.oldIndex!
			const newIndex = evt.newIndex!

			if (oldIndex === newIndex) {
				return
			}

			const tags = [...tagStore.tagList]
			const movedTag = tags.splice(oldIndex, 1)[0]
			tags.splice(newIndex, 0, movedTag)

			tagStore.addAllTags(tagStore.sortTags(tags))
		}
	})
}

// 监听 tabNavEl 变化，在 DOM 就绪后初始化拖拽
watch(
	tabNavEl,
	el => {
		if (el) {
			nextTick(() => initSortable())
		}
	},
	{ immediate: true }
)

onUnmounted(() => {
	sortableInstance.value?.destroy()
	sortableInstance.value = null
})

// tab右键菜单
const onTabContextMenu = (e: MouseEvent) => {
	const item = (e.target as HTMLElement).closest('.el-tabs__item') as HTMLElement
	if (!item) {
		return
	}

	e.preventDefault()

	const fullPath = item.id.replace(/^tab-/, '')
	const index = tagStore.tagList.findIndex(t => t.fullPath === fullPath)
	if (index === -1) {
		return
	}

	showTagMenu(e, tagStore.tagList[index], index)
}

// 显示标签右键菜单
const showTagMenu = (e: MouseEvent, tag: NavbarTag, index: number) => {
	e.preventDefault()
	Object.assign(currentMenuTag, tag)
	currentMenuTagIndex.value = index

	const x = e.clientX
	const y = e.clientY

	const menuWidth = 80
	const menuHeight = 200
	const windowWidth = window.innerWidth
	const windowHeight = window.innerHeight

	Object.assign(menuPosition, {
		left: `${Math.min(x, windowWidth - menuWidth)}px`,
		top: `${Math.min(y, windowHeight - menuHeight)}px`
	})

	tagMenuVisible.value = true
}

useEventListener(tabNavEl, 'contextmenu', onTabContextMenu)

// 关闭标签菜单
const closeTagMenu = () => {
	tagMenuVisible.value = false
}

// 点击页面任意位置关闭右键菜单
useEventListener(document, 'click', closeTagMenu)

// 处理点击标签
const handleTabClick = (tab: TabsPaneContext, _: Event) => {
	const fullPath = tab.paneName as string
	if (fullPath != route.fullPath) {
		router.push(fullPath)
	}
}

// 统一关闭入口
const canCloseTag = (tag: NavbarTag) => {
	// 首页保护
	if (tagStore.tagLength === 1 && tag.fullPath === defaultMenu.value) {
		return false
	}

	// 固定标签不能被关闭（必须先取消固定）
	return !tag.pinned
}

// 计算删除后的跳转目标
const resolveNextRouteAfterClose = (index: number, removedTag: NavbarTag) => {
	// 如果删的不是当前激活页，不需要跳转
	if (removedTag.fullPath !== route.fullPath) {
		return null
	}

	const tags = tagStore.tagList
	const lastIndex = tags.length - 1

	// 优先右侧
	if (index < lastIndex) {
		return tags[index + 1]?.fullPath
	}

	// 其次左侧
	if (index > 0) {
		return tags[index - 1]?.fullPath
	}

	// 兜底：首页
	return defaultMenu.value
}

// 统一删除函数
const closeTags = (tagsToClose: NavbarTag[]) => {
	if (isEmpty(tagsToClose)) {
		return
	}

	// 过滤掉不能关闭的标签
	const closableTags = tagsToClose.filter(canCloseTag)
	if (isEmpty(closableTags)) {
		return
	}

	const nameList = closableTags.map(tag => tag.name)

	// 删除缓存
	cacheStore.removeCacheComponentList(nameList)
	cacheStore.removeIframeCacheList(nameList)

	// 删除标签
	tagStore.removeTags(closableTags)
}

// 关闭单个标签
const closeSingleTag = (tag: NavbarTag) => {
	if (!canCloseTag(tag)) {
		return
	}

	const index = tagStore.tagList.findIndex(t => t.fullPath === tag.fullPath)
	if (index === -1) {
		return
	}

	const nextRoute = resolveNextRouteAfterClose(index, tag)

	closeTags([tag])

	if (nextRoute) {
		router.push(nextRoute)
	}
}

// 处理标签删除
// (name: TabPaneName) => name is string | number;
const handleTabRemove = (fullPath: TabPaneName) => {
	const tag = tagStore.tagList.find(t => t.fullPath === fullPath)
	if (tag) {
		closeSingleTag(tag)
	}
}

// 刷新当前标签页
const refreshCurrentTag = async () => {
	if (route.fullPath !== currentMenuTag.fullPath) {
		try {
			await router.push(currentMenuTag.fullPath)
		} catch {
			// 导航被取消或失败，不执行刷新
			closeTagMenu()
			return
		}
	}
	refreshPage()
	closeTagMenu()
}

// 关闭当前标签
const closeCurrentTag = () => {
	closeSingleTag(currentMenuTag)
	closeTagMenu()
}

// 关闭其他标签
const closeOtherTags = () => {
	const currentPath = currentMenuTag.fullPath
	const deleteTags = tagStore.tagList.filter(tag => tag.fullPath !== currentPath)
	closeTags(deleteTags)
	router.push(currentPath)
	closeTagMenu()
}

// 关闭所有标签
const closeAllTags = () => {
	const deleteTags = tagStore.tagList.filter(tag => tag.fullPath !== defaultMenu.value)

	closeTags(deleteTags)

	if (route.fullPath !== defaultMenu.value) {
		router.push(defaultMenu.value)
	}

	closeTagMenu()
}

// 关闭左侧标签
const closeLeftTag = () => {
	const deleteTags = tagStore.tagList.filter((_, index) => index < currentMenuTagIndex.value)

	closeTags(deleteTags)

	// 如果当前激活页被关了，切换到右测标签
	if (!tagStore.tagList.some(tag => tag.fullPath === route.fullPath)) {
		router.push(currentMenuTag.fullPath)
	}
}

// 关闭右侧标签
const closeRightTag = () => {
	const deleteTags = tagStore.tagList.filter((_, index) => index > currentMenuTagIndex.value)

	closeTags(deleteTags)

	if (!tagStore.tagList.some(tag => tag.fullPath === route.fullPath)) {
		router.push(currentMenuTag.fullPath)
	}
	closeTagMenu()
}

// 打开新窗口
const openNewWindow = () => {
	const resolve = router.resolve(currentMenuTag.fullPath)
	// 构造完整 URL
	const fullUrl = window.location.origin + resolve.href
	window.open(fullUrl, '_blank')
}

// 窗口最大化
const fullScreen = () => {
	appStore.toolFullscreen()
	ElMessage.info('右上角点击退出全屏')
}

// 切换固定
const togglePin = (tag: NavbarTag) => {
	tagStore.togglePinTag(tag)
}

const { refreshPage } = usePageRefresher()
</script>

<style scoped>
:deep(.el-tabs__header) {
	margin-bottom: 0;
}
/* Pin 图标基础样式 */
.pin-icon {
	border-radius: 50%; /* 圆形背景 */
	text-align: center;
	transition: all 0.3s ease; /* 背景和颜色过渡 */
	margin-left: 5px;
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	color: inherit;
}
:deep(.el-tabs__item:hover .icon-text-tooltip .svg-icon) {
	color: var(--el-color-primary) !important;
	transform: scale(var(--icon-hover-transform-scale)) translateY(var(--icon-hover-transform-translate-y));
}

/* Hover 高亮样式 */
.pin-icon:hover {
	color: var(--el-bg-color, #ffffff);
	background-color: var(--el-text-color-secondary, rgba(168, 171, 178, 0.96));
}

.tag-context-menu {
	position: fixed;
	align-items: center;
	background: var(--el-bg-color-overlay, #ffffff);
	border: 1px solid var(--el-border-color-light, #e4e7ed);
	border-radius: 8px;
	box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
	z-index: 1000;
	min-width: 100px;
}
</style>
