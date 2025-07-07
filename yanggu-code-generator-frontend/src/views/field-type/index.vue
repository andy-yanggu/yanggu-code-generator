<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item label="字段类型" prop="columnType">
				<el-input v-model="state.queryForm.columnType" clearable placeholder="请输入字段类型"></el-input>
			</el-form-item>
			<el-form-item label="属性类型" prop="attrType">
				<el-select v-model="state.queryForm.attrType" style="width: 160px" clearable placeholder="请选择属性类型">
					<el-option v-for="item in ATTR_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button @click="resetQueryHandle()">重置</el-button>
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
			<el-table-column prop="columnType" label="字段类型" header-align="center" align="center"></el-table-column>
			<el-table-column
				prop="attrType"
				label="属性类型"
				header-align="center"
				align="center"
				:formatter="(_: any, __: any, value: any) => getLabel(value, ATTR_TYPES)"
			></el-table-column>
			<el-table-column prop="packageName" label="属性包名" header-align="center" align="center"></el-table-column>
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
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { reactive } from 'vue'
import AddOrUpdate from '@/views/field-type/add-or-update.vue'
import { ATTR_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import { fieldTypeDeleteListApi, fieldTypeEntityPageApi } from '@/api/field-type'
import { useInitForm } from '@/hooks/use-init-form'

const state: IHooksOptions = reactive({
	dataListApi: fieldTypeEntityPageApi,
	deleteListApi: fieldTypeDeleteListApi,
	queryForm: {
		columnType: '',
		attrType: ''
	}
})

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle, sortChangeHandle, queryRef, resetQueryHandle } =
	useIndexQuery(state)

const { addOrUpdateRef, addOrUpdateHandle } = useInitForm()
</script>
