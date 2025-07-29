<template>
	<el-card class="layout-query" shadow="hover">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item label="连接名称" prop="connName">
				<el-input v-model="state.queryForm.connName" clearable placeholder="请输入连接名称"></el-input>
			</el-form-item>
			<el-form-item label="数据库类型" prop="dbType">
				<el-select v-model="state.queryForm.dbType" style="width: 160px" clearable placeholder="请选择数据库类型">
					<el-option v-for="item in DB_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
				</el-select>
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
		<el-space :size="'large'" class="layout-space">
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
		>
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="connName" label="连接名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column
				prop="dbType"
				label="数据库类型"
				header-align="center"
				align="center"
				:formatter="(_: any, __: any, value: any) => getLabel(value, DB_TYPES)"
			></el-table-column>
			<el-table-column prop="connUrl" label="URL" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="datasourceDesc" label="描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column
				prop="createTime"
				label="创建时间"
				show-overflow-tooltip
				width="120"
				header-align="center"
				align="center"
				sortable="custom"
			></el-table-column>
			<el-table-column
				prop="updateTime"
				label="修改时间"
				show-overflow-tooltip
				width="120"
				header-align="center"
				align="center"
				sortable="custom"
			></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link :icon="Connection" @click="datasourceTestHandle(scope.row.id)">测试</el-button>
					<el-button type="primary" link :icon="Edit" @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
					<el-button type="primary" link :icon="Delete" @click="deleteBatchHandle(scope.row.id)">删除</el-button>
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
import AddOrUpdate from './add-or-update.vue'
import { DB_TYPES } from '@/constant/enum'
import { ElMessage } from 'element-plus'
import { datasourceDeleteListApi, datasourceEntityPageApi, datasourceTestApi } from '@/api/datasource'
import { getLabel } from '@/utils/enum'
import { useInitForm } from '@/hooks/use-init-form'
import { Connection, Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenDatasource'
})

const state: IHooksOptions = reactive({
	dataListApi: datasourceEntityPageApi,
	deleteListApi: datasourceDeleteListApi,
	queryForm: {
		dbType: '',
		connName: ''
	}
})

const datasourceTestHandle = (id: number) => {
	datasourceTestApi(id).then((res: any) => {
		const { result, message } = res.data
		if (result === true) {
			ElMessage.success(message)
		} else {
			ElMessage.error(message)
		}
	})
}

const {
	getDataList,
	selectionChangeHandle,
	sizeChangeHandle,
	currentChangeHandle,
	deleteBatchHandle,
	sortChangeHandle,
	queryRef,
	resetQueryHandle,
	tableIndex
} = useIndexQuery(state)

const { addOrUpdateRef, addOrUpdateHandle } = useInitForm()
</script>
