<template>
	<el-dialog v-model="dialogVisible" title="枚举配置" width="80%" @close="dialogVisible = false">
		<el-card class="layout-query">
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

		<el-card>
			<el-space :size="'large'">
				<el-button type="primary" :icon="Plus" @click="addOrUpdateHandle()">新增</el-button>
				<el-button type="danger" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
			</el-space>
			<el-table
				v-loading="state.dataListLoading"
				:data="state.dataList"
				border
				class="layout-table"
				@selection-change="selectionChangeHandle"
				@sort-change="sortChangeHandle"
				>>
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="enumItemName" label="枚举项名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemCode" label="枚举项编码" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemDesc" label="枚举项描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemOrder" label="枚举项排序" header-align="center" align="center"></el-table-column>
				<el-table-column prop="createTime" label="创建时间" header-align="center" align="center" sortable="custom"></el-table-column>
				<el-table-column prop="updateTime" label="修改时间" header-align="center" align="center" sortable="custom"></el-table-column>
				<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
					<template #default="scope">
						<el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
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
			<add-or-update ref="addOrUpdateRef" @refresh-data-list="getDataList"></add-or-update>
		</el-card>
	</el-dialog>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { reactive, ref } from 'vue'
import AddOrUpdate from '@/views/enum-item/add-or-update.vue'
import { enumItemDeleteListApi, enumItemEntityPageApi } from '@/api/enum-item'
import { Delete, Plus, Refresh, Search } from '@element-plus/icons-vue'

const enumIdRef = ref()
const state: IHooksOptions = reactive({
	dataListApi: enumItemEntityPageApi,
	deleteListApi: enumItemDeleteListApi,
	createdIsNeed: false,
	queryForm: {
		enumItemName: '',
		enumId: -1
	}
})

const dialogVisible = ref(false)
const addOrUpdateRef = ref()
const addOrUpdateHandle = (id?: number) => {
	addOrUpdateRef.value.initData(enumIdRef.value, id)
}

const init = (enumId: number) => {
	dialogVisible.value = true
	resetQueryHandle()
	enumIdRef.value = enumId
	state.queryForm.enumId = enumId
	getDataList()
}

defineExpose({
	init
})

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle, sortChangeHandle, queryRef, resetQueryHandle } =
	useIndexQuery(state)
</script>
