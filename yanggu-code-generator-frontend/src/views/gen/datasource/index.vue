<template>
	<div>
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
					<el-button type="primary" :loading="state.dataListLoading" :icon="Search" @click="getDataList()">查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button :icon="Refresh" @click="resetQueryHandle()">重置</el-button>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card shadow="hover">
			<el-space class="layout-space">
				<el-button type="primary" :icon="Plus" @click="formInitHandle('add')">新增</el-button>
				<el-button type="danger" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
			</el-space>
			<el-table
				v-loading="state.dataListLoading"
				:data="state.dataList"
				border
				max-height="60vh"
				class="layout-table"
				header-cell-class-name="layout-table-header"
				@selection-change="selectionChangeHandle"
				@sort-change="sortChangeHandle"
			>
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="connName" label="连接名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="dbType" label="数据库类型" header-align="center" align="center" :formatter="getLabel(DB_TYPES)"></el-table-column>
				<el-table-column prop="connUrl" label="URL" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="datasourceDesc" label="描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
				<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
					<template #default="scope">
						<el-row>
							<el-col :span="12">
								<el-button type="primary" link :icon="Connection" @click="datasourceTestHandle(scope.row.id)">测试</el-button>
							</el-col>
							<el-col :span="12">
								<el-button type="primary" link :icon="Edit" @click="formInitHandle('update', scope.row.id)">修改</el-button>
							</el-col>
						</el-row>
						<el-row>
							<el-col :span="12">
								<el-button type="primary" link :icon="CopyDocument" @click="formInitHandle('copy', scope.row.id)">复制</el-button>
							</el-col>
							<el-col :span="12">
								<el-button type="primary" link :icon="Delete" @click="deleteBatchHandle(scope.row.id)">删除</el-button>
							</el-col>
						</el-row>
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

			<!-- 弹窗, 新增 / 修改 -->
			<gen-datasource-form ref="formRef" :mode="dialogMode" @refresh-data-list="getDataList"></gen-datasource-form>
		</el-card>
	</div>
</template>

<script setup lang="ts">
import useTableAction, { IHooksOptions } from '@/hooks/use-table-action'
import { reactive } from 'vue'
import GenDatasourceForm from '@/views/gen/datasource/form.vue'
import { DB_TYPES } from '@/constant/enum'
import { ElMessage } from 'element-plus'
import { genDataSourceApi } from '@/api/gen/datasource'
import { getLabel } from '@/utils/enum'
import { useComplexForm } from '@/hooks/use-init-form'
import { Connection, CopyDocument, Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenDatasource'
})

const state: IHooksOptions = reactive({
	dataListApi: genDataSourceApi.entityPage,
	deleteListApi: genDataSourceApi.deleteList,
	queryForm: {
		dbType: '',
		connName: ''
	}
})

const datasourceTestHandle = (id: number) => {
	genDataSourceApi.test(id).then(data => {
		const { result, message } = data
		if (result) {
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
} = useTableAction(state)

const { formRef, dialogMode, formInitHandle } = useComplexForm()
</script>
