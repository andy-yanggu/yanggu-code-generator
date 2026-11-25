/**
 * keep-alive 缓存页的下拉数据加载 hook
 *
 * 业务菜单页默认被 keep-alive 缓存（路由 meta.cache 缺省为 true），页面切走再切回时组件不重建，
 * 只写 onMounted 加载的下拉数据会一直是首次进入时的旧快照。这里把 onMounted + onActivated 的
 * 组合收拢到一处：首次挂载加载一次，之后每次页面被重新激活再加载一次。
 *
 * @param loadOptions 加载函数（如拉取项目 / 数据源 / 模板组下拉数据）
 */
export const useOnActivatedRefresh = (loadOptions: () => void) => {
	// 首次进入（无缓存场景由此加载；有缓存场景后续激活由 onActivated 负责）
	onMounted(loadOptions)

	// 首次激活紧随 onMounted，跳过以避免重复请求
	let activatedOnce = false
	onActivated(() => {
		if (!activatedOnce) {
			activatedOnce = true
			return
		}
		loadOptions()
	})
}
