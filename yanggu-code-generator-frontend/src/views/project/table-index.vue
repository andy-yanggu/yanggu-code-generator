<template>
	<el-card class="layout-query" shadow="hover">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item label="表名" prop="tableName">
				<el-input v-model="state.queryForm.tableName" clearable placeholder="请输入表名"></el-input>
			</el-form-item>
			<el-form-item label="类名" prop="className">
				<el-input v-model="state.queryForm.className" clearable placeholder="请输入类名"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" :icon="Search" @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button :icon="Refresh" @click="resetQueryHandle()">重置</el-button>
			</el-form-item>
		</el-form>
	</el-card>
	<el-card shadow="hover">
		<el-table
			ref="tableRef"
			v-loading="state.dataListLoading"
			row-key="id"
			:data="state.dataList"
			border
			class="layout-table"
			@selection-change="selectionChangeHandle"
		>
			<el-table-column type="selection" reserve-selection header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="tableName" label="表名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="className" label="类名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="functionName" label="功能名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="tableComment" label="注释" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="version" label="版本" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="author" label="作者" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
	</el-card>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { reactive, ref } from 'vue'
import { tableEntityPageApi } from '@/api/table'
import { Refresh, Search } from '@element-plus/icons-vue'

const emit = defineEmits(['selectChange'])

const state: IHooksOptions = reactive({
	dataListApi: tableEntityPageApi,
	createdIsNeed: false,
	queryForm: {
		tableName: '',
		className: '',
		projectId: null
	}
})

const tableRef = ref()
let isManualSelection = true

const init = (projectId: number) => {
	state.queryForm.projectId = projectId

	//重置表单查询
	resetQueryHandle()
	getDataList()
}

const selectionChangeHandle = (selections: any[]) => {
	// 仅用户操作时触发事件
	if (isManualSelection) {
		emit('selectChange', selections)
	}
}
const { getDataList, sizeChangeHandle, currentChangeHandle, queryRef, resetQueryHandle, tableIndex } = useIndexQuery(state)

const toggleRowSelection = (rowList: any[]) => {
	if (rowList.length === 0) {
		return
	}
	isManualSelection = false
	rowList.forEach((row: any) => {
		tableRef.value.toggleRowSelection(row, true)
	})
	isManualSelection = true
}
defineExpose({
	init,
	toggleRowSelection
})
</script>
