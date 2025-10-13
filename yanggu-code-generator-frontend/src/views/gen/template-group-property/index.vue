<template>
	<el-dialog v-model="visible" title="模板组属性" :close-on-click-modal="false" width="80%">
		<el-card class="layout-query" shadow="hover">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="属性标题" prop="propTitle">
					<el-input v-model="state.queryForm.propTitle" clearable placeholder="请输入属性标题"></el-input>
				</el-form-item>
				<el-form-item label="属性键" prop="propKey">
					<el-input v-model="state.queryForm.propKey" clearable placeholder="请输入属性键"></el-input>
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
				<el-button type="primary" :icon="Plus" @click="addOrUpdateHandle()">新增</el-button>
				<el-button type="danger" :icon="Delete" @click="deleteBatchHandle()">删除</el-button>
			</el-space>
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
				<el-table-column prop="propTitle" label="属性标题" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="propKey" label="属性键" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="propDefaultValue" label="属性默认值" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="componentType"
					label="组件类型"
					show-overflow-tooltip
					header-align="center"
					align="center"
					:formatter="getLabel(COMPONENT_TYPES)"
					min-width="70"
				></el-table-column>
				<el-table-column
					prop="required"
					label="是否必填"
					show-overflow-tooltip
					header-align="center"
					align="center"
					:formatter="row => (row.required === 0 ? '否' : '是')"
				></el-table-column>
				<el-table-column prop="propOrder" label="排序" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="remark" label="备注" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
					<template #default="scope">
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
				background
				layout="total, sizes, prev, pager, next, jumper"
				@size-change="sizeChangeHandle"
				@current-change="currentChangeHandle"
			>
			</el-pagination>

			<!-- 弹窗, 新增 / 修改 -->
			<add-or-update ref="addOrUpdateRef" @refresh-data-list="getDataList"></add-or-update>
		</el-card>
	</el-dialog>
</template>

<script setup lang="ts">
import { IHooksOptions, useIndexQuery } from '@/hooks/use-index-query'
import { useInitForm } from '@/hooks/use-init-form'
import { reactive, ref } from 'vue'
import AddOrUpdate from '@/views/gen/template-group-property/add-or-update.vue'
import { Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { genTemplateGroupPropertyApi } from '@/api/gen/template-group-property'
import { getLabel } from '@/utils/enum'
import { COMPONENT_TYPES } from '@/constant/enum'

defineOptions({
	name: 'GenTemplateGroupProperty'
})

const visible = ref(false)

const state: IHooksOptions = reactive({
	dataListApi: genTemplateGroupPropertyApi.entityPage,
	deleteListApi: genTemplateGroupPropertyApi.deleteList,
	queryForm: {
		templateGroupId: null,
		propTitle: '',
		propKey: '',
		order: 'propOrder',
		asc: true
	}
})

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

const init = (templateGroupId: number) => {
	visible.value = true
	state.queryForm.templateGroupId = templateGroupId
}

defineExpose({
	init
})

const addOrUpdateHandle = (id?: number) => {
	addOrUpdateRef.value.initHandle(state.queryForm.templateGroupId, id)
}

const { addOrUpdateRef } = useInitForm()
</script>
