<template>
	<el-dialog v-model="visible" title="导入表" :close-on-click-modal="false" width="70%">
		<el-card class="layout-query-card" shadow="hover">
			<el-form ref="queryRef" :model="state.queryForm" :inline="true" :rules="dataRules" @keyup.enter="getDataList()">
				<el-form-item label="项目" prop="projectId">
					<el-select v-model="state.queryForm.projectId" style="width: 180px" placeholder="请选择项目" clearable filterable>
						<el-option
							v-for="projectItem in projectList"
							:key="projectItem.id"
							:label="projectItem.projectName"
							:value="projectItem.id"
							:disabled="isBlank(projectItem.datasourceId)"
						>
							<option-label :label="projectItem.projectName" :desc="projectItem.projectDesc"></option-label>
						</el-option>
					</el-select>
				</el-form-item>
				<el-form-item label="表名" prop="tableName" :label-width="80">
					<el-input v-model="state.queryForm.tableName" placeholder="请输入表名" clearable style="width: 180px"></el-input>
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
				border
				style="width: 100%"
				:max-height="450"
				@selection-change="selectionChangeHandle"
			>
				<el-table-column type="selection" header-align="center" align="center" width="60" :selectable="(row: any) => !row.exist"></el-table-column>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="databaseName" label="数据库名称" header-align="center" align="center"></el-table-column>
				<el-table-column prop="tableName" label="表名" header-align="center" align="center"></el-table-column>
				<el-table-column prop="tableComment" label="注释" header-align="center" align="center"></el-table-column>
			</el-table>
		</el-card>
		<template #footer>
			<el-button type="primary" :icon="Upload" :disabled="isEmpty(state.dataListSelections!)" :loading="submitLoading" @click="submitData()">
				导入
			</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, shallowReactive } from 'vue'
import { ElMessage } from 'element-plus/es'
import { Close, Refresh, Search, Upload } from '@element-plus/icons-vue'
import OptionLabel from '@/components/option/label/index.vue'
import { useSubmitHandler, useTableAction } from '@/hooks'
import { genProjectApi, genTableApi } from '@/api'
import { GenProjectEntity, GenTableEntity, GenTableQuery, IHooksOptions, SubmitOptions } from '@/types'
import { isBlank, isEmpty } from '@/utils/tool'

defineOptions({
	name: 'GenTableImport'
})

const emit = defineEmits(['refreshDataList'])

// 初始化表单查询参数
const initQueryFormData = (): GenTableQuery => ({
	projectId: '',
	tableName: ''
})

const state = reactive({
	primaryKey: 'tableName',
	mountedGetData: false,
	resetQueryGetData: false,
	isPage: false,
	dataListApi: genProjectApi.tableList,
	initQueryFormData,
	queryForm: initQueryFormData()
} as IHooksOptions<GenTableEntity, GenTableQuery>)

const visible = ref(false)
const projectList = ref([] as GenProjectEntity[])

const dataRules = reactive({
	projectId: [{ required: true, message: '项目不能为空', trigger: 'blur' }]
})

// 初始化操作
const init = () => {
	visible.value = true
	state.dataList = []
	submitLoading.value = false

	// 重置表单数据
	resetQueryHandle()

	genProjectApi.entityList().then(data => {
		projectList.value = data
	})
}

// 表单提交
const submitData = () => {
	if (isEmpty(state.dataListSelections!)) {
		ElMessage.warning('请选择要导入的表')
		return
	}
	const dataForm = {
		tableNameList: state.dataListSelections,
		projectId: state.queryForm.projectId
	}
	submitHandle(dataForm)
}

const { getDataList, selectionChangeHandle, queryRef, resetQueryHandle, tableIndex } = useTableAction(state)

const submitState = shallowReactive({
	visible,
	successMessage: '导入成功',
	submitApi: genTableApi.importData,
	emit
} as SubmitOptions)

const { submitLoading, submitHandle } = useSubmitHandler(submitState)

defineExpose({
	init
})
</script>
