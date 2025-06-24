<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item prop="enumName">
				<el-input v-model="state.queryForm.enumName" clearable placeholder="请输入枚举名称"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button @click="resetQueryRef()">重置</el-button>
			</el-form-item>
		</el-form>
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
			<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="enumName" label="枚举名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="enumDesc" label="枚举描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import { enumEntityPageApi } from '@/api/enum'

const emit = defineEmits(['selectChange'])
const state: IHooksOptions = reactive({
	dataListApi: enumEntityPageApi,
	createdIsNeed: false,
	queryForm: {
		enumName: '',
		projectId: ''
	}
})
let isManualSelection = true

const queryRef = ref()
const tableRef = ref()
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

const selectionChangeHandle = (selections: any[]) => {
	if (isManualSelection) {
		emit('selectChange', selections)
	}
}

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

const { getDataList, sizeChangeHandle, currentChangeHandle } = useCrud(state)

defineExpose({
	init,
	toggleRowSelection
})
</script>
