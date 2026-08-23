<template>
	<div>
		<!-- 查询表单卡片 -->
		<el-card v-if="queryShow" class="layout-query-card" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="基类名称" prop="baseClassName">
					<el-input v-model="state.queryForm.baseClassName" clearable placeholder="请输入基类名称" style="width: 170px"></el-input>
				</el-form-item>
				<el-form-item label="基类包名" prop="packageName">
					<el-input v-model="state.queryForm.packageName" clearable placeholder="请输入基类包名" style="width: 170px"></el-input>
				</el-form-item>
				<el-form-item label="基类类名" prop="className">
					<el-input v-model="state.queryForm.className" clearable placeholder="请输入基类类名" style="width: 170px"></el-input>
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
		<el-card ref="tableCardRef" class="layout-table-card" :class="{ 'is-maximized': tableCardMaximized }" shadow="hover">
			<!-- 表格工具栏 -->
			<template #header>
				<table-tool-bar v-model:show-search="queryShow" v-model:query-loading="state.dataListLoading" v-model:maximized="tableCardMaximized" @get-data-list="getDataList()">
					<template #left>
						<el-space size="default">
							<el-button type="primary" :icon="Plus" @click="formInitHandle()">新增</el-button>
							<el-button type="danger" :loading="state.deleteLoading" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
						</el-space>
					</template>
				</table-tool-bar>
			</template>
			<!-- 表格数据 -->
			<el-table
				ref="tableRef"
				v-loading="state.dataListLoading!"
				:data="state.dataList"
				border
				max-height="60vh"
				@selection-change="selectionChangeHandle"
				@sort-change="sortChangeHandle">
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="baseClassName" label="基类名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="packageName" label="基类包名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="className" label="基类类名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="fieldList" label="基类字段" header-align="center" align="center" :min-width="140">
					<template #default="scope">
						<popover-list :list="scope.row.fieldList" :width="250" :preview-count="2"></popover-list>
					</template>
				</el-table-column>
				<el-table-column prop="remark" label="备注" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="createTime"
					label="创建时间"
					show-overflow-tooltip
					min-width="120"
					header-align="center"
					align="center"
					sortable="custom"></el-table-column>
				<el-table-column
					prop="updateTime"
					label="修改时间"
					show-overflow-tooltip
					min-width="120"
					header-align="center"
					align="center"
					sortable="custom"></el-table-column>
				<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
					<template #default="scope">
						<el-row>
							<el-col :span="12">
								<el-button type="primary" link :icon="View" @click="formInitHandle({ type: 'detail', id: scope.row.id })">详情</el-button>
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
								<el-button type="primary" link :icon="Delete" @click="deleteBatchHandle(scope.row)">删除</el-button>
							</el-col>
						</el-row>
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
				@current-change="currentChangeHandle"></el-pagination>
		</el-card>
		<!-- 弹窗表单 -->
		<base-class-form ref="formRef" @refresh-data-list="getDataList()"></base-class-form>
	</div>
</template>

<script setup lang="ts">
import { useInitForm, useTableAction } from '@/hooks'
import BaseClassForm from '@/views/gen/base-class/form.vue'
import TableToolBar from '@/components/table/tool-bar/index.vue'
import PopoverList from '@/components/popover/list/index.vue'
import { genBaseClassApi } from '@/api'
import { GenBaseClassEntity, GenBaseClassQuery, IHooksOptions } from '@/types'
import { CopyDocument, Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenBaseClass'
})

// 初始化表单查询数据
const initQueryFormData = (): GenBaseClassQuery => ({
	baseClassName: '',
	packageName: '',
	className: ''
})

const state = reactive({
	tableSubject: '基类',
	deleteNameKey: 'baseClassName',
	dataListApi: genBaseClassApi.entityPage,
	deleteListApi: genBaseClassApi.deleteList,
	initQueryFormData,
	queryForm: initQueryFormData()
} as IHooksOptions<GenBaseClassEntity, GenBaseClassQuery>)

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
	tableCardMaximized,
	tableRef,
	resetQueryHandle,
	tableIndex
} = useTableAction(state)

const { formRef, formInitHandle } = useInitForm()
</script>
