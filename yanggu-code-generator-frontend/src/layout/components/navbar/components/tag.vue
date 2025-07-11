<template>
	<!-- 防止tag过多添加滚动条 -->
	<el-scrollbar ref="scrollbarRef">
		<div class="tag-wrapper">
			<!-- 标签栏 -->
			<el-tag
				v-for="(tag, index) in store.tagsListRef"
				:key="tag.fullPath"
				size="default"
				:effect="tag.fullPath === route.fullPath ? 'dark' : 'plain'"
				closable
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
import { appStore, Tag } from '@/store'
import { useRoute, useRouter } from 'vue-router'
import { onMounted, ref, onUnmounted, Ref, nextTick } from 'vue'
import TagMenu from '@/layout/components/navbar/components/tag-menu.vue'
import SvgIcon from '@/components/svg-icon/src/svg-icon.vue'
import Sortable from 'sortablejs'

const route = useRoute()
const router = useRouter()
const tagMenuVisible = ref(false)
const menuPosition = ref({
	left: '0px',
	top: '0px'
})
const currentMenuTag = ref<Tag>({ fullPath: '/index', title: '首页', icon: 'icon-home', name: 'Index' })
const currentMenuTagIndex: Ref<number> = ref(0)
const store = appStore()
// 添加滚动条容器的引用
const scrollbarRef = ref()

onMounted(() => {
	// 点击页面任意位置关闭右键菜单
	document.addEventListener('click', closeTagMenu)
})

// 实现标签页的拖拽效果
onMounted(() => {
	nextTick(() => {
		const el = document.querySelector('.tag-wrapper') as HTMLElement
		if (el) {
			const scrollbar = scrollbarRef.value
			let lastScrollTime = 0
			const scrollStep = 50 // 增大步长

			new Sortable(el, {
				animation: 200,
				scroll: true, // 保留原生滚动（可选，若自定义scrollFn可设为false）
				scrollSensitivity: 30,
				scrollSpeed: 100,
				scrollFn: function (deltaX, deltaY, elt) {
					console.log('scrolling...')
					const now = Date.now()
					if (now - lastScrollTime < 50) {
						return
					} // 放宽触发频率
					lastScrollTime = now

					if (!scrollbar || !scrollbar.wrap$) {
						return
					}

					// 修正方向：deltaX>0（向右拖）时，scrollLeft增加（视觉向右滚动）
					const direction = Math.sign(deltaX)
					const currentScroll = scrollbar.wrap$.scrollLeft
					const maxScroll = scrollbar.wrap$.scrollWidth - scrollbar.wrap$.clientWidth
					const newScroll = Math.max(0, Math.min(currentScroll + direction * scrollStep, maxScroll))

					if (newScroll !== currentScroll) {
						scrollbar.setScrollLeft(newScroll) // 确保setScrollLeft存在
					}
				},
				onEnd: evt => {
					const { oldIndex, newIndex } = evt
					if (oldIndex !== null && newIndex !== null && oldIndex !== newIndex) {
						const movedTag = store.tagsListRef.splice(oldIndex!, 1)[0]
						store.tagsListRef.splice(newIndex!, 0, movedTag)
					}
				}
			})
		} else {
			console.error('未找到.tag-wrapper元素')
		}
	})
})

// 组件卸载时移除事件监听
onUnmounted(() => {
	document.removeEventListener('click', closeTagMenu)
})

const handleClick = (_: number, tag: Tag) => {
	router.push(tag.fullPath)
}

const handleClose = (index: number, tag: Tag) => {
	store.removeTag(tag)
	// 判断当前标签页是否为当前路由
	if (tag.fullPath === route.fullPath) {
		let to = '/' // 默认跳转首页

		// 关闭后如果还有标签页
		if (store.tagLength > 0) {
			// 优先尝试右侧标签
			if (index < store.tagLength) {
				to = store.tagsListRef[index].fullPath
			} else if (index > 0) {
				// 右侧无标签时选择左侧
				to = store.tagsListRef[index - 1].fullPath
			}
		}
		router.push(to)
	}
}

// 显示标签右键菜单
const showTagMenu = (e: MouseEvent, tag: Tag, index: number) => {
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
	// 使用路由跳转实现刷新
	router.push({
		path: '/redirect' + currentMenuTag.value.fullPath,
		query: route.query
	})
	store.removeCacheComponent(currentMenuTag.value.name)
	closeTagMenu()
}

// 关闭当前标签
const closeCurrentTag = () => {
	handleClose(currentMenuTagIndex.value, currentMenuTag.value)
	closeTagMenu()
}

// 关闭其他标签
const closeOtherTags = () => {
	// 保留当前标签，关闭其他所有标签
	const currentPath = currentMenuTag.value.fullPath
	store.addAllTags(store.tagsListRef.filter(tag => tag.fullPath === currentPath))
	router.push(currentPath)
	closeTagMenu()
}

// 关闭所有标签
const closeAllTags = () => {
	// 保留首页，关闭其他所有标签
	store.removeAllTags()
	router.push('/')
	closeTagMenu()
}

//关闭左侧标签
const closeLeftTag = () => {
	const currentIndex = currentMenuTagIndex.value
	store.addAllTags(store.tagsListRef.filter((_, index) => index >= currentIndex))
	router.push(currentMenuTag.value.fullPath)
	closeTagMenu()
}

//关闭右侧标签
const closeRightTag = () => {
	// 保留当前标签及其左侧所有标签，以及首页标签
	const currentIndex = currentMenuTagIndex.value
	store.addAllTags(store.tagsListRef.filter((_, index) => index <= currentIndex))
	closeTagMenu()
}
</script>

<style scoped>
.tag-wrapper {
	/* 保持原有样式不变 */
	margin-top: 15px;
	margin-bottom: 15px;
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
