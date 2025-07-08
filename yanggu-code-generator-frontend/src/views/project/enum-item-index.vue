<template>
	<el-dialog v-model="dialogVisible" title="枚举配置" width="80%" @close="dialogVisible = false">
		<el-card class="layout-query" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="枚举项名称" prop="enumItemName">
					<el-input v-model="state.queryForm.enumItemName" placeholder="请输入枚举项名称" clearable></el-input>
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
			<el-table v-loading="state.dataListLoading" :data="state.dataList" border class="layout-table" @sort-change="sortChangeHandle">
				<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="enumItemName" label="枚举项名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemCode" label="枚举项编码" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemDesc" label="枚举项描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemOrder" label="枚举项排序" header-align="center" align="center"></el-table-column>
				<el-table-column prop="createTime" label="创建时间" header-align="center" align="center" sortable="custom"></el-table-column>
				<el-table-column prop="updateTime" label="修改时间" header-align="center" align="center" sortable="custom"></el-table-column>
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
	</el-dialog>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { reactive, ref } from 'vue'
import { enumItemEntityPageApi } from '@/api/enum-item'
import { Refresh, Search } from '@element-plus/icons-vue'

const state: IHooksOptions = reactive({
	dataListApi: enumItemEntityPageApi,
	createdIsNeed: false,
	queryForm: {
		enumItemName: '',
		enumId: -1
	}
})

const dialogVisible = ref(false)

const init = (enumId: number) => {
	dialogVisible.value = true
	resetQueryHandle()
	state.queryForm.enumId = enumId
	getDataList()
}

defineExpose({
	init
})

const { getDataList, sizeChangeHandle, currentChangeHandle, sortChangeHandle, queryRef, resetQueryHandle } = useIndexQuery(state)
</script>
