import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import App from './App.vue'
import { router } from './router'
import 'virtual:svg-icons-register'
import 'xe-utils'
import SvgIcon from '@/components/svg-icon'
import 'vxe-table/lib/style.css'
import '@/icons/iconfont/iconfont'
import 'element-plus/dist/index.css'
import '@/styles/index.scss'

const app = createApp(App)

app.use(router)
app.use(SvgIcon)
app.use(ElementPlus)
app.mount('#app')
