<template>
	<!-- 搜索框 -->
	<div class="search-container">
		<el-tooltip :content="'菜单搜索'" effect="dark" placement="bottom" @click="searchState.visible = true">
			<el-icon :size="18" class="search-icon" @click="openSearch">
				<Search></Search>
			</el-icon>
		</el-tooltip>
		<el-dialog v-model="searchState.visible" placement="bottom" trigger="click" title="菜单搜索">
			<el-autocomplete
				ref="searchInputRef"
				v-model="searchState.keyword"
				placeholder="请输入菜单名称"
				clearable
				:fetch-suggestions="querySearch"
				:trigger-on-focus="true"
				@select="handleSelect"
			>
				<template #prefix>
					<el-icon>
						<Search></Search>
					</el-icon>
				</template>
				<template #default="{ item }">
					<div class="menu-item">
						<!-- 面包屑风格展示路径 -->
						<span v-for="(breadcrumbItem, index) in item.breadcrumbItemList" :key="breadcrumbItem" class="breadcrumb-item">
							<svg-icon :icon="breadcrumbItem.icon"></svg-icon>
							<el-text>{{ breadcrumbItem.title }}</el-text>
							<el-text v-if="index < item.breadcrumbItemList.length - 1">/</el-text>
						</span>
					</div>
				</template>
			</el-autocomplete>

			<template #footer>
				<div style="display: flex; gap: 10px">
					<div class="icon-text">
						<svg-icon icon="icon-enter" class="icon-button"></svg-icon>
						<el-text>确认</el-text>
					</div>
					<div class="icon-text">
						<svg-icon icon="icon-arrowup" class="icon-button"></svg-icon>
						<svg-icon icon="icon-arrowdown" class="icon-button"></svg-icon>
						<el-text>切换</el-text>
					</div>
					<div class="icon-text">
						<el-text class="icon-button">ESC</el-text>
						<el-text>关闭</el-text>
					</div>
				</div>
			</template>
		</el-dialog>
	</div>
</template>
<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'
import SvgIcon from '@/components/svg-icon/src/svg-icon.vue'
import { nextTick, reactive, ref } from 'vue'
import { MenuInfo, useUserStore } from '@/store/user-store'
import { useRouter } from 'vue-router'

defineOptions({
	name: 'MenuSearch'
})

interface BreadcrumbItem {
	// 标题
	title: string
	// 图标
	icon: string
}

interface SearchItem extends MenuInfo {
	breadcrumbItemList: BreadcrumbItem[]
}

const searchState = reactive({
	visible: false,
	keyword: '',
	searchItemList: [] as SearchItem[]
})
const searchInputRef = ref()
const userStore = useUserStore()
const router = useRouter()

// 打开搜索框
const openSearch = () => {
	searchState.visible = true
	searchState.keyword = ''

	// 将userStore.menuList进行平铺展开
	searchState.searchItemList = []
	userStore.menuList.forEach(menuItem => {
		buildMenuInfo([], menuItem)
	})

	nextTick(() => {
		if (searchInputRef.value) {
			searchInputRef.value.focus()
		}
	})
}

const buildMenuInfo = (parentBreadcrumb: BreadcrumbItem[], menuItem: MenuInfo) => {
	// 按钮不展示
	if (menuItem.meta.type === 2) {
		return
	}
	// 隐藏的菜单不显示
	if (menuItem.meta.hidden) {
		return
	}

	// 构建面包屑路径
	const currentBreadcrumb = [
		...parentBreadcrumb,
		{
			title: menuItem.meta.title,
			icon: menuItem.meta.icon
		} as BreadcrumbItem
	]

	// 如果是菜单类型时，添加到列表中
	if (menuItem.meta.type === 1) {
		const newMenuItem = {
			...menuItem,
			meta: { ...menuItem.meta }, // 深拷贝meta信息
			breadcrumbItemList: currentBreadcrumb
		}
		searchState.searchItemList.push(newMenuItem)
		return
	}

	// 递归处理子菜单
	if (menuItem.children && menuItem.children.length > 0) {
		menuItem.children.forEach(child => {
			buildMenuInfo(currentBreadcrumb, child)
		})
	}
}

// 处理搜索建议
const querySearch = (queryString: string, cb: Function) => {
	if (!queryString || !queryString.trim()) {
		cb(searchState.searchItemList)
		return
	}

	const keyword = queryString.toLowerCase()
	const results: SearchItem[] = []

	// 搜索菜单项
	searchState.searchItemList.forEach(menu => {
		// 检查面包屑中是否有匹配项
		const breadcrumbText = menu.breadcrumbItemList.flatMap(item => item.title).join('/')

		if (breadcrumbText.includes(keyword)) {
			results.push(menu)
		}
	})
	cb(results)
}

// 选择搜索结果
const handleSelect = (item: any) => {
	searchState.visible = false
	searchState.keyword = ''

	if (item.path) {
		router.push(item.path)
	}
}
</script>

<style scoped>
.search-container {
	display: flex;
	align-items: center;
}

.search-icon {
	cursor: pointer;
}

.menu-item {
	display: flex;
	align-items: center;
	gap: 10px;
}

.breadcrumb-item {
	display: inline-flex;
	align-items: center;
	gap: 5px;
}
.icon-text {
	display: flex;
	align-items: center;
	gap: 5px;
}
.icon-button {
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	padding: 4px;
	box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
	background: #ffffff;
	display: inline-block;
}
</style>
