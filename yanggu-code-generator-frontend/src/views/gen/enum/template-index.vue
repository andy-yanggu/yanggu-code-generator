<template>
	<div>
		<el-dialog v-model="dialogVisible" title="请选择模板" width="75%" @close="dialogVisible = false">
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
							filterable
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

			<el-card class="layout-table-card" shadow="hover">
				<el-table
					v-loading="state.dataListLoading!"
					:data="state.dataList"
					row-key="id"
					border
					max-height="60vh"
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
				<div class="footer-buttons">
					<el-button type="success" :icon="DocumentAdd" :loading="submitLoading" @click="generateCode()">生成</el-button>
					<el-button :icon="Close" @click="dialogVisible = false">取消</el-button>
				</div>
			</template>
		</el-dialog>
	</div>
</template>

<script setup lang="ts">
import { useSubmitHandler, useTableAction } from '@/hooks'
import { TEMPLATE_TYPES } from '@/constant/enum'
import { ElMessage } from 'element-plus'
import { getLabel } from '@/utils/enum'
import { genGeneratorApi, genTemplateApi } from '@/api'
import { GenTemplateEntity, GenTemplateQuery, IHooksOptions, SubmitOptions } from '@/types'
import { Close, DocumentAdd, Refresh, Search } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenEnumTemplate'
})

const emit = defineEmits(['clearSelection'])

// 初始化查询表单数据
const initQueryFormData = (): GenTemplateQuery => ({
	templateGroupId: '',
	templateName: '',
	templateType: ''
})

const state = reactive({
	dataListApi: genTemplateApi.voPage,
	mountedGetData: false,
	queryContext: {
		templateGroupId: ''
	},
	initQueryFormData,
	queryForm: initQueryFormData()
} as IHooksOptions<GenTemplateEntity, GenTemplateQuery>)

const initData = reactive({
	enumIdList: [],
	generatorType: -1
})

const dialogVisible = ref(false)

const init = (enumTemplateGroupId: number, generatorType: number, enumIdList: []) => {
	dialogVisible.value = true
	initData.enumIdList = enumIdList
	initData.generatorType = generatorType
	state.queryContext!.templateGroupId = enumTemplateGroupId

	//重置查询表单数据
	resetQueryHandle()
}

const generateCode = () => {
	const data = state.dataListSelections ?? []

	if (data.length === 0) {
		ElMessage.warning('请选择模板')
		return
	}
	const dataForm = {
		enumIdList: initData.enumIdList,
		templateIdList: data
	}
	const generatorType = initData.generatorType
	if (generatorType === 0) {
		submitState.submitApi = genGeneratorApi.enumDownloadZip
		submitState.successMessage = '代码已经下载到本地，请查看'
	} else if (generatorType === 1) {
		submitState.submitApi = genGeneratorApi.enumDownloadLocal
		submitState.successMessage = '代码已经生成到服务器本地，请查看'
	}
	submitHandle(dataForm)
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, queryRef, resetQueryHandle, tableIndex } = useTableAction(state)

const submitState = shallowReactive({
	visible: dialogVisible,
	successDuration: 1000,
	onSuccess: () => {
		emit('clearSelection')
		state.dataListSelections = []
	}
} as SubmitOptions)

const { submitLoading, submitHandle } = useSubmitHandler(submitState)

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
