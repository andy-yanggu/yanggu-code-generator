<template>
	<!-- 防止tag过多添加滚动条 -->
	<el-scrollbar ref="scrollbarRef" class="tag-scrollbar">
		<div class="tag-wrapper">
			<!-- 标签栏 -->
			<el-tag
				v-for="(tag, index) in appStore.tagsListRef"
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
						<svg-icon :icon="tag.icon"></svg-icon>
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
				></tag-menu>
			</div>
		</div>
	</el-scrollbar>
</template>

<script setup lang="ts">
import { useAppStore, NavbarTag } from '@/store/app-store'
import { useRoute, useRouter } from 'vue-router'
import { onMounted, ref, onUnmounted, Ref, nextTick, watch } from 'vue'
import TagMenu from '@/layout/navbar/components/tag-menu.vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import Sortable from 'sortablejs'

const route = useRoute()
const router = useRouter()
const tagMenuVisible = ref(false)
const menuPosition = ref({
	left: '0px',
	top: '0px'
})
const currentMenuTag = ref<NavbarTag>({ fullPath: '/index', title: '首页', icon: 'icon-home', name: 'Index' })
const currentMenuTagIndex: Ref<number> = ref(0)
const appStore = useAppStore()
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

	const tagLeft = tagDom.offsetLeft
	const tagWidth = tagDom.offsetWidth
	const scrollLeft = scrollWrapper.scrollLeft
	const wrapperWidth = scrollWrapper.clientWidth

	if (tagLeft < scrollLeft) {
		scrollWrapper.scrollTo({
			left: tagLeft - 20,
			behavior: 'smooth'
		})
	} else if (tagLeft + tagWidth > scrollLeft + wrapperWidth) {
		scrollWrapper.scrollTo({
			left: tagLeft - wrapperWidth + tagWidth + 20,
			behavior: 'smooth'
		})
	}
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
						const newTags = [...appStore.tagsListRef]
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

const handleClick = (_: number, tag: NavbarTag) => {
	router.push(tag.fullPath)
}

const handleClose = (index: number, tag: NavbarTag) => {
	// 新增：首页保护逻辑（当只有一个标签且是首页时不允许关闭）
	if (appStore.tagLength === 1 && tag.fullPath === '/index') {
		return // 直接返回，不执行关闭操作
	}

	// 删除对应的缓存组件
	appStore.removeCacheComponent(tag.name)

	// 删除标签页
	appStore.removeTag(tag)
	// 判断当前标签页是否为当前路由
	if (tag.fullPath === route.fullPath) {
		let to = '/' // 默认跳转首页

		// 关闭后如果还有标签页
		if (appStore.tagLength > 0) {
			// 优先尝试右侧标签
			if (index < appStore.tagLength) {
				to = appStore.tagsListRef[index].fullPath
			} else if (index > 0) {
				// 右侧无标签时选择左侧
				to = appStore.tagsListRef[index - 1].fullPath
			}
		}
		if (to === '/') {
			router.push({
				path: '/redirect/'
			})
		} else {
			// 跳转当前标签页
			router.push(to)
		}
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
	// 删除缓存组件
	appStore.removeCacheComponent(currentMenuTag.value.name)
	// 使用路由跳转实现刷新
	router.push({
		path: '/redirect' + currentMenuTag.value.fullPath,
		query: route.query
	})
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
	// 删除其他缓存
	appStore.removeCacheComponentList(appStore.tagsListRef.filter(tag => tag.fullPath !== currentPath).map(tag => tag.name))
	// 保留当前标签，关闭其他所有标签
	appStore.addAllTags(appStore.tagsListRef.filter(tag => tag.fullPath === currentPath))
	router.push(currentPath)
	closeTagMenu()
}

// 关闭所有标签
const closeAllTags = () => {
	appStore.removeAllTags()
	appStore.removeAllCache()
	closeTagMenu()
	router.push({
		path: '/redirect/'
	})
}

//关闭左侧标签
const closeLeftTag = () => {
	const currentIndex = currentMenuTagIndex.value
	appStore.removeCacheComponentList(appStore.tagsListRef.filter((_, index) => index < currentIndex).map(tag => tag.name))
	appStore.addAllTags(appStore.tagsListRef.filter((_, index) => index >= currentIndex))
	router.push(currentMenuTag.value.fullPath)
	closeTagMenu()
}

//关闭右侧标签
const closeRightTag = () => {
	// 保留当前标签及其左侧所有标签，以及首页标签
	const currentIndex = currentMenuTagIndex.value
	appStore.removeCacheComponentList(appStore.tagsListRef.filter((_, index) => index > currentIndex).map(tag => tag.name))
	appStore.addAllTags(appStore.tagsListRef.filter((_, index) => index <= currentIndex))
	closeTagMenu()
}
</script>

<style scoped>
.tag-scrollbar {
	max-height: 50px; /* 根据需要调整高度 */
	overflow: hidden;
	margin-bottom: 5px;
}
.tag-wrapper {
	/* 保持原有样式不变 */
	margin-top: 10px;
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
