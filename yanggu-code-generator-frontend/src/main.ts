import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import App from '@/App.vue'
import { router } from '@/router'
import 'virtual:svg-icons-register'
import '@/icons/iconfont/modules'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css' // ✅ 引入暗黑模式 css
import '@/styles/index.scss'
import directive from '@/directive'
import { setupEcharts } from '@/components/echarts'

const app = createApp(App)

// 创建和注册pinia实例
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)

// 注册echarts
setupEcharts(app)

// 自定义指令
directive(app)

app.use(router)

// 挂载应用
app.mount('#app')
