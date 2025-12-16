<template>
	<div>
		<div style="width: 100%; height: 360px">
			<base-chart :option="options" :loading="loading" :theme="theme"></base-chart>
		</div>
		<div style="width: 100%; height: 360px">
			<base-chart :option="barOptions" :loading="loading" :theme="theme"></base-chart>
		</div>
		<div style="width: 100%; height: 360px">
			<base-chart :option="lineOptions" :loading="loading" :theme="theme"></base-chart>
		</div>
		<div style="width: 100%; height: 360px">
			<base-chart :option="pieOptions" :loading="loading" :theme="theme"></base-chart>
		</div>
		<div style="width: 100%; height: 360px">
			<base-chart :option="donutOptions" :loading="loading" :theme="theme"></base-chart>
		</div>
		<div style="width: 100%; height: 360px">
			<base-chart :option="stackBarOptions" :loading="loading" :theme="theme"></base-chart>
		</div>
		<div style="width: 100%; height: 360px">
			<base-chart :option="multiLineOptions" :loading="loading" :theme="theme"></base-chart>
		</div>
		<div style="width: 100%; height: 360px">
			<base-chart :option="groupBarOptions" :loading="loading" :theme="theme"></base-chart>
		</div>
		<div style="width: 100%; height: 360px">
			<base-chart :option="stackBarOptions" :loading="loading" :theme="theme"></base-chart>
		</div>
	</div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import BaseChart from '@/components/echarts/index.vue'
import type { EChartsOption } from 'echarts/types/dist/shared.d.ts'

const loading = ref(false)
const theme = ref('light') // dark / light

const options = reactive({
	title: {
		text: '销售统计',
		subtext: '按日汇总',
		left: 'center',
		textStyle: { color: '#333', fontSize: 16 }
	},
	tooltip: {},
	xAxis: { data: ['A', 'B', 'C'] },
	yAxis: {},
	series: [{ type: 'bar', data: [5, 20, 36] }]
} as EChartsOption)

const barOptions = reactive({
	title: { text: '商品销量柱状图' },
	tooltip: {},
	xAxis: {
		type: 'category',
		data: ['手机', '电脑', '耳机', '鼠标', '键盘']
	},
	yAxis: { type: 'value' },
	series: [
		{
			type: 'bar',
			data: [120, 200, 150, 80, 70],
			barWidth: 30
		}
	]
} as EChartsOption)

const lineOptions = reactive({
	title: { text: '近7日访问量折线图' },
	tooltip: { trigger: 'axis' },
	xAxis: {
		type: 'category',
		data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
	},
	yAxis: { type: 'value' },
	series: [
		{
			type: 'line',
			data: [150, 230, 124, 300, 260, 190, 280],
			smooth: true
		}
	]
} as EChartsOption)

const pieOptions = reactive({
	title: { text: '用户来源占比', left: 'center' },
	tooltip: { trigger: 'item' },
	legend: { bottom: 0 },
	series: [
		{
			name: '来源',
			type: 'pie',
			radius: '60%',
			data: [
				{ value: 40, name: '搜索引擎' },
				{ value: 20, name: '社交媒体' },
				{ value: 25, name: '外链跳转' },
				{ value: 15, name: '直接访问' }
			]
		}
	]
} as EChartsOption)

const data = [
	{ value: 335, name: '待支付' },
	{ value: 310, name: '已支付' },
	{ value: 234, name: '配送中' },
	{ value: 135, name: '已完成' },
	{ value: 154, name: '退款中' }
]

const total = data.reduce((sum, item) => sum + item.value, 0)

const donutOptions = reactive({
	tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
	legend: { bottom: 0 },
	series: [
		{
			name: '订单状态',
			type: 'pie',
			radius: ['40%', '70%'],
			avoidLabelOverlap: false,
			label: { show: true, formatter: '{d}%' },
			labelLine: { show: true },
			data
		}
	],
	graphic: [
		{
			type: 'text',
			left: 'center',
			top: 'center',
			style: {
				text: `${total}`,
				textAlign: 'center',
				fill: '#409EFF',
				fontSize: 24,
				fontWeight: 'bold'
			}
		}
	]
} as EChartsOption)

const stackBarOptions = reactive({
	title: { text: '季度销售额（堆叠）' },
	tooltip: { trigger: 'axis' },
	legend: {},
	xAxis: { type: 'category', data: ['Q1', 'Q2', 'Q3', 'Q4'] },
	yAxis: { type: 'value' },
	series: [
		{
			name: '线上',
			type: 'bar',
			stack: 'total',
			data: [120, 132, 101, 134]
		},
		{
			name: '线下',
			type: 'bar',
			stack: 'total',
			data: [220, 182, 191, 234]
		}
	]
} as EChartsOption)

const multiLineOptions = reactive({
	title: { text: '温度变化对比' },
	tooltip: { trigger: 'axis' },
	legend: {},
	xAxis: { type: 'category', data: ['1时', '2时', '3时', '4时', '5时'] },
	yAxis: {},
	series: [
		{
			name: '室内',
			type: 'line',
			smooth: true,
			data: [20, 22, 21, 23, 24]
		},
		{
			name: '室外',
			type: 'line',
			smooth: true,
			data: [10, 12, 11, 13, 14]
		}
	]
} as EChartsOption)

const groupBarOptions = reactive({
	title: { text: '品类销量对比' },
	tooltip: { trigger: 'axis' },
	legend: {},
	xAxis: { type: 'category', data: ['手机', '电脑', '耳机'] },
	yAxis: {},
	series: [
		{
			name: '2024',
			type: 'bar',
			barGap: 0,
			data: [120, 110, 90]
		},
		{
			name: '2025',
			type: 'bar',
			data: [140, 100, 70]
		}
	]
} as EChartsOption)

// 模拟加载
loading.value = true
setTimeout(() => (loading.value = false), 1500)
</script>
