<template>
	<el-dialog v-model="dialogVisible" title="请选择模板" width="75%" destroy-on-close @close="dialogVisible = false">
		<query-table :table-hook="tableHook"></query-table>
		<template #footer>
			<el-button :icon="Close" @click="dialogVisible = false">取消</el-button>
			<el-button type="success" :icon="DocumentAdd" :loading="submitLoading" @click="generateCode()">生成</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { createBuiltinCreateTime, createBuiltinIndex, createBuiltinSelection, createBuiltinUpdateTime, useCrudTable, useSubmitHandler } from '@/hooks'
import { genGeneratorApi, genTemplateApi } from '@/api'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { getDictTag } from '@/utils/enum'
import { Close, DocumentAdd } from '@element-plus/icons-vue'
import QueryTable from '@/components/crud/QueryTable.vue'
import type { GenTemplateEntity, GenTemplateQuery, SubmitOptions, UseCrudTableOptions } from '@/types'

defineOptions({
	name: 'GenTableTemplate'
})

const emit = defineEmits(['clearSelection'])

// ====== 查询上下文 ======
// 模板组 ID 由 init() 传入，用 ref + computed 交给 hook，避免每次打开把条件写死在 queryForm 里
const currentTemplateGroupId = ref(-1)

const tableConfig: UseCrudTableOptions<GenTemplateEntity, GenTemplateQuery> = {
	initQueryData: (): GenTemplateQuery => ({
		templateGroupId: -1,
		templateName: '',
		templateType: ''
	}),
	queryContext: computed(() => ({ templateGroupId: currentTemplateGroupId.value })),
	dataListApi: genTemplateApi.voPage,
	mountedGetData: false,
	searchSchema: [
		{ prop: 'templateName', label: '模板名称' },
		{ prop: 'templateType', label: '模板类型', component: 'select', options: TEMPLATE_TYPES.items, width: 160 }
	],
	tableColumns: [
		createBuiltinSelection(),
		createBuiltinIndex(),
		{ key: 'templateName', label: '模板名称' },
		{ key: 'generatorPath', label: '模板路径' },
		{ key: 'templateType', label: '模板类型', formatter: getDictTag(TEMPLATE_TYPES) },
		{ key: 'templateDesc', label: '描述' },
		createBuiltinCreateTime(),
		createBuiltinUpdateTime()
	],
	showTableHeader: false
}

const tableHook = useCrudTable(tableConfig)

// ====== 弹窗与生成逻辑 ======
const dialogVisible = ref(false)
const generatorTypeRef = ref<number>(-1)
const tableIdRef = ref<number[]>([])

const init = (templateGroupId: number, generatorType: number, tableIdList: number[]) => {
	dialogVisible.value = true
	generatorTypeRef.value = generatorType
	tableIdRef.value = tableIdList
	currentTemplateGroupId.value = templateGroupId

	// destroy-on-close 会让表格实例每次重建，el-table 的勾选 store 是新实例的（空），
	// 但 hook 里的 selectedRows/selectedIds 是跨次打开的旧镜像，必须显式清掉，
	// 否则这次打开即便一条没勾，生成请求仍会带上上次的模板 id。
	tableHook.selection.clear()

	// 重置查询表单并加载数据
	nextTick(() => {
		tableHook.query.reset()
	})
}

const submitState = shallowReactive<SubmitOptions>({
	visible: dialogVisible,
	successDuration: 1000,
	onSuccess: () => {
		emit('clearSelection')
	}
})

const { submitLoading, submitHandle } = useSubmitHandler(submitState)

const generateCode = () => {
	// selection.ids 由 primaryKey 映射而来，与改造前 dataListSelections 的内容一致
	const templateIdList = [...tableHook.selection.ids]

	if (templateIdList.length === 0) {
		ElMessage.warning('请选择模板')
		return
	}
	const dataForm = {
		templateIdList,
		tableIdList: tableIdRef.value
	}
	const generatorType = generatorTypeRef.value
	if (generatorType === 0) {
		submitState.submitApi = genGeneratorApi.tableDownloadZip
		submitState.successMessage = '代码已经下载到浏览器'
	} else if (generatorType === 1) {
		submitState.submitApi = genGeneratorApi.tableDownloadLocal
		submitState.successMessage = '代码已经生成到本地'
	} else {
		ElMessage.warning('生成类型异常')
		return
	}
	submitHandle(dataForm)
}

defineExpose({
	init
})
</script>
