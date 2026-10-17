import { App } from 'vue'
import { hasPermission } from '@/directive/permission'

const directive = (app: App<Element>) => {
	app.directive('auth', hasPermission)
	//如果有其它的指令可以继续添加
}

export default directive
