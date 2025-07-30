<template>
	<el-input v-model="searchText" placeholder="请输入图标名称" clearable @input="filterIcons()"></el-input>
	<div class="icon-grid">
		<el-row :gutter="20">
			<el-col v-for="iconName in allIcons" :key="iconName" :span="3" class="icon-col">
				<div class="icon-item" @click="selectIcon(iconName)">
					<div class="icon-wrapper">
						<svg-icon :icon="iconName" class="large-icon"></svg-icon>
						<el-text size="small" truncated>{{ iconName }}</el-text>
					</div>
				</div>
			</el-col>
		</el-row>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import SvgIcon from '@/components/svg-icon/src/svg-icon.vue'
import { useClipboard } from '@vueuse/core'
import { ElMessage } from 'element-plus'

const loading = ref(true)
const allIcons = ref<string[]>([])
const allOriginIcons = ref<string[]>([])
const searchText = ref('')
const { copy, isSupported } = useClipboard()

// 从iconfont.js文件中提取图标
const loadIcons = async () => {
	try {
		// 使用setTimeout确保iconfont.js已经加载完成
		setTimeout(() => {
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
			loading.value = false
		}, 100)
	} catch (error) {
		console.error('提取图标失败:', error)
		loading.value = false
	}
}

// 根据搜索文本过滤图标
const filterIcons = () => {
	if (!searchText.value) {
		allIcons.value = allOriginIcons.value
	} else {
		allIcons.value = allOriginIcons.value.filter(icon => icon.toLowerCase().includes(searchText.value.toLowerCase()))
	}
}

// 选择图标时的回调
const selectIcon = (iconName: string) => {
	console.log('Selected icon:', iconName)
	// 检查浏览器是否支持 clipboard API
	if (isSupported) {
		copy(iconName)
			.then(() => {
				console.log('图标名称已复制到剪切板:', iconName)
				// 可以添加成功提示
				ElMessage.success(`已复制图标: ${iconName}`)
			})
			.catch(err => {
				console.error('复制失败:', err)
				// 可以添加失败提示
				ElMessage.error('复制失败')
			})
	} else {
		// 不支持时的降级处理
		console.warn('当前浏览器不支持 Clipboard API')
		// 可以使用传统的复制方法或提示用户
		ElMessage.warning('当前浏览器不支持自动复制功能')
	}
}

onMounted(() => {
	loadIcons()
})
</script>

<style scoped>
.icon-grid {
	margin-top: 20px;
}

.icon-col {
	margin-bottom: 20px;
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
	background-color: #fff;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.icon-item:hover {
	border-color: #409eff;
	background-color: #ecf5ff;
	transform: translateY(-4px);
	box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
}

.icon-wrapper {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	width: 100%;
	height: 100%;
}

.large-icon {
	font-size: 36px;
	color: #606266;
	transition: all 0.3s;
	margin-bottom: 10px;
}

.icon-item:hover .large-icon {
	color: #409eff;
	transform: scale(1.1);
}
</style>
