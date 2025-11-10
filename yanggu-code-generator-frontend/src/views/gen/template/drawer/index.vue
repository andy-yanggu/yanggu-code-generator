<template>
	<el-drawer v-model="drawerData.visible" :title="`${templateGroupName} - 模板配置`" :size="'100%'" :modal="false">
		<el-container style="height: 100%">
			<!-- 左侧：树结构 -->
			<el-aside v-show="!drawerData.isCollapse" width="400px" style="overflow: hidden"> </el-aside>
			<!-- 右侧：模板编辑 -->
			<el-container v-if="1 > 0" style="padding: 0; height: 100%" :class="{ 'full-screen-mode': isFullscreen }">
				<!-- 头部操作区域 -->
				<el-header> </el-header>

				<!-- 代码区域 -->
				<el-main style="padding: 10px; overflow: hidden"> </el-main>
			</el-container>
			<el-main v-else style="display: flex; flex-direction: column; height: 100%" :class="{ 'full-screen-mode': isFullscreen }"> </el-main>
		</el-container>
	</el-drawer>
</template>
<script setup lang="ts">
import { reactive } from 'vue'
import { useFullscreen } from '@vueuse/core'

const props = defineProps({
	templateGroupId: {
		type: Number,
		required: true
	},
	templateGroupName: {
		type: String,
		required: true
	},
	templateGroupType: {
		type: Number,
		required: true
	}
})

const drawerData = reactive({
	visible: false,
	isCollapse: false,
	activeItemId: -1
})

const init = () => {
	drawerData.visible = true
}

const { isFullscreen, toggle } = useFullscreen()

defineExpose({
	init
})
</script>

<style scoped></style>
