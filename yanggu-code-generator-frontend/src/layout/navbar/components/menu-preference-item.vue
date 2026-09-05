<template>
	<div class="preference-item">
		<!-- 菜单标题（可点击折叠/展开） -->
		<div class="item-header" @click="expanded = !expanded">
			<el-icon class="expand-arrow" :class="{ 'is-expanded': expanded }"><CaretRight /></el-icon>
			<icon-text-tooltip :icon="icon ?? ''" :title="title" :max-width="'300px'"></icon-text-tooltip>
			<el-tag v-if="hasAnyModified" size="small" type="warning" class="header-modified-tag">已修改</el-tag>
		</div>
		<!-- 三个开关（可折叠） -->
		<el-collapse-transition>
			<div v-show="expanded" class="item-toggles">
				<div class="toggle-row">
					<div class="toggle-label">
						<el-text size="small">页面缓存</el-text>
						<el-tag v-if="isModified('cache')" size="small" type="warning" class="modified-tag">已修改</el-tag>
						<el-text size="small" type="info" class="default-hint">默认: {{ serverCache ? '开' : '关' }}</el-text>
					</div>
					<el-switch v-model="effectiveCache" inline-prompt active-text="开" inactive-text="关" @change="onCacheChange"></el-switch>
				</div>
				<div class="toggle-row">
					<div class="toggle-label">
						<el-text size="small">显示在菜单</el-text>
						<el-tag v-if="isModified('hideMenu')" size="small" type="warning" class="modified-tag">已修改</el-tag>
						<el-text size="small" type="info" class="default-hint">默认: {{ serverHideMenu ? '关' : '开' }}</el-text>
					</div>
					<el-switch v-model="showMenu" inline-prompt active-text="开" inactive-text="关"></el-switch>
				</div>
				<div class="toggle-row">
					<div class="toggle-label">
						<el-text size="small">显示在标签栏</el-text>
						<el-tag v-if="isModified('hideTab')" size="small" type="warning" class="modified-tag">已修改</el-tag>
						<el-text size="small" type="info" class="default-hint">默认: {{ serverHideTab ? '关' : '开' }}</el-text>
					</div>
					<el-switch v-model="showTab" inline-prompt active-text="开" inactive-text="关"></el-switch>
				</div>
			</div>
		</el-collapse-transition>
	</div>
</template>

<script setup lang="ts">
import { CaretRight } from '@element-plus/icons-vue'
import { useMenuPreferenceStore } from '@/store'
import IconTextTooltip from '@/components/icon-text-tooltip/index.vue'

defineOptions({
	name: 'MenuPreferenceItem'
})

const props = defineProps({
	path: {
		type: String,
		required: true
	},
	title: {
		type: String,
		required: true
	},
	icon: {
		type: String,
		default: ''
	},
	serverCache: {
		type: Boolean,
		default: false
	},
	serverHideMenu: {
		type: Boolean,
		default: false
	},
	serverHideTab: {
		type: Boolean,
		default: false
	}
})

const menuPreferenceStore = useMenuPreferenceStore()

// 折叠状态（默认折叠）
const expanded = ref(false)

// 判断某个字段的有效值是否与服务端默认值不同
const isModified = (field: 'cache' | 'hideMenu' | 'hideTab'): boolean => {
	switch (field) {
		case 'cache':
			return menuPreferenceStore.getEffectiveCache(props.path, props.serverCache) !== props.serverCache
		case 'hideMenu':
			return menuPreferenceStore.getEffectiveHideMenu(props.path, props.serverHideMenu) !== props.serverHideMenu
		case 'hideTab':
			return menuPreferenceStore.getEffectiveHideTab(props.path, props.serverHideTab) !== props.serverHideTab
	}
}

// 是否有任何已修改的偏好
const hasAnyModified = computed(() => isModified('cache') || isModified('hideMenu') || isModified('hideTab'))

// 有效值（用户偏好 > 服务端默认）
const effectiveCache = ref(menuPreferenceStore.getEffectiveCache(props.path, props.serverCache))

// 显示在菜单/标签栏（反转 hideMenu/hideTab，让开关语义与标签一致：ON=显示）
const showMenu = computed({
	get: () => !menuPreferenceStore.getEffectiveHideMenu(props.path, props.serverHideMenu),
	set: (val: boolean) => menuPreferenceStore.setPreference(props.path, { hideMenu: !val })
})

const showTab = computed({
	get: () => !menuPreferenceStore.getEffectiveHideTab(props.path, props.serverHideTab),
	set: (val: boolean) => menuPreferenceStore.setPreference(props.path, { hideTab: !val })
})

// 监听 store 变化（其他组件可能修改了同一个偏好）
watch(
	() => menuPreferenceStore.preferenceMap,
	() => {
		effectiveCache.value = menuPreferenceStore.getEffectiveCache(props.path, props.serverCache)
	},
	{ deep: true }
)

// 缓存开关变化处理
const onCacheChange = (val: string | number | boolean) => {
	menuPreferenceStore.setPreference(props.path, { cache: val as boolean })
}
</script>

<style scoped>
.preference-item {
	padding: 10px 12px;
	margin-bottom: 8px;
	border: 1px solid var(--el-border-color-lighter);
	border-radius: 6px;
	background-color: var(--el-bg-color);
}

.item-header {
	display: flex;
	align-items: center;
	gap: 6px;
	cursor: pointer;
	padding: 2px 0;
	user-select: none;
}

.item-header:hover {
	color: var(--el-color-primary);
}

.expand-arrow {
	font-size: 12px;
	transition: transform 0.2s ease;
	color: var(--el-text-color-secondary);
}

.expand-arrow.is-expanded {
	transform: rotate(90deg);
}

.header-modified-tag {
	margin-left: auto;
	transform: scale(0.8);
	transform-origin: right center;
}

.item-toggles {
	display: flex;
	flex-direction: column;
	gap: 6px;
	margin-top: 8px;
	padding-top: 6px;
	border-top: 1px solid var(--el-border-color-extra-light);
}

.toggle-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.toggle-label {
	display: flex;
	align-items: center;
	gap: 4px;
	flex: 1;
	min-width: 0;
}

.default-hint {
	font-size: 11px;
}

.modified-tag {
	transform: scale(0.85);
	transform-origin: left center;
}

:deep(.el-switch) {
	--el-switch-on-color: var(--el-color-primary);
	--el-switch-off-color: var(--el-color-danger);
}
</style>
