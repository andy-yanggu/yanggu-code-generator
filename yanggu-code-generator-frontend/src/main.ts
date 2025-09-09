import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import { dynamicPersistPlugin } from '@/plugins/dynamic-persist-plugin'
import ElementPlus from 'element-plus'
import App from '@/App.vue'
import { router } from '@/router'
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/svg-icon'
import '@/icons/iconfont/iconfont'
import 'element-plus/dist/index.css'
import '@/styles/index.scss'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import directive from '@/directive'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
pinia.use(dynamicPersistPlugin)
//使用element-plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
	app.component(key, component)
}
// 自定义指令
directive(app)

app.use(router)
app.use(SvgIcon)
app.use(pinia)
app.use(ElementPlus)
app.mount('#app')
