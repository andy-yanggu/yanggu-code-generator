<template>
	<el-dialog v-model="visible" title="导入数据库表" :close-on-click-modal="false" width="55%">
		<el-form ref="queryRef" :model="queryForm" :inline="true" :rules="dataRules">
			<el-form-item label="项目" prop="projectId">
				<el-select v-model="queryForm.projectId" style="width: 180px" placeholder="请选择项目" clearable filterable>
					<el-option v-for="projectItem in projectList" :key="projectItem.id" :label="projectItem.projectName" :value="projectItem.id">
						<span style="font-weight: bold">{{ projectItem.projectName }}</span>
						<span v-if="projectItem.projectDesc && projectItem.projectDesc.trim()" style="color: #999; font-size: 12px">
							（{{ projectItem.projectDesc }}）
						</span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="表名" prop="tableName" :label-width="80">
				<el-input v-model="queryForm.tableName" placeholder="请输入表名" clearable style="width: 100%"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="getTableList()">查询</el-button>
			</el-form-item>
		</el-form>
		<el-table :data="tableList" border style="width: 100%" :max-height="400" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="60" :selectable="checkRowSelectable"></el-table-column>
			<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="tableName" label="表名" header-align="center" align="center"></el-table-column>
			<el-table-column prop="tableComment" label="注释" header-align="center" align="center"></el-table-column>
		</el-table>
		<template #footer>
			<el-button @click="visible = false">取消</el-button>
			<el-button type="primary" @click="submitHandle()">确定</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus/es'
import { tableImportApi } from '@/api/table'
import { projectEntityListApi, projectTableListApi } from '@/api/project'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const queryRef = ref()
const queryForm = reactive({
	projectId: null,
	tableName: ''
})

const projectList = ref([])
const tableList = ref([])

const dataForm = reactive({
	tableNameList: [] as any,
	projectId: null
})

const dataRules = reactive({
	projectId: [{ required: true, message: '项目不能为空', trigger: 'blur' }]
})

// 多选
const selectionChangeHandle = (selections: any[]) => {
	dataForm.tableNameList = selections.map((item: any) => item['tableName'])
}

const init = () => {
	visible.value = true
	tableList.value = []

	// 重置表单数据
	if (queryRef.value) {
		queryRef.value.resetFields()
	}

	getProjectList()
}

const getProjectList = () => {
	projectEntityListApi({}).then(res => {
		projectList.value = res.data
	})
}

const getTableList = () => {
	queryRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		projectTableListApi(queryForm).then(res => {
			tableList.value = res.data
		})
	})
}

// 表单提交
const submitHandle = () => {
	if (dataForm.tableNameList.length === 0) {
		ElMessage.warning('请选择记录')
		return
	}
	dataForm.projectId = queryForm.projectId

	tableImportApi(dataForm).then(() => {
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
