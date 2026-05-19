<template>
	<div>
		<el-affix :offset="100">
			<el-input v-model="searchText" placeholder="请输入图标名称" :prefix-icon="Search" clearable @input="debouncedFilterIcons()"></el-input>
		</el-affix>
		<div class="icon-grid">
			<el-row :gutter="20">
				<el-col v-for="iconName in allIcons.sort()" :key="iconName" :span="3" class="icon-col">
					<div class="icon-item" @click="selectIcon(iconName)">
						<svg-icon :icon="iconName" class="large-icon" is-pointer></svg-icon>
						<text-tooltip :title="iconName"></text-tooltip>
					</div>
				</el-col>
			</el-row>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SvgIcon from '@/components/svg-icon/index.vue'
import { ElMessage } from 'element-plus'
import { copyToClipboard } from '@/utils/tool'
import { Search } from '@element-plus/icons-vue'
import { useDebounceFn } from '@vueuse/core'
import TextTooltip from '@/components/text-tooltip/index.vue'

defineOptions({
	name: 'IconSearch'
})

const allIcons = ref<string[]>([])
const allOriginIcons = ref<string[]>([])
const searchText = ref('')

// 从iconfont.js文件中提取图标
const loadIcons = () => {
	// 查找所有已注入的图标
	const symbols = document.querySelectorAll('symbol[id^="icon-"]')
	const iconNames: string[] = []

	symbols.forEach(symbol => {
		const id = symbol.getAttribute('id')
		if (id) {
			iconNames.push(id)
		}
	})

	allOriginIcons.value = iconNames
	allIcons.value = iconNames
}

// 使用防抖优化搜索过滤
const debouncedFilterIcons = useDebounceFn(() => {
	if (searchText.value && searchText.value.trim()) {
		allIcons.value = allOriginIcons.value.filter(icon => icon.toLowerCase().includes(searchText.value.toLowerCase()))
	} else {
		allIcons.value = allOriginIcons.value
	}
}, 300) // 300ms 防抖延迟

// 选择图标时的回调
const selectIcon = (iconName: string) => {
	copyToClipboard(iconName).then(() => {
		// 可以添加成功提示
		ElMessage.success(`图标名称: ${iconName}已经复制到剪切板`)
	})
}

onMounted(() => {
	loadIcons()
})
</script>

<style scoped>
.icon-grid {
	margin-top: 10px;
	margin-bottom: 10px;
	padding: 10px;
}

.icon-col {
	margin-bottom: 20px;
	cursor: pointer;
}

.icon-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	border: 2px solid #ebeef5;
	border-radius: 8px;
	cursor: pointer;
	transition: all 0.3s;
	height: 100px; /* 增加高度以容纳图标和文字 */
	background-color: var(--el-bg-color-overlay, #ffffff);
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.icon-item:hover {
	border-color: var(--el-color-primary);
	transform: translateY(-4px);
}
:deep(.icon-item:hover .text-tooltip-title) {
	color: var(--el-color-primary);
}
.large-icon {
	font-size: 40px;
	color: var(--el-text-color-primary);
	transition: all 0.3s;
	margin-bottom: 10px;
}

.icon-item:hover .large-icon {
	color: var(--el-color-primary);
	transform: scale(1.1);
}
</style>
