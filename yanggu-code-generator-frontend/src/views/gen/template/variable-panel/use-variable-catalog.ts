import { ref, watch, type Ref } from 'vue'
import { genTemplateApi } from '@/api'
import type { VariableNode } from '@/types'
import { resolveFailureReason } from './failure-reason'

// 按模板组缓存：切回看过的组不再请求
const catalogCache = new Map<number, VariableNode[]>()

/**
 * 变量目录加载：失败保留上一次成功目录，提示只落在面板内（规格 6.2）
 *
 * @param options.immediate 默认 true。宿主想在更晚的时机才首轮加载（如抽屉真正打开）时传 false，之后调用 ensureLoaded 启动
 */
export const useVariableCatalog = (templateGroupId: Ref<number>, options: { immediate?: boolean } = {}) => {
	const catalog = ref<VariableNode[]>([])
	const loading = ref(false)
	// 空串表示无异常
	const error = ref('')
	// 当前组是否已有一份可展示的目录（成功或命中缓存）：宿主据此决定收起态徽标显示数字还是留空
	const loaded = ref(false)
	// 本次实例的请求序号：快速切组时用于丢弃后到的旧响应
	let requestSeq = 0
	// immediate:false 时，宿主启动之前连切组也不自动加载，避免抽屉还没打开就发请求
	let started = options.immediate !== false

	const load = async (force = false) => {
		const groupId = templateGroupId.value
		// 递增必须早于所有早返分支：缓存命中、空组同样代表“上一次请求的显示结果已作废”
		const seq = ++requestSeq
		if (!groupId) {
			loading.value = false
			loaded.value = false
			return
		}
		const cached = catalogCache.get(groupId)
		if (cached && !force) {
			catalog.value = cached
			error.value = ''
			// 在飞请求的 finally 会被序号守卫拦下，故 loading 必须在此处自行收尾
			loading.value = false
			loaded.value = true
			return
		}
		// 新数据还没到位，先撤下可展示标记，避免宿主徽标停留在上一个组的数字
		loaded.value = false
		loading.value = true
		try {
			const data = await genTemplateApi.variableCatalog(groupId)
			const list = data ?? []
			// 缓存与是否为最新请求无关：这份数据对它的 groupId 始终有效
			catalogCache.set(groupId, list)
			if (seq !== requestSeq) {
				return
			}
			catalog.value = list
			error.value = ''
			loaded.value = true
		} catch (e: any) {
			if (seq !== requestSeq) {
				return
			}
			error.value = resolveFailureReason(e)
		} finally {
			if (seq === requestSeq) {
				loading.value = false
			}
		}
	}

	watch(templateGroupId, () => {
		if (started) {
			load()
		}
	})
	if (started) {
		load()
	}

	// 宿主驱动的启动点：缓存命中时不会发请求，重复调用无副作用
	const ensureLoaded = () => {
		started = true
		return load()
	}

	const reload = () => {
		started = true
		return load(true)
	}

	return { catalog, loading, error, loaded, reload, ensureLoaded }
}
