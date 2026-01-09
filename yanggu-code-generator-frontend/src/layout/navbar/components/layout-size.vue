<template>
	<div class="layout-size">
		<el-tooltip content="布局大小" effect="dark" placement="bottom">
			<el-dropdown trigger="click" @command="(size: LayOutSize) => appStore.setLayoutSize(size)">
				<svg-icon icon="icon-font-size" class="icon-button" size="18px" is-pointer></svg-icon>
				<template #dropdown>
					<el-dropdown-menu>
						<el-dropdown-item v-for="item of sizeOptions" :key="item.value" :disabled="appStore.layoutSize === item.value" :command="item.value">
							{{ item.label }}
						</el-dropdown-item>
					</el-dropdown-menu>
				</template>
			</el-dropdown>
		</el-tooltip>
	</div>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/svg-icon/index'
import { ref } from 'vue'
import { useAppStore } from '@/store'
import { LayOutSize } from '@/types'

defineOptions({
	name: 'LayoutSize'
})

const appStore = useAppStore()

const sizeOptions = ref([
	{ label: '较大', value: 'large' },
	{ label: '默认', value: 'default' },
	{ label: '稍小', value: 'small' }
])
</script>

<style scoped>
.layout-size {
	display: flex;
	align-items: center;
	cursor: pointer;
}
.icon-button:hover {
	color: var(--el-color-primary) !important;
	transform: scale(1.1) translateY(-1px);
}
</style>
