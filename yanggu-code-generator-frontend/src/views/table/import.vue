<template>
	<el-dialog v-model="visible" title="导入数据库表" :close-on-click-modal="false" width="70%">
		<el-form ref="queryRef" :model="state.queryForm" :inline="true" :rules="dataRules">
			<el-form-item label="项目" prop="projectId">
				<el-select v-model="state.queryForm.projectId" style="width: 180px" placeholder="请选择项目" clearable filterable>
					<el-option v-for="projectItem in projectList" :key="projectItem.id" :label="projectItem.projectName" :value="projectItem.id">
						<span style="font-weight: bold">{{ projectItem.projectName }}</span>
						<span v-if="projectItem.projectDesc && projectItem.projectDesc.trim()" style="color: #999; font-size: 12px">
							（{{ projectItem.projectDesc }}）
						</span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="表名" prop="tableName" :label-width="80">
				<el-input v-model="state.queryForm.tableName" placeholder="请输入表名" clearable style="width: 100%"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" :loading="state.dataListLoading" :icon="Search" @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button :icon="Refresh" @click="resetQueryHandle()">重置</el-button>
			</el-form-item>
		</el-form>
		<el-table
			v-loading="state.dataListLoading"
			:data="state.dataList"
			border
			style="width: 100%"
			:max-height="400"
			@selection-change="selectionChangeHandle"
		>
			<el-table-column type="selection" header-align="center" align="center" width="60" :selectable="(row: any) => !row.exist"></el-table-column>
			<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="databaseName" label="数据库名称" header-align="center" align="center"></el-table-column>
			<el-table-column prop="tableName" label="表名" header-align="center" align="center"></el-table-column>
			<el-table-column prop="tableComment" label="注释" header-align="center" align="center"></el-table-column>
		</el-table>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus/es'
import { tableImportApi } from '@/api/table'
import { projectEntityListApi, projectTableListApi } from '@/api/project'
import { Check, Close, Refresh, Search } from '@element-plus/icons-vue'
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'

const emit = defineEmits(['refreshDataList'])

const state: IHooksOptions = reactive({
	primaryKey: 'tableName',
	createdIsNeed: false,
	isPage: false,
	dataListApi: projectTableListApi,
	queryForm: {
		projectId: null,
		tableName: ''
	}
})

const visible = ref(false)
const submitLoading = ref(false)
const projectList = ref([])

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

	projectEntityListApi({}).then(res => {
		projectList.value = res.data
	})
}

// 表单提交
const submitHandle = () => {
	if (state.dataListSelections!.length === 0) {
		ElMessage.warning('请选择表')
		return
	}
	const dataForm = {
		tableNameList: state.dataListSelections,
		projectId: state.queryForm.projectId
	}
	submitLoading.value = true

	tableImportApi(dataForm).then(() => {
		ElMessage.success({
			message: '导入成功',
			duration: 500,
			onClose: () => {
				submitLoading.value = false
				visible.value = false
				emit('refreshDataList')
			}
		})
	})
}

const { getDataList, selectionChangeHandle, queryRef, resetQueryHandle, tableIndex } = useIndexQuery(state)

defineExpose({
	init
})
</script>
