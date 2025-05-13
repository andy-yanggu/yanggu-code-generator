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
			<el-table-column prop="tableComment" label="说明" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
					<el-button type="primary" link @click="previewHandle(scope.row.id)">预览</el-button>
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
	</el-card>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import Import from './import.vue'
import Update from './update.vue'
import Preview from './preview.vue'
import { projectEntityListApi } from '@/api/project'

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
const previewRef = ref()

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

const previewHandle = (tableId?: number) => {
	previewRef.value.init(tableId)
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle } = useCrud(state)
</script>
