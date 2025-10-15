<template>
	<el-dialog v-model="visible" :title="`${templateGroupName} - 属性配置`" :close-on-click-modal="false" width="80%">
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
				<el-upload :limit="1" :show-file-list="false" :http-request="handleManualUpload">
					<el-button type="success" :icon="Upload">导入</el-button>
				</el-upload>
				<el-button type="info" :icon="Download" @click="exportHandle()">导出</el-button>
			</el-space>
			<el-table
				ref="tableRef"
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
import { Delete, Download, Edit, Plus, Refresh, Search, Upload } from '@element-plus/icons-vue'
import { genTemplateGroupPropertyApi } from '@/api/gen/template-group-property'
import { getLabel } from '@/utils/enum'
import { COMPONENT_TYPES } from '@/constant/enum'
import { ElMessage } from 'element-plus'

defineOptions({
	name: 'GenTemplateGroupProperty'
})

const props = defineProps({
	templateGroupId: {
		type: Number,
		required: true
	},
	templateGroupName: {
		type: String,
		required: true
	}
})

const visible = ref(false)
const tableRef = ref()

const state: IHooksOptions = reactive({
	dataListApi: genTemplateGroupPropertyApi.entityPage,
	deleteListApi: genTemplateGroupPropertyApi.deleteList,
	createdIsNeed: false,
	queryForm: {
		templateGroupId: null,
		propTitle: '',
		propKey: ''
	},
	order: 'propOrder',
	asc: true
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

const init = () => {
	visible.value = true
	state.queryForm.templateGroupId = props.templateGroupId
	getDataList()
}

// 导出
const exportHandle = () => {
	const idList = state.dataListSelections ? state.dataListSelections : []
	if (idList.length === 0) {
		ElMessage.warning('请选择导出的模板组属性')
		return
	}
	genTemplateGroupPropertyApi.export(idList).then(() => {
		ElMessage.success('导出成功，请查看已下载的文件')
		tableRef.value.clearSelection()
		state.dataListSelections = []
	})
}

// 导入
const handleManualUpload = (options: any) => {
	const { file } = options
	const formData = new FormData()
	formData.append('file', file)
	formData.append('templateGroupId', state.queryForm.templateGroupId)

	genTemplateGroupPropertyApi
		.import(formData)
		.then(() => {
			ElMessage.success('模板组属性导入成功')
		})
		.then(() => {
			getDataList()
		})
}

defineExpose({
	init
})

const addOrUpdateHandle = (id?: number) => {
	addOrUpdateRef.value.initHandle(state.queryForm.templateGroupId, id)
}

const { addOrUpdateRef } = useInitForm()
</script>
