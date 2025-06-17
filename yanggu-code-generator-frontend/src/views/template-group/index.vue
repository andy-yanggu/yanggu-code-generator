<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item prop="groupName">
				<el-input v-model="state.queryForm.groupName" clearable placeholder="请输入模板组名称"></el-input>
			</el-form-item>
			<el-form-item prop="type">
				<el-select v-model="state.queryForm.type" style="width: 170px" clearable placeholder="请选择模板组类型">
					<el-option v-for="item in TEMPLATE_GROUP_TYPES" :key="item.value" :label="item.label" :value="item.value"></el-option>
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
				<Import ref="templateGroupImportRef" @refresh-data-list="getDataList"></Import>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="exportHandle()">导出</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="danger" @click="deleteBatchHandle()">删除</el-button>
			</el-form-item>
		</el-form>
	</el-card>

	<el-card>
		<el-table v-loading="state.dataListLoading" :data="state.dataList" border class="layout-table" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="groupName" label="模板组名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column
				prop="type"
				label="模板组类型"
				:formatter="(_: any, __: any, value: any) => getLabel(value, TEMPLATE_GROUP_TYPES)"
				header-align="center"
				align="center"
			></el-table-column>
			<el-table-column prop="groupDesc" label="模板组描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link @click="handlerTemplate(scope.row)">模板配置</el-button>
					<el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
					<el-button type="primary" link @click="copyTemplateGroupHandle(scope.row.id)">复制</el-button>
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
		<add-or-update ref="addOrUpdateRef" @refresh-data-list="getDataList"></add-or-update>
		<!-- 弹窗，复制模板组 -->
		<copy ref="copyTemplateGroupRef" @refresh-data-list="getDataList"></copy>
	</el-card>

	<template-index ref="templateIndexRef" :key="currentGroupId"></template-index>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import AddOrUpdate from './add-or-update.vue'
import { TEMPLATE_GROUP_TYPES } from '@/constant/enum'
import Copy from '@/views/template-group/copy.vue'
import TemplateIndex from '../template/index.vue'
import { ElMessage } from 'element-plus'
import { exportTemplateGroupApi } from '@/api/templateGroup'
import Import from './import.vue'
import { getLabel } from '@/utils/enum'

const state: IHooksOptions = reactive({
	dataListUrl: '/templateGroup/entityPage',
	deleteUrl: '/templateGroup/deleteList',
	queryForm: {
		groupName: '',
		type: ''
	}
})

const queryRef = ref()
const addOrUpdateRef = ref()
const copyTemplateGroupRef = ref()
const currentGroupId = ref<number>(-1)
const templateIndexRef = ref()
const templateGroupImportRef = ref()

const addOrUpdateHandle = (id: number) => {
	addOrUpdateRef.value.init(id)
}
const resetQueryRef = () => {
	queryRef.value.resetFields()
}

const copyTemplateGroupHandle = (id: number) => {
	copyTemplateGroupRef.value.init(id)
}

const handlerTemplate = (row: any) => {
	templateIndexRef.value.init(row)
}

const exportHandle = () => {
	const idList = state.dataListSelections ? state.dataListSelections : []
	if (idList.length === 0) {
		ElMessage.warning('请选择导出的模板组')
		return
	}
	exportTemplateGroupApi(idList)
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle } = useCrud(state)
</script>
