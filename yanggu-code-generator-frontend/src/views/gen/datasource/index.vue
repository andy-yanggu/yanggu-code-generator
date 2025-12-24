<template>
	<div>
		<el-card v-if="queryShow" class="layout-query-card" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="连接名称" prop="connName">
					<el-input v-model="state.queryForm.connName" clearable placeholder="请输入连接名称"></el-input>
				</el-form-item>
				<el-form-item label="数据库类型" prop="dbType">
					<el-select
						v-model="state.queryForm.dbType"
						:options="DB_TYPES"
						style="width: 160px"
						filterable
						clearable
						placeholder="请选择数据库类型"
					></el-select>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" :loading="state.dataListLoading" :icon="Search" @click="getDataList()">查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button :icon="Refresh" @click="resetQueryHandle()">重置</el-button>
				</el-form-item>
			</el-form>
		</el-card>

		<el-card ref="tableCardRef" class="layout-table-card" shadow="hover">
			<!-- 表格工具栏 -->
			<template #header>
				<table-tool-bar
					v-if="tableCardRef"
					v-model:show-search="queryShow"
					v-model:query-loading="state.dataListLoading"
					:table-card-ref="tableCardRef"
					@get-data-list="getDataList()"
				>
					<template #left>
						<el-space>
							<el-button type="primary" :icon="Plus" @click="formInitHandle()">新增</el-button>
							<el-button type="danger" :loading="state.deleteLoading" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
						</el-space>
					</template>
				</table-tool-bar>
			</template>
			<el-table
				ref="tableRef"
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
								<el-button type="primary" link :icon="Edit" @click="formInitHandle(scope.row.id)">修改</el-button>
							</el-col>
						</el-row>
						<el-row>
							<el-col :span="12">
								<el-button type="primary" link :icon="CopyDocument" @click="formInitHandle({ type: 'copy', id: scope.row.id })">复制</el-button>
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
			<gen-datasource-form ref="formRef" @refresh-data-list="getDataList()"></gen-datasource-form>
		</el-card>
	</div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import GenDatasourceForm from '@/views/gen/datasource/form.vue'
import { DB_TYPES } from '@/constant/enum'
import { ElMessage } from 'element-plus'
import { genDatasourceApi } from '@/api'
import { GenDatasourceEntity, GenDatasourceQuery, IHooksOptions } from '@/types'
import { useInitForm, useTableAction } from '@/hooks'
import { getLabel } from '@/utils/enum'
import { Connection, CopyDocument, Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import TableToolBar from '@/components/table/tool-bar/index.vue'

defineOptions({
	name: 'GenDatasource'
})

const state = reactive({
	dataListApi: genDatasourceApi.entityPage,
	deleteListApi: genDatasourceApi.deleteList,
	queryForm: {
		dbType: '',
		connName: ''
	}
} as IHooksOptions<GenDatasourceEntity, GenDatasourceQuery>)

const datasourceTestHandle = (id: number) => {
	genDatasourceApi.test(id).then(data => {
		const { result, errorMessage, databaseName } = data
		if (result) {
			ElMessage.success(`测试成功，数据库为：${databaseName}`)
		} else {
			ElMessage.error(`测试失败，异常信息：${errorMessage}`)
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
	queryShow,
	tableCardRef,
	tableRef,
	resetQueryHandle,
	tableIndex
} = useTableAction(state)

const { formRef, formInitHandle } = useInitForm()
</script>
