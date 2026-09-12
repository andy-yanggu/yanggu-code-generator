<template>
	<div class="preference-item">
		<!-- 菜单标题（可点击折叠/展开） -->
		<div class="item-header" @click="expanded = !expanded">
			<template v-if="editing">
				<el-icon class="expand-arrow"><CaretRight /></el-icon>
				<el-input
					v-model="editingTitle"
					placeholder="请输入自定义标题"
					clearable
					size="small"
					class="item-edit-input"
					@click.stop
					@keydown.enter="saveEdit()"
					@keydown.escape="cancelEdit()"></el-input>
				<div class="item-edit-btns">
					<el-button type="primary" :icon="Check" link size="small" @click.stop="saveEdit()"></el-button>
					<el-button :icon="Close" link size="small" @click.stop="cancelEdit()"></el-button>
				</div>
			</template>
			<template v-else>
				<el-icon class="expand-arrow" :class="{ 'is-expanded': expanded }"><CaretRight /></el-icon>
				<icon-text-tooltip :icon="icon ?? ''" :title="title" :max-width="'300px'"></icon-text-tooltip>
				<template v-if="customTitle !== title">
					<el-text type="info">→</el-text>
					<el-text type="primary">{{ customTitle }}</el-text>
				</template>
				<el-tooltip content="修改标题" placement="top">
					<el-button :icon="Edit" type="primary" link size="small" class="item-edit-btn" @click.stop="startEdit()"></el-button>
				</el-tooltip>
				<el-tag v-if="hasAnyModified" size="small" type="warning" class="header-modified-tag">已修改</el-tag>
			</template>
		</div>
		<!-- 三个开关（可折叠） -->
		<el-collapse-transition>
			<div v-show="expanded" class="item-toggles">
				<div class="toggle-row">
					<div class="toggle-label">
						<el-text size="small">页面缓存</el-text>
						<el-tag v-if="isFieldModified(effectiveCache, serverCache)" size="small" type="warning" class="modified-tag">已修改</el-tag>
						<el-text size="small" type="info" class="default-hint">默认: {{ serverCache ? '开' : '关' }}</el-text>
					</div>
					<el-switch v-model="effectiveCache" inline-prompt active-text="开" inactive-text="关"></el-switch>
				</div>
				<div class="toggle-row">
					<div class="toggle-label">
						<el-text size="small">显示在菜单</el-text>
						<el-tag v-if="isFieldModified(showMenu, !serverHideMenu)" size="small" type="warning" class="modified-tag">已修改</el-tag>
						<el-text size="small" type="info" class="default-hint">默认: {{ serverHideMenu ? '关' : '开' }}</el-text>
					</div>
					<el-switch v-model="showMenu" inline-prompt active-text="开" inactive-text="关"></el-switch>
				</div>
				<div class="toggle-row">
					<div class="toggle-label">
						<el-text size="small">显示在标签栏</el-text>
						<el-tag v-if="isFieldModified(showTab, !serverHideTab)" size="small" type="warning" class="modified-tag">已修改</el-tag>
						<el-text size="small" type="info" class="default-hint">默认: {{ serverHideTab ? '关' : '开' }}</el-text>
					</div>
					<el-switch v-model="showTab" inline-prompt active-text="开" inactive-text="关"></el-switch>
				</div>
			</div>
		</el-collapse-transition>
	</div>
</template>

<script setup lang="ts">
import { CaretRight, Check, Close, Edit } from '@element-plus/icons-vue'
import { useMenuPreferenceStore } from '@/store'
import { MenuPreferenceItem } from '@/types'
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

// 单一数据源：当前路径的原始偏好
const preference = computed(() => menuPreferenceStore.getPreference(props.path))

// 统一写入路径：所有字段（包括 title）都走 syncPreference
const saveField = (field: keyof MenuPreferenceItem, value: any, serverDefault: any) => {
	menuPreferenceStore.syncPreference(props.path, { [field]: value }, { [field]: serverDefault })
}

// 统一判断：有效值是否与服务端默认值不同
const isFieldModified = (effectiveValue: any, serverDefault: any): boolean => {
	return effectiveValue !== serverDefault
}

// 重命名
const customTitle = computed({
	get: () => preference.value?.title || props.title,
	set: (val: string) => saveField('title', val.trim() !== props.title ? val.trim() || undefined : undefined, props.title)
})

// 编辑状态
const editing = ref(false)
const editingTitle = ref('')

const startEdit = () => {
	editing.value = true
	editingTitle.value = customTitle.value
}

const saveEdit = () => {
	const trimmed = editingTitle.value.trim()
	saveField('title', trimmed !== props.title ? trimmed || undefined : undefined, props.title)
	editing.value = false
}

const cancelEdit = () => {
	editing.value = false
}

// 是否有任何已修改的偏好
const hasAnyModified = computed(
	() =>
		isFieldModified(customTitle.value, props.title) ||
		isFieldModified(effectiveCache.value, props.serverCache) ||
		isFieldModified(showMenu.value, !props.serverHideMenu) ||
		isFieldModified(showTab.value, !props.serverHideTab)
)

// 有效值（用户偏好 > 服务端默认）
const effectiveCache = computed({
	get: () => preference.value?.cache ?? props.serverCache,
	set: (val: boolean) => saveField('cache', val, props.serverCache)
})

// 显示在菜单/标签栏（反转 hideMenu/hideTab，让开关语义与标签一致：ON=显示）
const showMenu = computed({
	get: () => !(preference.value?.hideMenu ?? props.serverHideMenu),
	set: (val: boolean) => saveField('hideMenu', !val, props.serverHideMenu)
})

const showTab = computed({
	get: () => !(preference.value?.hideTab ?? props.serverHideTab),
	set: (val: boolean) => saveField('hideTab', !val, props.serverHideTab)
})
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

.item-edit-input {
	width: 160px;
}

.item-edit-btns {
	display: flex;
	gap: 2px;
}

.item-edit-btn {
	opacity: 0;
	transition: opacity 0.2s;
}

.item-header:hover .item-edit-btn {
	opacity: 1;
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
