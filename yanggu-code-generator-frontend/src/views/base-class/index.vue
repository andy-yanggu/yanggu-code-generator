<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item label="基类编码" prop="code">
				<el-input v-model="state.queryForm.code" clearable placeholder="请输入基类编码"></el-input>
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
		<el-table
			v-loading="state.dataListLoading"
			:data="state.dataList"
			border
			class="layout-table"
			@selection-change="selectionChangeHandle"
			@sort-change="sortChangeHandle"
		>
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="packageName" label="基类包名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="code" label="基类编码" header-align="center" align="center"></el-table-column>
			<el-table-column prop="fields" label="基类字段" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="remark" label="备注" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import AddOrUpdate from './add-or-update.vue'
import { baseClassDeleteListApi, baseClassEntityPageApi } from '@/api/baseClass'

const state: IHooksOptions = reactive({
	dataListApi: baseClassEntityPageApi,
	deleteListApi: baseClassDeleteListApi,
	queryForm: {
		code: ''
	}
})

const queryRef = ref()
const addOrUpdateRef = ref()
const addOrUpdateHandle = (id?: number) => {
	addOrUpdateRef.value.init(id)
}
const resetQueryRef = () => {
	queryRef.value.resetFields()
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle, sortChangeHandle } = useCrud(state)
</script>
