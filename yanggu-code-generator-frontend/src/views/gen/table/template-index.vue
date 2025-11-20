<template>
	<el-dialog v-model="dialogVisible" title="请选择模板" width="75%" destroy-on-close @close="dialogVisible = false">
		<el-card class="layout-query-card" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="模板名称" prop="templateName">
					<el-input v-model="state.queryForm.templateName" clearable placeholder="请输入模板名称"></el-input>
				</el-form-item>
				<el-form-item label="模板类型" prop="templateType">
					<el-select
						v-model="state.queryForm.templateType"
						:options="TEMPLATE_TYPES"
						style="width: 160px"
						clearable
						placeholder="请选择模板类型"
					></el-select>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" :loading="state.dataListLoading" :icon="Search" @click="getDataList()">查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button :icon="Refresh" @click="resetQueryHandle()">重置</el-button>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card shadow="hover">
			<el-table
				v-loading="state.dataListLoading"
				:data="state.dataList"
				row-key="id"
				border
				max-height="60vh"
				class="layout-table"
				header-cell-class-name="layout-table-header"
				@selection-change="selectionChangeHandle"
			>
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="templateName" label="模板名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="generatorPath" label="模板路径" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="templateType"
					label="模板类型"
					header-align="center"
					align="center"
					:formatter="getLabel(TEMPLATE_TYPES)"
				></el-table-column>
				<el-table-column prop="templateDesc" label="描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			</el-table>
			<el-pagination
				:current-page="state.pageNum"
				:page-sizes="state.pageSizes"
				:page-size="state.pageSize"
				:total="state.total"
				layout="total, sizes, prev, pager, next, jumper"
				@size-change="sizeChangeHandle"
				@current-change="currentChangeHandle"
			></el-pagination>
		</el-card>
		<template #footer>
			<el-button type="success" :icon="DocumentAdd" :loading="submitLoading" @click="generateCode()">生成</el-button>
			<el-button :icon="Close" @click="dialogVisible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import useTableAction, { IHooksOptions } from '@/hooks/use-table-action'
import { reactive, ref, shallowReactive } from 'vue'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { ElMessage } from 'element-plus'
import { Close, DocumentAdd, Refresh, Search } from '@element-plus/icons-vue'
import { getLabel } from '@/utils/enum'
import { SubmitOptions, useSubmitHandler } from '@/hooks/use-submit-handler'
import { genGeneratorApi, genTemplateApi } from '@/api'
import { GenTemplateEntity, GenTemplateQuery } from '@/types'

defineOptions({
	name: 'GenTableTemplate'
})

const emit = defineEmits(['clearSelection'])

const state = reactive({
	dataListApi: genTemplateApi.voPage,
	createdIsNeed: false,
	queryForm: {
		templateGroupId: -1,
		templateName: '',
		templateType: ''
	}
} as IHooksOptions<GenTemplateQuery, GenTemplateEntity>)
const generatorTypeRef = ref()
const tableIdRef = ref()
const dialogVisible = ref(false)

const init = (templateGroupId: number, generatorType: number, tableIdList: number[]) => {
	dialogVisible.value = true
	state.queryForm.templateGroupId = templateGroupId
	generatorTypeRef.value = generatorType
	tableIdRef.value = tableIdList

	//重置查询表单数据，并且进行分页查询
	resetQueryHandle()
}

const generateCode = () => {
	const data = state.dataListSelections ?? []

	if (data.length === 0) {
		ElMessage.warning('请选择模板')
		return
	}
	const dataForm = {
		templateIdList: state.dataListSelections,
		tableIdList: tableIdRef.value
	}
	const generatorType = generatorTypeRef.value
	if (generatorType === 0) {
		submitState.submitApi = genGeneratorApi.tableDownloadZip
		submitState.message = '代码已经下载到浏览器'
	} else if (generatorType === 1) {
		submitState.submitApi = genGeneratorApi.tableDownloadLocal
		submitState.message = '代码已经下载到本地'
	} else {
		ElMessage.warning('生成类型异常')
		return
	}
	submitHandle(dataForm)
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, queryRef, resetQueryHandle, tableIndex } = useTableAction(state)

const submitState = shallowReactive({
	visible: dialogVisible,
	duration: 1000,
	onSuccess: () => {
		emit('clearSelection')
	}
} as SubmitOptions)

const { submitLoading, submitHandle } = useSubmitHandler(submitState)

defineExpose({
	init
})
</script>
