<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item prop="tableName">
				<el-input v-model="state.queryForm.tableName" clearable placeholder="请输入表名"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button icon="Search" type="primary" @click="getDataList()">查询</el-button>
			</el-form-item>
		</el-form>
	</el-card>

	<el-card>
		<el-table v-loading="state.dataListLoading" :data="state.dataList" border class="layout-table" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="tableName" label="表名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="tableComment" label="说明" show-overflow-tooltip header-align="center" align="center"></el-table-column>
		</el-table>
	</el-card>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'

const state: IHooksOptions = reactive({
	dataListUrl: '/table/entityList',
	isPage: false,
	createdIsNeed: false,
	queryForm: {
		tableName: '',
		projectId: null
	}
})

const queryRef = ref()

const resetQueryRef = () => {
	queryRef.value.resetFields()
}

const init = (projectId: number) => {
	state.queryForm.projectId = projectId

	//重置表单查询
	if (queryRef.value) {
		resetQueryRef()
	}
	getDataList()
}

const getSelections = () => {
	return state.dataListSelections
}

const { getDataList, selectionChangeHandle } = useCrud(state)

defineExpose({
	init,
	getSelections
})
</script>
