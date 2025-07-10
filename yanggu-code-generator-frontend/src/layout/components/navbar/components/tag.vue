<template>
	<!-- 标签栏 -->
	<div class="tag-wrapper">
		<el-tag
			v-for="(tag, index) in store.tagsListRef"
			:key="tag.fullPath"
			size="default"
			:effect="tag.fullPath === route.fullPath ? 'dark' : 'plain'"
			:closable="tag.fullPath != '/index'"
			@click="handleClick(index, tag)"
			@close="handleClose(index, tag)"
			@contextmenu.prevent="showTagMenu($event, tag, index)"
		>
			{{ tag.title }}
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
</template>

<script setup lang="ts">
import { appStore } from '@/store'
import { useRoute, useRouter } from 'vue-router'
import { onMounted, ref, onUnmounted, Ref } from 'vue'
import TagMenu from '@/layout/components/navbar/components/tag-menu.vue'

const route = useRoute()
const router = useRouter()
const tagMenuVisible = ref(false)
const menuPosition = ref({
	left: '0px',
	top: '0px'
})
const currentMenuTag = ref({ fullPath: '', name: '', title: '' })
const currentMenuTagIndex: Ref<number> = ref(0)
const store = appStore()

onMounted(() => {
	// 点击页面任意位置关闭右键菜单
	document.addEventListener('click', closeTagMenu)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
	document.removeEventListener('click', closeTagMenu)
})

const handleClick = (_: number, tag: { fullPath: string; name: string }) => {
	router.push(tag.fullPath)
}

const handleClose = (index: number, tag: { fullPath: string; name: string }) => {
	store.removeTag(tag)
	// 判断当前标签页是否为当前路由
	if (tag.fullPath === route.fullPath) {
		//跳转到前一个标签页
		let to
		if (index - 1 < 0) {
			to = '/index'
		} else {
			to = store.tagsListRef[index - 1].fullPath
		}
		router.push(to)
	}
}

// 显示标签右键菜单
const showTagMenu = (e: MouseEvent, tag: any, index: number) => {
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
	store.addAllTags(store.tagsListRef.filter(tag => tag.fullPath === currentPath || tag.fullPath === '/index'))
	// 如果当前标签不是首页，则跳转到当前标签
	if (currentPath !== '/index') {
		router.push(currentPath)
	}
	closeTagMenu()
}

// 关闭所有标签
const closeAllTags = () => {
	// 保留首页，关闭其他所有标签
	store.addAllTags(store.tagsListRef.filter(tag => tag.fullPath === '/index'))
	router.push('/index')
	closeTagMenu()
}

//关闭左侧标签
const closeLeftTag = () => {
	const currentIndex = currentMenuTagIndex.value
	store.addAllTags(store.tagsListRef.filter((tag, index) => index >= currentIndex || tag.fullPath === '/index'))
	router.push(currentMenuTag.value.fullPath)
	closeTagMenu()
}

//关闭右侧标签
const closeRightTag = () => {
	// 保留当前标签及其左侧所有标签，以及首页标签
	const currentIndex = currentMenuTagIndex.value
	store.addAllTags(store.tagsListRef.filter((tag, index) => index <= currentIndex || tag.fullPath === '/index'))
	// 如果当前标签不是首页，则跳转到当前标签
	if (currentMenuTag.value.fullPath !== '/index') {
		router.push(currentMenuTag.value.fullPath)
	}
	closeTagMenu()
}
</script>

<style scoped>
.tag-wrapper {
	margin-top: 15px;
	margin-bottom: 15px;
	display: flex;
	flex-wrap: wrap; /* 内容超出时自动换行 */
	align-items: center;
	gap: 8px; /* 标签之间的间距 */
	position: relative; /* 为右键菜单提供定位上下文 */
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
