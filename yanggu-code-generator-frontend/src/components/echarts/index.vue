<template>
	<div class="base-chart">
		<!-- Loading 状态 -->
		<div v-if="loading" class="chart-loading">
			<span class="loader"></span>
		</div>

		<!-- 空数据状态 -->
		<div v-else-if="isEmpty" class="chart-empty">
			<slot name="empty">暂无数据</slot>
		</div>

		<!-- ECharts -->
		<v-chart v-else :option="option" :theme="theme" autoresize class="chart-box"></v-chart>
	</div>
</template>

<script setup lang="ts">
import type { ECBasicOption } from 'echarts/types/dist/shared'

const props = defineProps({
	option: {
		type: Object as PropType<ECBasicOption>,
		required: true
	},
	loading: {
		type: Boolean,
		default: false
	},
	theme: {
		type: String,
		default: 'light' // dark / light
	},
	emptyCheck: {
		type: Function as PropType<(opt: ECBasicOption) => boolean>,
		default: null // 用户可自定义判断方式
	}
})

// 是否为空数据（自动判断）
const isEmpty = computed(() => {
	if (props.emptyCheck) {
		return props.emptyCheck(props.option)
	}

	const series = props.option?.series
	if (!series || !Array.isArray(series) || series.length === 0) {
		return true
	}

	// 判断所有序列是否无数据
	return series.every(s => !s.data || s.data.length === 0)
})
</script>

<style scoped>
.base-chart {
	width: 100%;
	height: 100%;
	position: relative;
}

.chart-box {
	width: 100%;
	height: 100%;
}

.chart-loading,
.chart-empty {
	width: 100%;
	height: 100%;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #999;
	font-size: 14px;
}

.loader {
	width: 32px;
	height: 32px;
	border-radius: 50%;
	border: 4px solid #ddd;
	border-top-color: #409eff;
	animation: loading 0.8s linear infinite;
}

@keyframes loading {
	to {
		transform: rotate(360deg);
	}
}
</style>
