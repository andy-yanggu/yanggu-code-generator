<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item prop="projectId">
				<el-select v-model="state.queryForm.projectId" style="width: 140px" clearable placeholder="请选择项目">
					<el-option v-for="item in projectList" :key="item.id" :label="item.projectName" :value="item.id"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item prop="tableName">
				<el-input v-model="state.queryForm.tableName" clearable placeholder="请输入表名"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button icon="Search" type="primary" @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button icon="RefreshRight" @click="resetQueryRef()">重置</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="importHandle()">导入</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="generatorCodeBatch()">生成代码</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="danger" @click="deleteBatchHandle()">删除</el-button>
			</el-form-item>
		</el-form>
	</el-card>

	<el-card>
		<el-table v-loading="state.dataListLoading" :data="state.dataList" border class="layout-table" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="projectName" label="项目名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="tableName" label="表名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="generatorType" label="生成方式" :formatter="handlerType" header-align="center" align="center"></el-table-column>
			<el-table-column prop="tableComment" label="说明" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
					<el-button type="primary" link @click="generatorCode(scope.row)">生成代码</el-button>
					<el-button type="primary" link @click="editHandle(scope.row.id)">字段配置</el-button>
					<el-button type="primary" link @click="previewHandle(scope.row)">预览</el-button>
					<el-button type="primary" link @click="syncHandle(scope.row)">同步</el-button>
					<el-button type="primary" link @click="deleteBatchHandle(scope.row.id)">删除</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination
			:current-page="state.pageNum"
			:page-sizes="state.pageSizes"
			:page-size="state.pageSize"
			:total="state.total"
			layout="total, sizes, prev, pager, next, jumper"
			@size-change="sizeChangeHandle"
			@current-change="currentChangeHandle"
		>
		</el-pagination>

		<!-- 弹窗, 新增 / 修改 -->
		<update ref="addOrUpdateRef" @refresh-data-list="getDataList"></update>

		<!-- 导入表组件 -->
		<import ref="importRef" @refresh-data-list="getDataList"></import>

		<!-- 预览 -->
		<preview ref="previewRef" @refresh-data-list="getDataList"></preview>

		<!-- 字段配置 -->
		<field-config ref="editRef" @refresh-data-list="getDataList"></field-config>

		<!-- 模板组展示 -->
		<template-index
			ref="templateIndexRef"
			:key="currentTemplateGroupIdTs"
			:table-id="currentTableId"
			:table-id-list="currentTableIdList"
			:generator-type="currentGeneratorType"
			:template-group-id="currentTemplateGroupId"
		></template-index>
	</el-card>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { nextTick, reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import Import from './import.vue'
import Update from './update.vue'
import Preview from './preview.vue'
import FieldConfig from './field-config.vue'
import TemplateIndex from './template-index.vue'
import { projectEntityListApi } from '@/api/project'
import { tableSyncApi, tableGenerateCheckApi } from '@/api/table'
import { ElMessage } from 'element-plus/es'
import { ElMessageBox } from 'element-plus'
import { PROJECT_GENERATE_TYPES } from '@/constant/enum'

const state: IHooksOptions = reactive({
	dataListUrl: '/table/voPage',
	deleteUrl: '/table/deleteList',
	queryForm: {
		tableName: '',
		projectId: null
	}
})

const queryRef = ref()
const addOrUpdateRef = ref()
const importRef = ref()
const editRef = ref()
const previewRef = ref()
const currentTemplateGroupId = ref()
const currentTemplateGroupIdTs = ref()
const currentTableId = ref()
const currentTableIdList = ref()
const currentGeneratorType = ref()
const templateIndexRef = ref()

const addOrUpdateHandle = (id: number) => {
	addOrUpdateRef.value.init(id)
}
const projectList = ref([])
projectEntityListApi({}).then((res: any) => {
	projectList.value = res.data
})

const resetQueryRef = () => {
	queryRef.value.resetFields()
}

const importHandle = (id?: number) => {
	importRef.value.init(id)
}

const previewHandle = (tableItem: any) => {
	previewRef.value.init(tableItem)
}

const editHandle = (id?: number) => {
	editRef.value.init(id)
}

const handlerType = (row: any) => {
	return PROJECT_GENERATE_TYPES.find(item => item.value === row.generatorType)?.label
}

//生成代码
const generatorCode = item => {
	currentTemplateGroupIdTs.value = Date.now()
	currentTemplateGroupId.value = item.tableTemplateGroupId
	currentTableId.value = item.id
	currentGeneratorType.value = item.generatorType
	nextTick(() => {
		templateIndexRef.value.init()
	})
}

const generatorCodeBatch = () => {
	const data = state.dataListSelections ? state.dataListSelections : []
	if (data.length === 0) {
		ElMessage.warning('请选择要生成代码的表')
		return
	}
	currentTemplateGroupIdTs.value = Date.now()
	tableGenerateCheckApi(data).then(res => {
		const checkData = res.data
		if (!checkData.checkResult) {
			ElMessage.warning('当前选择的表不是同一个项目')
			return
		} else {
			currentTemplateGroupId.value = checkData.tableTemplateGroupId
			currentTableIdList.value = data
			currentGeneratorType.value = checkData.generatorType
			nextTick(() => {
				templateIndexRef.value.init()
			})
		}
	})
}

const syncHandle = (row: any) => {
	ElMessageBox.confirm(`确定同步数据表${row.tableName}吗?`, '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			tableSyncApi(row.id).then(() => {
				ElMessage.success('同步成功')
			})
		})
		.catch(() => {})
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle } = useCrud(state)
</script>
