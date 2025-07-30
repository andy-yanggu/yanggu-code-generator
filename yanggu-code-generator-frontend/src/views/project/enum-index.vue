<template>
	<el-card class="layout-query" shadow="hover">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item label="枚举名称" prop="enumName">
				<el-input v-model="state.queryForm.enumName" clearable placeholder="请输入枚举名称"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" :loading="state.dataListLoading" :icon="Search" @click="getDataList()">查询</el-button>
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
			<el-table-column prop="enumName" label="枚举名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="enumDesc" label="枚举描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="createTime" label="创建时间" header-align="center" align="center"></el-table-column>
			<el-table-column prop="updateTime" label="更新时间" header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link :icon="View" @click="enumItemIndexShow(scope.row.id)">查看枚举项</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination
			:current-page="state.pageNum"
			:page-sizes="state.pageSizes"
			:page-size="state.pageSize"
			:total="state.total"
			background
			layout="total, sizes, prev, pager, next, jumper"
			@size-change="sizeChangeHandle"
			@current-change="currentChangeHandle"
		>
		</el-pagination>
	</el-card>

	<enum-item-index ref="enumItemIndexRef"></enum-item-index>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { reactive, ref } from 'vue'
import { enumEntityPageApi } from '@/api/enum'
import EnumItemIndex from '@/views/project/enum-item-index.vue'
import { Refresh, Search, View } from '@element-plus/icons-vue'

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

const tableRef = ref()
const enumItemIndexRef = ref()
const init = (projectId: number) => {
	state.queryForm.projectId = projectId
	//重置表单查询
	resetQueryHandle()
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

const enumItemIndexShow = (enumId: number) => {
	enumItemIndexRef.value.init(enumId)
}

const { getDataList, sizeChangeHandle, currentChangeHandle, queryRef, resetQueryHandle, tableIndex } = useIndexQuery(state)

defineExpose({
	init,
	toggleRowSelection
})
</script>
