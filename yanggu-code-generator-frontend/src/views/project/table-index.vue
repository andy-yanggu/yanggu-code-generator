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
			<el-table-column prop="tableName" label="表名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="tableComment" label="说明" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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

const emit = defineEmits(['selectChange'])

const state: IHooksOptions = reactive({
	dataListUrl: '/table/entityPage',
	createdIsNeed: false,
	queryForm: {
		tableName: '',
		projectId: null
	}
})

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
	emit('selectChange', selections)
}

const { getDataList, sizeChangeHandle, currentChangeHandle } = useCrud(state)

defineExpose({
	init,
	toggleRowSelection: (row, selected) => {
		tableRef.value.toggleRowSelection(row, selected)
	}
})
</script>
