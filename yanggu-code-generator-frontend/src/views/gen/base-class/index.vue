<template>
	<div>
		<!-- 查询表单卡片 -->
		<el-card v-if="queryShow" class="layout-query-card" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="基类包名" prop="packageName">
					<el-input v-model="state.queryForm.packageName" clearable placeholder="请输入基类包名"></el-input>
				</el-form-item>
				<el-form-item label="基类类名" prop="className">
					<el-input v-model="state.queryForm.className" clearable placeholder="请输入基类类名"></el-input>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" :loading="state.dataListLoading" :icon="Search" @click="getDataList()">查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button :icon="Refresh" @click="resetQueryHandle()">重置</el-button>
				</el-form-item>
			</el-form>
		</el-card>

		<!-- 表格卡片 -->
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
							<el-button type="primary" :icon="Plus" @click="formInitHandle('add')">新增</el-button>
							<el-button type="danger" :loading="state.deleteLoading" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
						</el-space>
					</template>
				</table-tool-bar>
			</template>
			<!-- 表格数据 -->
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
				<el-table-column prop="baseClassName" label="基类名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="packageName" label="基类包名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="className" label="基类类名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="fields" label="基类字段" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="remark" label="备注" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
						<el-button type="primary" link :icon="Edit" @click="formInitHandle('update', scope.row.id)">修改</el-button>
						<el-button type="primary" link :icon="CopyDocument" @click="formInitHandle('copy', scope.row.id)">复制</el-button>
						<el-button type="primary" link :icon="Delete" @click="deleteBatchHandle(scope.row.id)">删除</el-button>
					</template>
				</el-table-column>
			</el-table>
			<!-- 分页操作栏 -->
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
		</el-card>
	</div>
	<!-- 弹窗表单 -->
	<base-class-form ref="formRef" :mode="dialogMode" @refresh-data-list="getDataList()"></base-class-form>
</template>

<script setup lang="ts">
import useTableAction, { IHooksOptions } from '@/hooks/use-table-action'
import { useComplexForm } from '@/hooks/use-init-form'
import { reactive } from 'vue'
import BaseClassForm from '@/views/gen/base-class/form.vue'
import TableToolBar from '@/components/table/tool-bar/index.vue'
import { genBaseClassApi } from '@/api'
import { GenBaseClassEntity, GenBaseClassQuery } from '@/types'
import { CopyDocument, Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenBaseClass'
})

const state = reactive({
	dataListApi: genBaseClassApi.entityPage,
	deleteListApi: genBaseClassApi.deleteList,
	queryForm: {
		packageName: '',
		className: ''
	}
} as IHooksOptions<GenBaseClassQuery, GenBaseClassEntity>)

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
	resetQueryHandle,
	tableIndex
} = useTableAction(state)

const { formRef, dialogMode, formInitHandle } = useComplexForm()
</script>
