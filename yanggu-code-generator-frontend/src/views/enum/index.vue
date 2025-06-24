<template>
	<el-card class="layout-query">
		<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item label="项目" prop="projectId">
				<el-select v-model="state.queryForm.projectId" style="width: 140px" clearable placeholder="请选择项目">
					<el-option v-for="item in projectList" :key="item.id" :value="item.id" :label="item.projectName">{{ item.projectName }}</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="枚举名称" prop="enumName">
				<el-input v-model="state.queryForm.enumName" clearable placeholder="请输入枚举名称"></el-input>
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
				<el-button type="success" @click="generatorBatchHandler()">生成代码</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="danger" @click="deleteBatchHandle()">删除</el-button>
			</el-form-item>
		</el-form>
	</el-card>

	<el-card>
		<el-table
			ref="tableRef"
			v-loading="state.dataListLoading"
			:data="state.dataList"
			border
			class="layout-table"
			@selection-change="selectionChangeHandle"
		>
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column type="index" label="序号" header-align="center" align="center" width="60"></el-table-column>
			<el-table-column prop="projectName" label="项目名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="enumName" label="枚举名称" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column
				prop="generatorType"
				label="生成类型"
				show-overflow-tooltip
				header-align="center"
				:formatter="(_: any, __: any, value: any) => getLabel(value, PROJECT_GENERATE_TYPES)"
				align="center"
			></el-table-column>
			<el-table-column prop="enumDesc" label="枚举描述" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link @click="configEnumItemHandle(scope.row.id)">枚举配置</el-button>
					<el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
					<el-button type="primary" link @click="previewHandle(scope.row)">预览</el-button>
					<el-button type="primary" link @click="generatorHandler(scope.row)">生成代码</el-button>
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

		<enum-item-index ref="enumItemIndexRef"></enum-item-index>

		<preview ref="previewRef"></preview>

		<template-index ref="templateIndexRef" :key="currentTemplateGroupIdTs" @clear-selection="clearSelectionHandler()"></template-index>
	</el-card>
</template>

<script setup lang="ts">
import { useCrud } from '@/hooks'
import { nextTick, onMounted, reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import AddOrUpdate from './add-or-update.vue'
import EnumItemIndex from '@/views/enum-item/index.vue'
import TemplateIndex from '@/views/enum/template-index.vue'
import Preview from './preview.vue'
import { projectEntityListApi } from '@/api/project'
import { ElMessage } from 'element-plus'
import { enumGenerateCheckApi } from '@/api/enum'
import { getLabel } from '@/utils/enum'
import { PROJECT_GENERATE_TYPES } from '@/constant/enum'

onMounted(() => {
	getProjectList()
})

const state: IHooksOptions = reactive({
	dataListUrl: '/enum/voPage',
	deleteUrl: '/enum/deleteList',
	queryForm: {
		enumName: '',
		projectId: ''
	}
})

const queryRef = ref()
const addOrUpdateRef = ref()
const enumItemIndexRef = ref()
const previewRef = ref()
const tableRef = ref()
const templateIndexRef = ref()
const currentTemplateGroupIdTs = ref()
const projectList = ref([])
const addOrUpdateHandle = (id: number) => {
	addOrUpdateRef.value.init(id)
}
const resetQueryRef = () => {
	queryRef.value.resetFields()
}

const getProjectList = () => {
	projectEntityListApi({}).then((res: any) => {
		projectList.value = res.data
	})
}

const configEnumItemHandle = (id: number) => {
	enumItemIndexRef.value.init(id)
}

const previewHandle = (row: any) => {
	previewRef.value.init(row)
}

const generatorBatchHandler = () => {
	const data = state.dataListSelections ? state.dataListSelections : []
	if (data.length === 0) {
		ElMessage.warning('请选择要生成代码的枚举')
		return
	}
	currentTemplateGroupIdTs.value = Date.now()
	enumGenerateCheckApi(data).then(res => {
		const { checkResult, enumTemplateGroupId, generatorType } = res.data
		if (!checkResult) {
			ElMessage.warning('当前选择的枚举不是同一个项目')
		} else {
			nextTick(() => {
				templateIndexRef.value.init(enumTemplateGroupId, generatorType, data)
			})
		}
	})
}

const clearSelectionHandler = () => {
	state.dataListSelections = []
	tableRef.value.clearSelection()
}

const generatorHandler = (row: any) => {
	currentTemplateGroupIdTs.value = Date.now()
	nextTick(() => {
		templateIndexRef.value.init(row.enumTemplateGroupId, row.generatorType, [row.id])
	})
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle } = useCrud(state)
</script>
