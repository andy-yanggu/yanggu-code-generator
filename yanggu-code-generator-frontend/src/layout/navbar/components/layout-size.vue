<template>
	<el-tooltip content="布局大小" effect="dark" placement="bottom">
		<el-dropdown trigger="click" @command="(size: LayoutSize) => appStore.setLayoutSize(size)">
			<svg-icon icon="icon-font-size" class="icon-button" size="18px" is-pointer></svg-icon>
			<template #dropdown>
				<el-dropdown-menu>
					<el-dropdown-item v-for="item of LAYOUT_SIZE_TYPES.items" :key="item.value" :disabled="appStore.layoutSize === item.value" :command="item.value">
						{{ item.label }}
					</el-dropdown-item>
				</el-dropdown-menu>
			</template>
		</el-dropdown>
	</el-tooltip>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/svg-icon/index.vue'
import { useAppStore } from '@/store'
import { LAYOUT_SIZE_TYPES, type LayoutSize } from '@/constant/enum'

defineOptions({
	name: 'LayoutSize'
})

const appStore = useAppStore()
</script>
