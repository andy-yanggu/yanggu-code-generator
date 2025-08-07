import { App } from 'vue'
import { hasPermission } from '@/directive/permission'

export default function directive(app: App<Element>) {
	app.directive('auth', hasPermission)
	//如果有其它的指令可以继续添加
}
