import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick, ref } from 'vue'
import type { VariableNode } from '@/types'
import { useVariableCatalog } from './use-variable-catalog'

const { mocks } = vi.hoisted(() => ({ mocks: { variableCatalog: vi.fn() } }))
vi.mock('@/api', () => ({ genTemplateApi: mocks }))

// 模块级缓存在整个文件内共享，故每个用例占用不同的 groupId 区段，互不污染
const nodeOf = (name: string): VariableNode => ({
	name,
	path: name,
	javaType: 'String',
	kind: 'SCALAR',
	desc: '',
	loopVar: '',
	nullable: false,
	children: []
})

/** 让 watcher（pre flush）与被测的异步 load 都跑完 */
const flush = async () => {
	await nextTick()
	await new Promise(resolve => setTimeout(resolve, 0))
}

describe('useVariableCatalog', () => {
	beforeEach(() => {
		mocks.variableCatalog.mockReset()
	})

	it('已加载过的模板组命中缓存，切回不再发请求', async () => {
		mocks.variableCatalog.mockImplementation((groupId: number) => Promise.resolve([nodeOf(`g${groupId}`)]))
		const groupId = ref(9001)
		const { catalog } = useVariableCatalog(groupId)
		await flush()
		expect(mocks.variableCatalog).toHaveBeenCalledTimes(1)

		groupId.value = 9002
		await flush()
		expect(mocks.variableCatalog).toHaveBeenCalledTimes(2)

		groupId.value = 9001
		await flush()
		expect(mocks.variableCatalog).toHaveBeenCalledTimes(2)
		expect(catalog.value[0]?.name).toBe('g9001')
	})

	it('templateGroupId 为 0 时不请求，也不置 loading', async () => {
		const groupId = ref(0)
		const { loading } = useVariableCatalog(groupId)
		await flush()
		expect(mocks.variableCatalog).not.toHaveBeenCalled()
		expect(loading.value).toBe(false)
	})

	it('reload 强制绕过缓存并采用新数据', async () => {
		let callCount = 0
		mocks.variableCatalog.mockImplementation(() => Promise.resolve(callCount++ === 0 ? [nodeOf('first')] : [nodeOf('first'), nodeOf('second')]))
		const groupId = ref(9101)
		const { catalog, reload } = useVariableCatalog(groupId)
		await flush()
		expect(catalog.value).toHaveLength(1)

		reload()
		await flush()
		expect(mocks.variableCatalog).toHaveBeenCalledTimes(2)
		expect(catalog.value).toHaveLength(2)
	})

	it('加载失败保留上一次成功目录、面板内给出业务原因并结束 loading', async () => {
		mocks.variableCatalog.mockResolvedValueOnce([nodeOf('kept')])
		const groupId = ref(9201)
		const { catalog, loading, error, reload } = useVariableCatalog(groupId)
		await flush()
		expect(error.value).toBe('')

		// request.ts 对业务错误 reject 的是 { code, message } 而非 Error 实例
		mocks.variableCatalog.mockRejectedValueOnce({ code: 700, message: '模板组不存在' })
		reload()
		await flush()
		expect(error.value).toBe('模板组不存在')
		expect(catalog.value[0]?.name).toBe('kept')
		expect(loading.value).toBe(false)
	})

	it('网络类错误不把 axios 原文给用户，改为友好原因', async () => {
		const groupId = ref(9301)
		mocks.variableCatalog.mockRejectedValueOnce(new Error('Request failed with status code 500'))
		const { error } = useVariableCatalog(groupId)
		await flush()
		expect(error.value).toBe('变量目录加载失败，请检查网络后重试')
		expect(error.value).not.toContain('500')
	})

	it('请求超时给出超时与重试原因', async () => {
		const groupId = ref(9302)
		mocks.variableCatalog.mockRejectedValueOnce(Object.assign(new Error('timeout of 1000ms exceeded'), { code: 'ECONNABORTED' }))
		const { error } = useVariableCatalog(groupId)
		await flush()
		expect(error.value).toBe('请求超时，请稍后重试')
	})

	it('快速切组时后到的旧响应不覆盖当前组目录', async () => {
		const groupId = ref(9401)
		let resolveStale: (list: VariableNode[]) => void = () => {}
		mocks.variableCatalog.mockImplementation((id: number) => {
			if (id === 9401) {
				return new Promise<VariableNode[]>(resolve => {
					resolveStale = resolve
				})
			}
			return Promise.resolve([nodeOf('current')])
		})
		const { catalog } = useVariableCatalog(groupId)
		await nextTick()

		groupId.value = 9402
		await flush()
		expect(catalog.value[0]?.name).toBe('current')

		// 旧组请求此刻才返回，不得把目录换回 9401
		resolveStale([nodeOf('stale')])
		await flush()
		expect(catalog.value[0]?.name).toBe('current')
		// 旧组数据仍写入缓存，切回去时不必重新请求
		groupId.value = 9401
		await flush()
		expect(mocks.variableCatalog).toHaveBeenCalledTimes(2)
		expect(catalog.value[0]?.name).toBe('stale')
	})

	it('接口返回空响应时降级为空目录而非 undefined', async () => {
		mocks.variableCatalog.mockResolvedValueOnce(undefined as any)
		const groupId = ref(9501)
		const { catalog, error } = useVariableCatalog(groupId)
		await flush()
		expect(catalog.value).toEqual([])
		expect(error.value).toBe('')
	})

	// 评审 I1：序号递增原先在缓存分支之后，导致“缓存命中切回”不会作废仍在飞的旧请求
	it('缓存命中切组会结束 loading，且旧组的后到成功响应不覆盖当前目录', async () => {
		const groupId = ref(9601)
		mocks.variableCatalog.mockImplementationOnce((id: number) => Promise.resolve([nodeOf(`cached-${id}`)]))
		const { catalog, error, loading } = useVariableCatalog(groupId)
		await flush()
		expect(catalog.value[0]?.name).toBe('cached-9601')

		let resolvePending: (list: VariableNode[]) => void = () => {}
		mocks.variableCatalog.mockImplementationOnce(
			() =>
				new Promise<VariableNode[]>(resolve => {
					resolvePending = resolve
				})
		)
		groupId.value = 9602
		await flush()
		expect(loading.value).toBe(true)

		// 切回已缓存的 9601：显示归属立即切换，loading 自行收尾
		groupId.value = 9601
		await flush()
		expect(loading.value).toBe(false)
		expect(catalog.value[0]?.name).toBe('cached-9601')

		resolvePending([nodeOf('late-9602')])
		await flush()
		expect(catalog.value[0]?.name).toBe('cached-9601')
		expect(error.value).toBe('')
		// 后到的结果仍按自己的 groupId 落缓存，切回去不必重新请求
		groupId.value = 9602
		await flush()
		expect(catalog.value[0]?.name).toBe('late-9602')
		expect(mocks.variableCatalog).toHaveBeenCalledTimes(2)
	})

	it('旧组的后到失败不给当前组写 error', async () => {
		const groupId = ref(9701)
		mocks.variableCatalog.mockResolvedValueOnce([nodeOf('ok-9701')])
		const { error } = useVariableCatalog(groupId)
		await flush()

		let rejectPending: (e: unknown) => void = () => {}
		mocks.variableCatalog.mockImplementationOnce(
			() =>
				new Promise<VariableNode[]>((_resolve, reject) => {
					rejectPending = reject
				})
		)
		groupId.value = 9702
		await flush()
		groupId.value = 9701
		await flush()

		rejectPending(new Error('旧组加载失败'))
		await flush()
		expect(error.value).toBe('')
	})

	it('旧请求后到不会提前结束当前请求的 loading', async () => {
		const groupId = ref(9801)
		const pending: ((list: VariableNode[]) => void)[] = []
		mocks.variableCatalog.mockImplementation(
			() =>
				new Promise<VariableNode[]>(resolve => {
					pending.push(resolve)
				})
		)
		const { loading } = useVariableCatalog(groupId)
		await flush()
		groupId.value = 9802
		await flush()
		expect(loading.value).toBe(true)

		pending[0]([nodeOf('late-9801')])
		await flush()
		expect(loading.value).toBe(true)

		pending[1]([nodeOf('ok-9802')])
		await flush()
		expect(loading.value).toBe(false)
	})

	it('在飞请求期间切到空模板组会结束 loading', async () => {
		const groupId = ref(9901)
		let resolvePending: (list: VariableNode[]) => void = () => {}
		mocks.variableCatalog.mockImplementationOnce(
			() =>
				new Promise<VariableNode[]>(resolve => {
					resolvePending = resolve
				})
		)
		const { loading } = useVariableCatalog(groupId)
		await flush()
		expect(loading.value).toBe(true)

		groupId.value = 0
		await flush()
		expect(loading.value).toBe(false)

		resolvePending([nodeOf('late-9901')])
		await flush()
		expect(loading.value).toBe(false)
	})

	// 评审 M1：目录状态改由宿主（tree-v2.vue）持有，首轮加载时机必须可由宿主控制
	it('immediate:false 不抢跑，启动前的切组也不请求，ensureLoaded 后加载当前组', async () => {
		mocks.variableCatalog.mockImplementation((groupId: number) => Promise.resolve([nodeOf(`g${groupId}`)]))
		const groupId = ref(9020)
		const { catalog, loaded, ensureLoaded } = useVariableCatalog(groupId, { immediate: false })
		await flush()
		expect(mocks.variableCatalog).not.toHaveBeenCalled()
		expect(loaded.value).toBe(false)

		// 宿主启动之前的切组同样不抢跑
		groupId.value = 9021
		await flush()
		expect(mocks.variableCatalog).not.toHaveBeenCalled()

		await ensureLoaded()
		expect(mocks.variableCatalog).toHaveBeenCalledTimes(1)
		expect(mocks.variableCatalog).toHaveBeenLastCalledWith(9021)
		expect(catalog.value[0]?.name).toBe('g9021')
		expect(loaded.value).toBe(true)

		// 启动之后切组恢复自动加载
		groupId.value = 9022
		await flush()
		expect(mocks.variableCatalog).toHaveBeenCalledTimes(2)
		expect(catalog.value[0]?.name).toBe('g9022')
	})

	it('新一轮加载期间 loaded 先撤下，不让宿主徽标停留在上一个组的数字', async () => {
		const groupId = ref(9030)
		mocks.variableCatalog.mockResolvedValueOnce([nodeOf('a'), nodeOf('b')])
		const { loaded, reload } = useVariableCatalog(groupId)
		await flush()
		expect(loaded.value).toBe(true)

		let resolvePending: (list: VariableNode[]) => void = () => {}
		mocks.variableCatalog.mockImplementationOnce(
			() =>
				new Promise<VariableNode[]>(resolve => {
					resolvePending = resolve
				})
		)
		reload()
		await nextTick()
		expect(loaded.value).toBe(false)

		resolvePending([nodeOf('a')])
		await flush()
		expect(loaded.value).toBe(true)
	})

	it('切到空模板组时 loaded 撤下，收起态不残留上一组徽标', async () => {
		const groupId = ref(9040)
		mocks.variableCatalog.mockResolvedValue([nodeOf('x')])
		const { loaded } = useVariableCatalog(groupId)
		await flush()
		expect(loaded.value).toBe(true)

		groupId.value = 0
		await flush()
		expect(loaded.value).toBe(false)
	})

	// 评审：request.ts 对 401/403 直接 reject 中文字符串（L148 / L154），且本接口 noErrorMessage 时不弹全局 toast
	it('401/403 reject 中文字符串时原样展示原因', async () => {
		const groupId = ref(9050)
		mocks.variableCatalog.mockRejectedValueOnce('您没有权限访问该资源')
		const { error, loaded } = useVariableCatalog(groupId)
		await flush()
		expect(error.value).toBe('您没有权限访问该资源')
		expect(loaded.value).toBe(false)
	})
})
