<template>
	<el-tabs ref="tagWrapperRef" v-model="userStore.activeMenuPath" type="card" @tab-click="handleTabClick" @tab-remove="handleTabRemove">
		<!-- 标签 -->
		<el-tab-pane
			v-for="tag in appStore.tagList"
			:key="tag.fullPath"
			:name="tag.fullPath"
			:closable="tag.fullPath != defaultMenu || appStore.tagLength > 1"
		>
			<template #label>
				<icon-text-tooltip :enable-icon="systemSettingStore.tag.isOpenTagIcon" :icon="tag.icon" :title="tag.title"></icon-text-tooltip>
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
		></tag-menu>
	</div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import TagMenu from '@/layout/navbar/components/tag-menu.vue'
import Sortable from 'sortablejs'
import { usePageRefresher } from '@/hooks'
import { useAppStore, useSystemSettingStore, useUserStore } from '@/store'
import { NavbarTag } from '@/types'
import IconTextTooltip from '@/components/icon-text-tooltip/index.vue'
import { TabsPaneContext } from 'element-plus'

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
const systemSettingStore = useSystemSettingStore()
const userStore = useUserStore()
const tagWrapperRef = ref()

onMounted(() => {
	// 点击页面任意位置关闭右键菜单
	document.addEventListener('click', closeTagMenu)
})

// 默认菜单
const defaultMenu = computed(() => systemSettingStore.menu.menuDefault)

// 实现标签页的拖拽效果
onMounted(() => {
	nextTick(() => {
		const tabsWrapper = tagWrapperRef.value.$el.querySelector('.el-tabs__nav') as HTMLElement
		if (!tabsWrapper) {
			return
		}
		new Sortable(tabsWrapper, {
			animation: 200,
			handle: '.el-tabs__item', // 拖拽手柄
			onEnd: evt => {
				const oldIndex = evt.oldIndex!
				const newIndex = evt.newIndex!
				// console.log('oldIndex:', oldIndex, 'newIndex:', newIndex)
				if (oldIndex !== null && newIndex !== null && oldIndex !== newIndex) {
					const newTags = [...appStore.tagList]
					const movedTag = newTags.splice(oldIndex!, 1)[0]
					newTags.splice(newIndex!, 0, movedTag)
					appStore.addAllTags(newTags)
					// 激活被移动的标签
					if (systemSettingStore.tag.isOpenTagDragActivated) {
						if (route.fullPath != movedTag.fullPath) {
							router.push(movedTag.fullPath)
						}
					}
				}
			}
		})
	})
})

onMounted(() => {
	nextTick(() => {
		const nav = tagWrapperRef.value.$el.querySelector('.el-tabs__nav')
		if (!nav) {
			return
		}

		nav.addEventListener('contextmenu', (e: MouseEvent) => {
			const item = (e.target as HTMLElement).closest('.el-tabs__item')
			if (!item) {
				return
			}

			const index = Array.from(nav.children).indexOf(item)
			const tag = appStore.tagList[index]

			showTagMenu(e, tag, index)
		})
	})
})

// 组件卸载时移除事件监听
onUnmounted(() => {
	document.removeEventListener('click', closeTagMenu)
})

// 处理点击标签
const handleTabClick = (tab: TabsPaneContext, _: Event) => {
	const fullPath = tab.paneName as string
	if (fullPath != route.fullPath) {
		router.push(fullPath)
	}
}

// 处理标签删除
const handleTabRemove = (fullPath: string) => {
	const index = appStore.tagList.findIndex(tag => tag.fullPath === fullPath)
	handleClose(index, appStore.tagList[index])
}

// 处理关闭单个标签
const handleClose = (index: number, tag: NavbarTag) => {
	// 新增：首页保护逻辑（当只有一个标签且是首页时不允许关闭）
	if (appStore.tagLength === 1 && tag.fullPath === defaultMenu.value) {
		return // 直接返回，不执行关闭操作
	}

	// 删除对应的缓存组件和标签
	deleteCacheAndTag([tag])

	// 判断当前标签页是否为当前路由
	if (tag.fullPath === route.fullPath) {
		let to = defaultMenu.value // 默认跳转首页

		// 关闭后如果还有标签页
		if (appStore.tagLength > 0) {
			// 优先尝试右侧标签
			if (index < appStore.tagLength) {
				to = appStore.tagList[index].fullPath
			} else if (index > 0) {
				// 右侧无标签时选择左侧
				to = appStore.tagList[index - 1].fullPath
			}
		}
		// 跳转到对应路由
		router.push(to)
	}
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

// 关闭标签菜单
const closeTagMenu = () => {
	tagMenuVisible.value = false
}

// 刷新当前标签页
const refreshCurrentTag = () => {
	if (route.fullPath != currentMenuTag.fullPath) {
		router.push(currentMenuTag.fullPath)
		setTimeout(() => refreshPage(), 10)
	} else {
		refreshPage()
	}
	closeTagMenu()
}

// 关闭当前标签
const closeCurrentTag = () => {
	handleClose(currentMenuTagIndex.value, currentMenuTag)
	closeTagMenu()
}

// 关闭其他标签
const closeOtherTags = () => {
	const currentPath = currentMenuTag.fullPath
	// 保留当前标签，关闭其他所有标签
	const deleteTagList = appStore.tagList.filter(tag => tag.fullPath !== currentPath)
	deleteCacheAndTag(deleteTagList)
	router.push(currentPath)
	closeTagMenu()
}

// 关闭所有标签
const closeAllTags = () => {
	deleteCacheAndTag(appStore.tagList.filter(item => item.fullPath !== defaultMenu.value))
	closeTagMenu()
	// 回到首页
	if (route.fullPath !== defaultMenu.value) {
		router.push(defaultMenu.value)
	}
}

// 关闭左侧标签
const closeLeftTag = () => {
	// 激活的标签索引
	const activeTagIndex = appStore.tagList.findIndex(tag => tag.fullPath === route.fullPath)
	const deleteTagList = appStore.tagList.filter((_, index) => index < currentMenuTagIndex.value)
	deleteCacheAndTag(deleteTagList)
	// 检查当前激活的标签是否在被关闭的标签中
	if (activeTagIndex < currentMenuTagIndex.value) {
		// 当前激活标签在被关闭范围内，切换到右键点击的标签
		router.push(currentMenuTag.fullPath)
	}
	closeTagMenu()
}

// 关闭右侧标签
const closeRightTag = () => {
	// 激活的标签索引
	const activeTagIndex = appStore.tagList.findIndex(tag => tag.fullPath === route.fullPath)
	// 保留当前标签及其左侧所有标签
	const deleteTagList = appStore.tagList.filter((_, index) => index > currentMenuTagIndex.value)
	deleteCacheAndTag(deleteTagList)
	// 检查当前激活的标签是否在被关闭的标签中
	if (activeTagIndex > currentMenuTagIndex.value) {
		// 当前激活标签在被关闭范围内，切换到右键点击的标签
		router.push(currentMenuTag.fullPath)
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
	appStore.removeTags(tagList)
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
}

const { refreshPage } = usePageRefresher()
</script>

<style scoped>
:deep(.el-tabs__header) {
	margin-bottom: 0;
}

.tag-context-menu {
	position: fixed;
	align-items: center;
	background: var(--el-tag-bg-color);
	border: 1px solid var(--el-border-color);
	z-index: 1000;
	min-width: 100px;
}
</style>
