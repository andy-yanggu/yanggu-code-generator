<template>
	<el-form-item>
		<el-link type="primary" underline="never" @click="showMore = !showMore">
			{{ showMore ? '收起' : '展开' }}
			<el-icon style="margin-left: 5px">
				<ArrowUp v-if="showMore"></ArrowUp>
				<ArrowDown v-else></ArrowDown>
			</el-icon>
		</el-link>
	</el-form-item>
</template>

<script setup lang="ts">
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'

const showMore = defineModel('showMore', {
	type: Boolean,
	default: false
})
</script>
