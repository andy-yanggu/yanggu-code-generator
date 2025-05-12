<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item>
				<el-button icon="Search" type="primary" @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button icon="RefreshRight" @click="reset(queryRef)">重置</el-button>
			</el-form-item>
		</el-form>
	</el-card>

	<el-card>
		<el-space>
			<el-space>
				<el-button icon="Plus" type="primary" @click="addOrUpdateHandle()">新增</el-button>
			</el-space>
			<el-space>
				<el-button icon="Delete" plain type="danger" @click="deleteBatchHandle()">批量删除</el-button>
			</el-space>
		</el-space>
		<el-table v-loading="state.dataListLoading" :data="state.dataList" border class="layout-table" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column prop="id" label="id" header-align="center" align="center"></el-table-column>
			<el-table-column prop="tableName" label="表名" header-align="center" align="center"></el-table-column>
			<el-table-column prop="databaseName" label="数据库名" header-align="center" align="center"></el-table-column>
			<el-table-column prop="className" label="类名" header-align="center" align="center"></el-table-column>
			<el-table-column prop="tableComment" label="说明" header-align="center" align="center"></el-table-column>
			<el-table-column prop="projectId" label="项目ID" header-align="center" align="center"></el-table-column>
			<el-table-column prop="author" label="作者" header-align="center" align="center"></el-table-column>
			<el-table-column prop="version" label="项目版本号" header-align="center" align="center"></el-table-column>
			<el-table-column prop="functionName" label="功能名" header-align="center" align="center"></el-table-column>
			<el-table-column prop="formLayout" label="表单布局  1：一列   2：两列" header-align="center" align="center"></el-table-column>
			<el-table-column prop="createTime" label="创建时间" header-align="center" align="center"></el-table-column>
			<el-table-column prop="updateTime" label="修改时间" header-align="center" align="center"></el-table-column>
			<el-table-column prop="isDelete" label="是否删除（0未删除, 1删除）" header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
					<el-button type="primary" link @click="deleteBatchHandle(scope.row.id)">删除</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination
			:current-page="state.page"
			:page-sizes="state.pageSizes"
			:page-size="state.limit"
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

const state: IHooksOptions = reactive({
	dataListUrl: '/table/entityPage',
	deleteUrl: '/table/deleteList',
	queryForm: {}
})

const queryRef = ref()
const addOrUpdateRef = ref()
const addOrUpdateHandle = (id: number) => {
	addOrUpdateRef.value.init(id)
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle, reset } = useCrud(state)
</script>
