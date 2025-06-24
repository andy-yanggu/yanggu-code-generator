<template>
	<el-dialog v-model="dialogVisible" title="枚举配置" width="75%" @close="dialogVisible = false">
		<el-card class="layout-query">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="枚举项名称" prop="enumItemName">
					<el-input v-model="state.queryForm.enumItemName" placeholder="请输入枚举项名称" clearable></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="getDataList()">查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button @click="resetQueryRef()">重置</el-button>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="addOrUpdateHandle()">新增</el-button>
				</el-form-item>
				<el-form-item>
					<el-button type="danger" @click="deleteBatchHandle()">删除</el-button>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card>
			<el-table v-loading="state.dataListLoading" :data="state.dataList" border class="layout-table" @selection-change="selectionChangeHandle">
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="enumItemName" label="枚举项名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemCode" label="枚举项编码" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemDesc" label="枚举项描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemOrder" label="枚举项排序" header-align="center" align="center"></el-table-column>
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
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import AddOrUpdate from './add-or-update.vue'
import { enumItemDeleteListApi, enumItemEntityPageApi } from '@/api/enumItem'

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

const queryRef = ref()
const dialogVisible = ref(false)
const addOrUpdateRef = ref()
const addOrUpdateHandle = (id: number) => {
	addOrUpdateRef.value.init(enumIdRef.value, id)
}
const resetQueryRef = () => {
	queryRef.value.resetFields()
}

const init = (enumId: number) => {
	dialogVisible.value = true
	if (queryRef.value) {
		resetQueryRef()
	}
	enumIdRef.value = enumId
	state.queryForm.enumId = enumId
	getDataList()
}

defineExpose({
	init
})

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle } = useCrud(state)
</script>
