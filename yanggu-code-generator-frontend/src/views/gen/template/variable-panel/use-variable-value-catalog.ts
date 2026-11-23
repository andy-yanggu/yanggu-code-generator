import { ref, type Ref } from 'vue'
import { genGeneratorApi } from '@/api'
import type { VariableNode } from '@/types'
import { resolveFailureReason } from './failure-reason'

/**
 * 带值变量目录加载：值依赖「选中的测试对象」而非模板组，故不做按组缓存，每次选择对象都实打实重查
 * （数据模型源自数据库当前状态，导入表结构、改属性后回到同一对象也不该看到旧值）。
 * 失败保留上一次成功目录，提示只落在面板内（规格 6.2，与 use-variable-catalog 同款策略）。
 *
 * 与结构目录 hook 的另一处不同：本 hook 不 watch 入参自动加载，由宿主在级联选择/清空时显式调 load——
 * 「渲染前发起值目录请求」的时序点只有宿主知道，塞进 watch 反而绕。
 */
export const useVariableValueCatalog = (
	templateGroupId: Ref<number>,
	templateGroupType: Ref<number>,
	projectId: Ref<number | string>,
	testId: Ref<number>
) => {
	const catalog = ref<VariableNode[]>([])
	const loading = ref(false)
	// 空串表示无异常
	const error = ref('')
	// 是否已有一份可展示的目录：宿主据此决定收起态徽标显示数字还是留空
	const loaded = ref(false)
	// 本次实例的请求序号：快速切对象时用于丢弃后到的旧响应
	let requestSeq = 0

	// 入参不完整（未选对象）时不请求：目录与状态一并清空，避免上一个对象的值停留。
	// testId 只对表/枚举组（type 1/2）必需：项目组的数据模型仅由 projectId 构建，后端根本不读 testId
	const load = async () => {
		const seq = ++requestSeq
		const needTestId = templateGroupType.value === 1 || templateGroupType.value === 2
		if (!templateGroupId.value || !projectId.value || (needTestId && !testId.value)) {
			catalog.value = []
			loaded.value = false
			error.value = ''
			loading.value = false
			return
		}
		loading.value = true
		try {
			// templateId/templateContent 与数据模型无关，但入参复用 TemplateTestQuery 契约，占位补齐
			const data = await genGeneratorApi.testDataModel({
				projectId: projectId.value,
				templateGroupId: templateGroupId.value,
				templateGroupType: templateGroupType.value,
				templateId: 0,
				templateContent: '',
				testId: testId.value
			})
			if (seq !== requestSeq) {
				return
			}
			catalog.value = data ?? []
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

	return { catalog, loading, error, loaded, load }
}
