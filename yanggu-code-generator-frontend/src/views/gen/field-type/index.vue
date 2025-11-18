<template>
	<div>
		<el-card v-if="queryShow" class="layout-query-card" shadow="hover">
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
				v-loading="state.dataListLoading"
				:data="state.dataList"
				border
				row-key="id"
				max-height="60vh"
				class="layout-table"
				header-cell-class-name="layout-table-header"
				@selection-change="selectionChangeHandle"
				@sort-change="sortChangeHandle"
			>
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="columnType" label="字段类型" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="attrType"
					label="属性类型"
					header-align="center"
					show-overflow-tooltip
					align="center"
					:formatter="getLabel(ATTR_TYPES)"
				></el-table-column>
				<el-table-column prop="packageName" label="包名" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
						<el-button type="primary" link :icon="Edit" @click="formInitHandle(scope.row.id)">修改</el-button>
						<el-button type="primary" link :icon="Delete" @click="deleteBatchHandle(scope.row.id)">删除</el-button>
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
			<field-type-form ref="formRef" @refresh-data-list="getDataList"></field-type-form>
		</el-card>
	</div>
</template>

<script setup lang="ts">
import useTableAction, { IHooksOptions } from '@/hooks/use-table-action'
import { reactive } from 'vue'
import FieldTypeForm from '@/views/gen/field-type/form.vue'
import { ATTR_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import { genFieldTypeApi, GenFieldTypeEntity, GenFieldTypeQuery } from '@/api/gen/field-type'
import { useInitForm } from '@/hooks/use-init-form'
import { Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import TableToolBar from '@/components/table/tool-bar/index.vue'

defineOptions({
	name: 'GenFieldType'
})

const state = reactive({
	dataListApi: genFieldTypeApi.entityPage,
	deleteListApi: genFieldTypeApi.deleteList,
	queryForm: {
		columnType: '',
		attrType: ''
	}
} as IHooksOptions<GenFieldTypeQuery, GenFieldTypeEntity>)

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

const { formRef, formInitHandle } = useInitForm()
</script>
