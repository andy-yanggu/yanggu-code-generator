<template>
	<el-dialog v-model="dialogVisible" title="请选择模板" width="75%" @close="dialogVisible = false">
		<query-table ref="queryTableRef" :table-hook="tableHook">
			<!-- 工具栏留空，不需要默认按钮 -->
			<template #toolbar-left></template>
		</query-table>
		<template #footer>
			<div class="footer-buttons">
				<el-button type="success" :icon="DocumentAdd" :loading="submitLoading" @click="generateCode()">生成</el-button>
				<el-button :icon="Close" @click="dialogVisible = false">取消</el-button>
			</div>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { createBuiltinCreateTime, createBuiltinIndex, createBuiltinSelection, createBuiltinUpdateTime, useCrudTable } from '@/hooks'
import { genGeneratorApi, genTemplateApi } from '@/api'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import type { GenTemplateEntity, GenTemplateQuery, UseCrudTableOptions } from '@/types'
import { Close, DocumentAdd } from '@element-plus/icons-vue'
import QueryTable from '@/components/crud/QueryTable.vue'

defineOptions({
	name: 'GenEnumTemplate'
})

const emit = defineEmits(['clearSelection'])

// ====== 查询上下文（响应式） ======
const currentGroupId = ref<number>(0)

// ====== 表格配置 ======
const tableConfig: UseCrudTableOptions<GenTemplateEntity, GenTemplateQuery> = {
	initQueryForm: (): GenTemplateQuery => ({
		templateGroupId: '',
		templateName: '',
		templateType: ''
	}),
	dataListApi: genTemplateApi.voPage,
	mountedGetData: false,
	queryContext: computed(() => ({ templateGroupId: currentGroupId.value })),
	searchSchema: [
		{ prop: 'templateName', label: '模板名称' },
		{ prop: 'templateType', label: '模板类型', component: 'select', options: TEMPLATE_TYPES.items, width: 160 }
	],
	tableColumns: [
		createBuiltinSelection(),
		createBuiltinIndex(),
		{ key: 'templateName', label: '模板名称' },
		{ key: 'generatorPath', label: '模板路径' },
		{ key: 'templateType', label: '模板类型', formatter: getLabel(TEMPLATE_TYPES) },
		{ key: 'templateDesc', label: '描述' },
		createBuiltinUpdateTime(),
		createBuiltinCreateTime()
	],
	showOperation: false
}

const tableHook = useCrudTable(tableConfig)

// ====== 弹窗与提交逻辑 ======
const dialogVisible = ref(false)
const initData = reactive({
	enumIdList: [] as string[],
	generatorType: -1
})

const init = (enumTemplateGroupId: number, generatorType: number, enumIdList: string[]) => {
	dialogVisible.value = true
	initData.enumIdList = enumIdList
	initData.generatorType = generatorType
	currentGroupId.value = enumTemplateGroupId

	// 重置查询表单并加载数据
	nextTick(() => {
		tableHook.query.reset()
	})
}

const submitLoading = ref(false)

const generateCode = () => {
	const selectedRows = tableHook.selection.rows
	if (selectedRows.length === 0) {
		ElMessage.warning('请选择模板')
		return
	}

	const dataForm = {
		enumIdList: initData.enumIdList,
		templateIdList: selectedRows.map((row: GenTemplateEntity) => row.id)
	}

	const generatorType = initData.generatorType
	const submitApi = generatorType === 0 ? genGeneratorApi.enumDownloadZip : genGeneratorApi.enumDownloadLocal
	const successMessage = generatorType === 0 ? '代码已经下载到本地，请查看' : '代码已经生成到服务器本地，请查看'

	submitLoading.value = true
	submitApi(dataForm)
		.then(() => {
			ElMessage.success(successMessage)
			dialogVisible.value = false
			emit('clearSelection')
		})
		.finally(() => {
			submitLoading.value = false
		})
}

defineExpose({
	init
})
</script>
<style scoped>
.footer-buttons {
	display: flex;
	justify-content: flex-end;
}
</style>
