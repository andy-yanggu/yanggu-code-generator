<template>
	<!-- 防止tag过多添加滚动条 -->
	<el-scrollbar ref="scrollbarRef" class="tag-scrollbar">
		<div class="tag-wrapper">
			<!-- 标签栏 -->
			<el-tag
				v-for="(tag, index) in appStore.tagList"
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
					<div style="display: inline-flex; align-items: center; gap: 5px">
						<svg-icon v-if="tag.icon && systemSettingStore.isOpenTagIcon" :icon="tag.icon" is-pointer></svg-icon>
						<el-tooltip :content="tag.title" :disabled="!tagTextRecord[tag.fullPath]" placement="top">
							<el-text :ref="el => (tagTextRefs[tag.fullPath] = el)" class="tag-text">
								{{ tag.title }}
							</el-text>
						</el-tooltip>
					</div>
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
import { nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import TagMenu from '@/layout/navbar/components/tag-menu.vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import Sortable from 'sortablejs'
import { usePageRefresher } from '@/hooks/use-refresh-current-page'
import { useSystemSettingStore } from '@/store/system-setting-store'

defineOptions({
	name: 'TagBar'
})

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
const tagRefs: Record<string, any> = reactive({})
const tagTextRecord: Record<string, boolean> = reactive({})
const tagTextRefs: Record<string, any> = reactive({})
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

// 滚动到指定标签
const scrollToTag = (fullPath: string) => {
	const tagEl = tagRefs[fullPath]

	if (!tagEl) {
		return
	}

	// 获取对应的dom元素
	const tagDom = tagEl.$el || tagEl.el || tagEl

	// 滚动到指定标签
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
						const newTags = [...appStore.tagList]
						const movedTag = newTags.splice(oldIndex!, 1)[0]
						newTags.splice(newIndex!, 0, movedTag)
						appStore.addAllTags(newTags)
						// 激活被移动的标签
						if (systemSettingStore.isOpenTagDragActivated) {
							if (route.fullPath != movedTag.fullPath) {
								router.push(movedTag.fullPath)
							}
						}
					}
				}
			})
		} else {
			console.error('未找到.tag-wrapper或scrollWrapper元素')
		}
	})
})

// 检测标签文字是否过长
onMounted(() => {
	nextTick(() => {
		Object.entries(tagTextRefs).forEach(([fullPath, el]) => {
			const tagTextElement = el.$el
			tagTextRecord[fullPath] = tagTextElement.scrollWidth > tagTextElement.offsetWidth
		})
	})
})

// 组件卸载时移除事件监听
onUnmounted(() => {
	document.removeEventListener('click', closeTagMenu)
})

// 处理点击标签
const handleClick = (_: number, tag: NavbarTag) => {
	if (tag.fullPath != route.fullPath) {
		router.push(tag.fullPath)
	}
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
	const deleteTagList = appStore.tagList.filter(tag => tag.fullPath !== currentPath)
	deleteCacheAndTag(deleteTagList)
	router.push(currentPath)
	closeTagMenu()
}

// 关闭所有标签
const closeAllTags = () => {
	deleteCacheAndTag(appStore.tagList.filter(item => item.fullPath !== '/index'))
	closeTagMenu()
	// 回到首页
	if (route.fullPath !== '/index') {
		router.push('/index')
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
		router.push(currentMenuTag.value.fullPath)
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
	appStore.removeTags(tagList)
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
	max-height: 41px;
	margin-bottom: 10px;
	overflow-x: auto;
	border-bottom: 1px solid var(--el-border-color);
}

.tag-wrapper {
	display: flex;
	align-items: center;
	margin-top: 7px;
	margin-left: 10px;
	margin-right: 10px;
	gap: 10px;
	flex-wrap: nowrap;
	width: max-content;
	padding-right: 5px;
	box-sizing: content-box;
}
.tag-text {
	max-width: 100px; /* 控制最大宽度 */
	display: inline-block;
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
	vertical-align: middle;
	color: inherit;
}

:deep(.el-scrollbar__wrap) {
	overflow-x: auto !important;
	overflow-y: hidden !important;
}

:deep(.tag-wrapper .el-tag) {
	position: relative;
	cursor: pointer;
}

/* 每个标签右侧加竖线 */
:deep(.tag-wrapper .el-tag::after) {
	content: '';
	position: absolute;
	right: -6px;
	top: 0;
	bottom: 0; /* ✅ 撑满整个高度 */
	width: 1px;
	background-color: var(--el-border-color);
}

/* 去掉第一个和最后一个标签的外侧分割线 */
:deep(.tag-wrapper .el-tag:last-child::after) {
	display: none;
}

.tag-context-menu {
	position: fixed;
	align-items: center;
	background: white;
	border: 1px solid #ddd;
	z-index: 1000;
	min-width: 100px;
}

/* 暗黑模式下的右键菜单样式 */
html.dark .tag-context-menu {
	background: #1e1e1e;
	border: 1px solid #3a3a3a;
}
</style>
