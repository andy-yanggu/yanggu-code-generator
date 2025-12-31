<template>
	<el-dialog v-model="dialogVisible" :title="`枚举配置（${enumNameRef}）`" width="80%" @close="dialogVisible = false">
		<el-card v-if="queryShow" class="layout-query-card" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="枚举项名称" prop="enumItemName">
					<el-input v-model="state.queryForm.enumItemName" placeholder="请输入枚举项名称" clearable></el-input>
				</el-form-item>
				<el-form-item label="枚举项编码" prop="enumItemCode">
					<el-input v-model="state.queryForm.enumItemCode" placeholder="请输入枚举项编码" clearable></el-input>
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
				>>
				<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
				<el-table-column type="index" :index="tableIndex" label="序号" header-align="center" align="center" width="60"></el-table-column>
				<el-table-column prop="enumItemName" label="枚举项名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemCode" label="枚举项编码" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemDesc" label="枚举项描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="enumItemOrder" label="枚举项排序" width="120" header-align="center" align="center" sortable="custom"></el-table-column>
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
				layout="total, sizes, prev, pager, next, jumper"
				@size-change="sizeChangeHandle"
				@current-change="currentChangeHandle"
			></el-pagination>

			<!-- 弹窗, 新增 / 修改 -->
			<enum-item-form ref="formRef" @refresh-data-list="getDataList()"></enum-item-form>
		</el-card>
	</el-dialog>
</template>

<script setup lang="ts">
import { useInitForm, useTableAction } from '@/hooks'
import { reactive, ref } from 'vue'
import EnumItemForm from '@/views/gen/enum-item/form.vue'
import { Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import TableToolBar from '@/components/table/tool-bar/index.vue'
import { genEnumItemApi } from '@/api'
import { GenEnumItemEntity, GenEnumItemQuery, IHooksOptions } from '@/types'

defineOptions({
	name: 'GenEnumItem'
})

const state = reactive({
	dataListApi: genEnumItemApi.entityPage,
	deleteListApi: genEnumItemApi.deleteList,
	mountedGetData: false,
	queryForm: {
		enumItemName: '',
		enumItemCode: '',
		enumId: -1
	}
} as IHooksOptions<GenEnumItemEntity, GenEnumItemQuery>)

const enumNameRef = ref('')

const dialogVisible = ref(false)

const init = (enumId: number, enumName: string) => {
	dialogVisible.value = true
	enumNameRef.value = enumName
	state.queryForm.enumId = enumId
	resetQueryHandle()
}

const { formRef, formInitHandle } = useInitForm(() => ({ enumId: state.queryForm.enumId }))

defineExpose({
	init
})

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
</script>
