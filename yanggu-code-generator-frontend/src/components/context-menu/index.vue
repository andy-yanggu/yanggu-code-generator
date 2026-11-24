<template>
	<Teleport to="body">
		<div v-if="visible" ref="menuRef" class="context-menu" :style="rootStyle">
			<!-- Layer 2：提供 items 则内置渲染 ul/li -->
			<ul v-if="items && items.length" class="context-menu__list">
				<template v-for="item in items" :key="item.key">
					<el-divider v-if="item.dividerBefore && isVisible(item)" class="context-menu__divider"></el-divider>
					<li v-if="isVisible(item)" :class="['context-menu__item', { 'is-disabled': isDisabled(item) }]" @click="handleClick(item)">
						<slot :name="item.key" :item="item" :target="target">
							<el-icon v-if="componentIcon(item)" size="10">
								<component :is="componentIcon(item)"></component>
							</el-icon>
							<svg-icon v-else-if="isStringIcon(iconOf(item))" :icon="iconName(item)" size="10px"></svg-icon>
							<span>{{ resolveLabel(item) }}</span>
						</slot>
					</li>
				</template>
			</ul>
			<!-- Layer 1：不提供 items 时透出自渲染插槽，作用域内给出当前 target -->
			<slot v-else :target="target"></slot>
		</div>
	</Teleport>
</template>

<script setup lang="ts">
import type { Component, CSSProperties } from 'vue'
import { computeMenuPosition } from '@/utils/context-menu'
import SvgIcon from '@/components/svg-icon/index.vue'
import { useContextMenu } from './use-context-menu'
import type { ContextMenuItem, IconSpec } from './types'

defineOptions({
	name: 'ContextMenu'
})

defineProps<{
	items?: ContextMenuItem[]
}>()

const emit = defineEmits<{
	(e: 'closed'): void
}>()

const { visible, target, position, open: openMenu, close: closeMenu } = useContextMenu()

const menuRef = ref<HTMLElement>()
const pos = ref({ left: 0, top: 0 })
// 内容渲染并实测落位前保持 hidden，避免首帧在错误坐标闪跳
const ready = ref(false)

const rootStyle = computed<CSSProperties>(() => ({
	left: `${pos.value.left}px`,
	top: `${pos.value.top}px`,
	visibility: ready.value ? 'visible' : 'hidden'
}))

// 静态值或 (target) => 值 的动态求值统一入口
const resolve = <R>(dyn: R | ((t: any) => R)): R => (typeof dyn === 'function' ? (dyn as (t: any) => R)(target.value) : dyn)

const isVisible = (item: ContextMenuItem): boolean => (item.visible ? item.visible(target.value) : true)
const isDisabled = (item: ContextMenuItem): boolean => (item.disabled ? item.disabled(target.value) : false)
const resolveLabel = (item: ContextMenuItem): string => resolve(item.label)
const iconOf = (item: ContextMenuItem): IconSpec | undefined => (item.icon === undefined ? undefined : resolve(item.icon))

const isStringIcon = (icon: IconSpec | undefined): icon is string => typeof icon === 'string'
const componentIcon = (item: ContextMenuItem): Component | undefined => {
	const icon = iconOf(item)
	return icon && !isStringIcon(icon) ? icon : undefined
}
const iconName = (item: ContextMenuItem): string => {
	const icon = iconOf(item)
	return isStringIcon(icon) ? icon : ''
}

const handleClick = (item: ContextMenuItem): void => {
	if (isDisabled(item)) {
		return
	}
	item.onClick(target.value)
	closeMenu()
}

const measure = (): void => {
	const el = menuRef.value
	if (!el) {
		return
	}
	pos.value = computeMenuPosition(
		{ x: position.value.x, y: position.value.y },
		{ width: el.offsetWidth, height: el.offsetHeight },
		{ width: window.innerWidth, height: window.innerHeight }
	)
	ready.value = true
}

watch(
	visible,
	async value => {
		if (value) {
			ready.value = false
			await nextTick()
			measure()
		} else {
			emit('closed')
		}
	},
	{ flush: 'post' }
)

// 点击外部/Esc 关闭：用本实例 $el 判定，不用全局类名 querySelector（根治 v1 双菜单串扰）
const onPointerDown = (e: PointerEvent): void => {
	if (!visible.value) {
		return
	}
	const el = menuRef.value
	if (el && !el.contains(e.target as Node)) {
		closeMenu()
	}
}
const onKeyDown = (e: KeyboardEvent): void => {
	if (e.key === 'Escape' && visible.value) {
		closeMenu()
	}
}
document.addEventListener('pointerdown', onPointerDown, true)
document.addEventListener('keydown', onKeyDown)
// 监听器随组件实例卸载，根治 v1 顶层 addEventListener 泄漏
onScopeDispose(() => {
	document.removeEventListener('pointerdown', onPointerDown, true)
	document.removeEventListener('keydown', onKeyDown)
})

defineExpose({
	open: openMenu,
	close: closeMenu
})
</script>

<style scoped>
/* 配色一律走 EP 语义变量：明暗两套由 html.dark 的变量取值自行切换，无需手写 dark 分支 */
.context-menu {
	position: fixed;
	background: var(--el-bg-color-overlay);
	border: 1px solid var(--el-border-color);
	/* 黑色半透明阴影明暗通用，无主题耦合，故保持字面量 */
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
	border-radius: 4px;
	padding: 5px;
	/* 菜单可能在 el-drawer 内触发，抽屉已由 popup manager 递增到 2000+，故必须高于 var(--el-index-popper) */
	z-index: 9999;
	min-width: 85px;
	max-width: 200px;
	white-space: nowrap;
	/* 翻上来仍嫌高时的最后防线：限高内部滚动（margin 8 → 2*8=16px） */
	max-height: calc(100vh - 16px);
	overflow-y: auto;
}

.context-menu__list {
	list-style: none;
	margin: 0;
	padding: 0;
}

.context-menu__item {
	display: flex;
	align-items: center;
	gap: 5px;
	font-size: 12px;
	cursor: pointer;
	padding: 5px 10px;
	border-radius: 4px;
	text-align: left;
	transition:
		background-color 0.2s ease,
		color 0.2s ease;
}

.context-menu__item:hover {
	/* 中性灰 hover：取填充色阶的 dark 档，明暗两套分别落到 #ebedf0 / #39393A */
	background-color: var(--el-fill-color-dark);
	color: var(--el-text-color-primary);
}

.context-menu__item.is-disabled {
	cursor: not-allowed;
	color: var(--el-disabled-text-color);
}

.context-menu__item.is-disabled:hover {
	background-color: transparent;
	color: var(--el-disabled-text-color);
}

.context-menu__divider {
	margin: 4px 0;
}
</style>
