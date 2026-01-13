<template>
	<el-card class="layout-query-card" shadow="hover">
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
			max-height="60vh"
			class="layout-table"
			header-cell-class-name="layout-table-header"
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
					<el-button type="primary" link :icon="View" @click="enumItemIndexShow(scope.row.id, scope.row.enumName)">查看枚举项</el-button>
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
		></el-pagination>
	</el-card>

	<enum-item-index ref="enumItemIndexRef"></enum-item-index>
</template>

<script setup lang="ts">
import { useTableAction } from '@/hooks'
import { reactive, ref } from 'vue'
import { genEnumApi } from '@/api'
import { GenEnumEntity, GenEnumQuery, IHooksOptions } from '@/types'
import EnumItemIndex from '@/views/gen/project/enum-item-index.vue'
import { Refresh, Search, View } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenProjectEnum'
})

const emit = defineEmits(['selectChange'])

// 初始化查询表单数据
const initQueryFormData = (): GenEnumQuery => ({
	enumName: '',
	projectId: ''
})

const state = reactive({
	dataListApi: genEnumApi.entityPage,
	mountedGetData: false,
	queryContext: {
		projectId: ''
	},
	initQueryFormData,
	queryForm: initQueryFormData()
} as IHooksOptions<GenEnumEntity, GenEnumQuery>)
let isManualSelection = true

const tableRef = ref()
const enumItemIndexRef = ref()
const init = (projectId: number) => {
	state.queryContext!.projectId = projectId
	//重置表单查询
	resetQueryHandle()
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

const enumItemIndexShow = (enumId: number, enumName: string) => {
	enumItemIndexRef.value.init(enumId, enumName)
}

const { getDataList, sizeChangeHandle, currentChangeHandle, queryRef, resetQueryHandle, tableIndex } = useTableAction(state)

defineExpose({
	init,
	toggleRowSelection
})
</script>
