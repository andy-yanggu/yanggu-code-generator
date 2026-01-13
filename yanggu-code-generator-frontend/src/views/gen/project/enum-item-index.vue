<template>
	<el-dialog v-model="dialogVisible" :title="`枚举项（${enumNameRef}）`" width="80%" @close="dialogVisible = false">
		<el-card class="layout-query-card" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="枚举项名称" prop="enumItemName">
					<el-input v-model="state.queryForm.enumItemName" placeholder="请输入枚举项名称" clearable></el-input>
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
				v-loading="state.dataListLoading"
				:data="state.dataList"
				border
				max-height="60vh"
				class="layout-table"
				header-cell-class-name="layout-table-header"
				@sort-change="sortChangeHandle"
			>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="enumItemName" label="枚举项名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemCode" label="枚举项编码" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemDesc" label="枚举项描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemOrder" label="枚举项排序" header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="createTime"
					label="创建时间"
					show-overflow-tooltip
					min-width="120"
					header-align="center"
					align="center"
					sortable="custom"
				></el-table-column>
				<el-table-column
					prop="updateTime"
					label="修改时间"
					show-overflow-tooltip
					min-width="120"
					header-align="center"
					align="center"
					sortable="custom"
				></el-table-column>
			</el-table>
			<el-pagination
				:current-page="state.pageNum"
				:page-sizes="state.pageSizes"
				:page-size="state.pageSize"
				:total="state.total"
				layout="total, sizes, prev, pager, next, jumper"
				@size-change="sizeChangeHandle"
				@current-change="currentChangeHandle"
			></el-pagination>
		</el-card>
	</el-dialog>
</template>

<script setup lang="ts">
import { useTableAction } from '@/hooks'
import { reactive, ref } from 'vue'
import { genEnumItemApi } from '@/api'
import { GenEnumItemEntity, GenEnumItemQuery, IHooksOptions } from '@/types'
import { Refresh, Search } from '@element-plus/icons-vue'

defineOptions({
	name: 'ProjectEnumItem'
})

// 初始化表单查询参数
const initQueryFormData = (): GenEnumItemQuery => ({
	enumItemName: '',
	enumId: -1
})

const state = reactive({
	dataListApi: genEnumItemApi.entityPage,
	mountedGetData: false,
	queryContext: {
		enumId: -1
	},
	initQueryFormData,
	queryForm: initQueryFormData()
} as IHooksOptions<GenEnumItemEntity, GenEnumItemQuery>)

const dialogVisible = ref(false)
const enumNameRef = ref('')

const init = (enumId: number, enumName: string) => {
	dialogVisible.value = true
	enumNameRef.value = enumName
	state.queryContext!.enumId = enumId
	resetQueryHandle()
}

defineExpose({
	init
})

const { getDataList, sizeChangeHandle, currentChangeHandle, sortChangeHandle, queryRef, resetQueryHandle, tableIndex } = useTableAction(state)
</script>
