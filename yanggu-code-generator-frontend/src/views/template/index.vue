<template>
	<el-dialog v-model="dialogVisible" :title="templateGroupDialogTitleRef" width="80%" @close="dialogVisible = false">
		<el-card class="layout-query">
			<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
				<el-form-item label="模板名称" prop="templateName">
					<el-input v-model="state.queryForm.templateName" clearable placeholder="请输入模板名称"></el-input>
				</el-form-item>
				<el-form-item label="模板类型" prop="templateType">
					<el-select v-model="state.queryForm.templateType" style="width: 160px" clearable placeholder="请选择模板类型">
						<el-option v-for="item in TEMPLATE_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
					</el-select>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" @click="getDataList()">查询</el-button>
				</el-form-item>
				<el-form-item>
					<el-button @click="resetQueryRef()">重置</el-button>
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
				<el-table-column prop="templateName" label="模板名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column prop="generatorPath" label="模板路径" show-overflow-tooltip header-align="center" align="center"></el-table-column>
				<el-table-column
					prop="templateType"
					label="模板类型"
					header-align="center"
					align="center"
					:formatter="(_: any, __: any, value: any) => getLabel(value, TEMPLATE_TYPES)"
				></el-table-column>
				<el-table-column prop="templateDesc" label="模板描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
			<add-or-update ref="addOrUpdateRef" :template-group-id="currentGroupId" @refresh-data-list="getDataList"></add-or-update>
		</el-card>
	</el-dialog>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import AddOrUpdate from './add-or-update.vue'
import { TEMPLATE_GROUP_TYPES, TEMPLATE_TYPES } from '@/constant/enum'
import { getLabel } from '@/utils/enum'
import { templateDeleteListApi, templateEntityPageApi } from '@/api/template'

const state: IHooksOptions = reactive({
	dataListApi: templateEntityPageApi,
	deleteListApi: templateDeleteListApi,
	queryForm: {
		templateGroupId: null,
		templateName: '',
		templateType: null
	}
})

const dialogVisible = ref(false)
const templateGroupDialogTitleRef = ref('')
const currentGroupId = ref<number>(-1)
const queryRef = ref()
const addOrUpdateRef = ref()
const addOrUpdateHandle = (id?: number) => {
	addOrUpdateRef.value.init(id)
}
const resetQueryRef = () => {
	if (queryRef.value) {
		queryRef.value.resetFields()
	}
}

const init = (row: any) => {
	dialogVisible.value = true
	resetQueryRef()
	state.queryForm.templateGroupId = row.id
	getDataList()
	templateGroupDialogTitleRef.value = `${row.groupName}（${getLabel(row.type, TEMPLATE_GROUP_TYPES)}）模板配置`
	currentGroupId.value = row.id
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle, sortChangeHandle } = useCrud(state)

defineExpose({
	init
})
</script>
