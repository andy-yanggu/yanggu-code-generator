<template>
	<el-dialog v-model="visible" title="导入数据库表" :close-on-click-modal="false" draggable>
		<el-form ref="dataFormRef" :model="dataForm">
			<el-row>
				<el-col :span="11">
					<el-form-item label="项目" prop="projectId">
						<el-select v-model="dataForm.projectId" style="width: 100%" placeholder="请选择项目" clearable filterable @change="getTableList">
							<el-option v-for="projectItem in dataForm.projectList" :key="projectItem.id" :label="projectItem.projectName" :value="projectItem.id">
								<span style="font-weight: bold">{{ projectItem.projectName }}</span>
								<span v-if="projectItem.projectDesc && projectItem.projectDesc.trim()" style="color: #999; font-size: 12px"
									>（{{ projectItem.projectDesc }}）</span
								>
							</el-option>
						</el-select>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="表名" prop="tableName" :label-width="80">
						<!-- 监听tableName值，如果发生变化，则调用getTableList -->
						<el-input v-model="dataForm.tableName" placeholder="请输入表名" clearable style="width: 100%" @input="getTableList"></el-input>
					</el-form-item>
				</el-col>
			</el-row>
			<el-table :data="dataForm.tableList" border style="width: 100%" :max-height="400" @selection-change="selectionChangeHandle">
				<el-table-column type="selection" header-align="center" align="center" width="60" :selectable="checkRowSelectable"></el-table-column>
				<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="tableName" label="表名" header-align="center" align="center"></el-table-column>
				<el-table-column prop="tableComment" label="注释" header-align="center" align="center"></el-table-column>
			</el-table>
		</el-form>
		<template #footer>
			<el-button @click="visible = false">取消</el-button>
			<el-button type="primary" @click="submitHandle()">确定</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus/es'
// import { useTableImportSubmitApi } from '@/api/table'
import { projectTableListApi } from '@/api/project'
import { projectEntityListApi } from '@/api/project'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: '',
	tableNameListSelections: [] as any,
	projectId: null,
	tableName: '',
	projectList: [] as any,
	datasourceList: [] as any,
	tableList: [] as any
})

// 多选
const selectionChangeHandle = (selections: any[]) => {
	dataForm.tableNameListSelections = selections.map((item: any) => item['tableName'])
}

const init = () => {
	visible.value = true
	dataForm.id = ''

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	// 清空 tableList
	dataForm.tableList = []

	dataForm.projectList = []

	getProjectList()
}

const getProjectList = () => {
	projectEntityListApi({}).then(res => {
		dataForm.projectList = res.data
	})
}

const getTableList = () => {
	//清空projectId时，清空tableLis
	if (!dataForm.projectId) {
		dataForm.tableList = []
		return
	}
	const queryListForm = {
		projectId: dataForm.projectId,
		tableName: dataForm.tableName
	}
	projectTableListApi(queryListForm).then(res => {
		dataForm.tableList = res.data
	})
}

// 表单提交
const submitHandle = () => {
	const tableNameList = dataForm.tableNameListSelections ? dataForm.tableNameListSelections : []
	if (tableNameList.length === 0) {
		ElMessage.warning('请选择记录')
		return
	}

	useTableImportSubmitApi(dataForm.projectId, tableNameList).then(() => {
		ElMessage.success({
			message: '操作成功',
			duration: 500,
			onClose: () => {
				visible.value = false
				emit('refreshDataList')
			}
		})
	})
}

const checkRowSelectable = (row: any, _: number) => {
	return !row.exist
}

defineExpose({
	init
})
</script>
