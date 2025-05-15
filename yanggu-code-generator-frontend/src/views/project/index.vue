<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item prop="projectName">
				<el-input v-model="state.queryForm.projectName" clearable placeholder="请输入项目名称"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button icon="Search" type="primary" @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button icon="RefreshRight" @click="resetQueryRef()">重置</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="addOrUpdateHandle()">新增</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="danger" @click="deleteProjectBatchHandle()">删除</el-button>
			</el-form-item>
		</el-form>
	</el-card>

	<el-card>
		<el-table v-loading="state.dataListLoading" :data="state.dataList" border class="layout-table" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="projectName" label="项目名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="projectPackage" label="项目包名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="projectVersion" label="项目版本" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="projectDesc" label="项目描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
					<el-button type="primary" link @click="generatorCode(scope.row)">生成代码</el-button>
					<el-button type="primary" link @click="previewHandle(scope.row.id)">预览</el-button>
					<el-button type="primary" link @click="deleteProjectBatchHandle(scope.row.id)">删除</el-button>
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
		<add-or-update ref="addOrUpdateRef" @refresh-data-list="getDataList"></add-or-update>

		<!-- 预览 -->
		<preview ref="previewRef" @refresh-data-list="getDataList"></preview>

		<steps ref="stepsRef" :key="currentProjectId"></steps>
	</el-card>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import AddOrUpdate from './add-or-update.vue'
import Preview from './preview.vue'

import Steps from './steps.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import service from '@/utils/request'

const state: IHooksOptions = reactive({
	dataListUrl: '/project/entityPage',
	deleteUrl: '/project/deleteList',
	queryForm: {
		projectName: ''
	}
})

const queryRef = ref()
const addOrUpdateRef = ref()
const previewRef = ref()
const currentProjectId = ref()
const stepsRef = ref()
const addOrUpdateHandle = (id: number) => {
	addOrUpdateRef.value.init(id)
}
const resetQueryRef = () => {
	queryRef.value.resetFields()
}

const previewHandle = (projectId?: number) => {
	previewRef.value.init(projectId)
}

const deleteProjectBatchHandle = (projectId?: number) => {
	let data: any[] = []
	if (projectId) {
		data = [projectId]
	} else {
		data = state.dataListSelections ? state.dataListSelections : []
	}
	if (data.length === 0) {
		ElMessage.warning('请选择删除记录')
		return
	}
	ElMessageBox.confirm('删除项目会删除项目下的所有表，是否继续?', '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			service.delete(state.deleteUrl, { data }).then(() => {
				ElMessage.success('删除成功')
				getDataList()
			})
		})
		.catch(() => {})
}

const generatorCode = item => {
	stepsRef.value.init(item)
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle } = useCrud(state)
</script>
