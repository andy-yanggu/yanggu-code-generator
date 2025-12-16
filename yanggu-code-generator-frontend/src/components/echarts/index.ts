// ECharts core + 按需导入
import * as echarts from 'echarts/core'
import VueECharts from 'vue-echarts'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GraphicComponent, GridComponent, LegendComponent, TitleComponent, TooltipComponent } from 'echarts/components'
import { App } from 'vue'

// 注册（必须）：渲染器 + 图表 + 组件
echarts.use([CanvasRenderer, LineChart, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, GraphicComponent])

export const setupEcharts = (app: App<Element>) => {
	app.component('VChart', VueECharts) // 注册图表组件VChart
}
