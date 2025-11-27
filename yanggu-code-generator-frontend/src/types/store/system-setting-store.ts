// 菜单相关设置
export interface MenuSetting {
	// 是否开启logo
	isOpenLogo: boolean
	// 是否开启菜单折叠动画
	isOpenMenuCollapseAnimation: boolean
	// 是否开启手风琴模式（同一时间只允许一个子菜单展开）
	isOpenMenuUniqueOpened: boolean
	// 菜单展开/折叠按钮
	isOpenMenuCollapseButton: boolean
	// 菜单展开宽度
	menuExpandWidth: number
	// 菜单折叠宽度
	menuFoldWidth: number
	// 默认菜单
	menuDefault: string
}

// 工具栏相关设置
export interface ToolbarSetting {
	// 菜单展开/折叠按钮
	isOpenMenuCollapseButton: boolean
	// 是否开启菜单搜索
	isOpenMenuSearch: boolean
	// 是否开启刷新页面
	isOpenRefreshPage: boolean
	// 布局大小
	isOpenLayoutSetting: boolean
	// 切换全屏
	isOpenFullscreen: boolean
	// 主题切换（日间/夜间）
	isOpenThemeSwitch: boolean
	// 是否开启面包屑
	isOpenBreadcrumb: boolean
	// 面包屑是否显示图标
	isOpenBreadcrumbIcon: boolean
}

// 标签页相关设置
export interface TagSetting {
	// 是否开启标签页
	isOpenTag: boolean
	// 标签页是否显示图标
	isOpenTagIcon: boolean
	// 标签页是否持久化
	isOpenTagCache: boolean
	// 拖拽后是否激活标签页
	isOpenTagDragActivated: boolean
}

// 其他设置
export interface OtherSetting {
	// 是否开启动态标题
	isOpenDynamicTitle: boolean
	// 是否开启载入进度条
	isOpenProgress: boolean
	// 是否开启页面缓存
	isOpenPageCache: boolean
}
