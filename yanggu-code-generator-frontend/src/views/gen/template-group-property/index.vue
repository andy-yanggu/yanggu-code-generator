<template>
	<el-dialog v-model="visible" :title="`属性配置（${templateGroupName}）`" :close-on-click-modal="false" width="80%">
		<el-card v-if="queryShow" class="layout-query-card" shadow="hover">
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
							<el-upload :limit="1" :show-file-list="false" :http-request="({ file }) => importHandle(file, { templateGroupId })">
								<el-button type="success" :icon="Upload">导入</el-button>
							</el-upload>
							<el-button type="info" :icon="Download" @click="exportHandle()">导出</el-button>
						</el-space>
					</template>
				</table-tool-bar>
			</template>
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
				<el-table-column prop="required" label="是否必填" show-overflow-tooltip header-align="center" align="center">
					<template #default="scope">
						<el-switch
							v-model="scope.row.required"
							:active-value="requiredSwitch.activeValue"
							:inactive-value="requiredSwitch.inactiveValue"
							:active-text="requiredSwitch.activeText"
							:inactive-text="requiredSwitch.inactiveText"
							inline-prompt
							@change="(val: number) => requiredChangeHandle(val, scope.row)"
						></el-switch>
					</template>
				</el-table-column>
				<el-table-column prop="propOrder" label="排序" header-align="center" align="center" sortable="custom" :min-width="100">
					<template #default="scope">
						<el-input-number
							v-model="scope.row.propOrder"
							:disabled="state.dataListLoading"
							:min="0"
							size="small"
							@focus="scope.row._oldPropOrder = scope.row.propOrder"
							@change="(currentValue: number, oldValue: number) => orderChangeHandle(currentValue, oldValue, scope.row)"
						></el-input-number>
					</template>
				</el-table-column>
				<el-table-column
					prop="columnSpan"
					label="字段布局方式"
					show-overflow-tooltip
					header-align="center"
					align="center"
					:formatter="getLabel(COLUMN_SPAN_TYPES)"
				></el-table-column>
				<el-table-column prop="remark" label="备注" show-overflow-tooltip header-align="center" align="center"></el-table-column>
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
			<template-group-property-form ref="formRef" @refresh-data-list="getDataList()"></template-group-property-form>
		</el-card>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import TemplateGroupPropertyForm from '@/views/gen/template-group-property/form.vue'
import { Delete, Download, Edit, Plus, Refresh, Search, Upload } from '@element-plus/icons-vue'
import { getLabel } from '@/utils/enum'
import { COLUMN_SPAN_TYPES, COMPONENT_TYPES } from '@/constant/enum'
import TableToolBar from '@/components/table/tool-bar/index.vue'
import { useInitForm, useSwitchChangeHandler, useSwitchState, useTableAction } from '@/hooks'
import { ElMessage } from 'element-plus'
import { genTemplateGroupPropertyApi } from '@/api'
import { GenTemplateGroupPropertyEntity, GenTemplateGroupPropertyQuery, IHooksOptions, SwitchUpdateConfig } from '@/types'

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

const state = reactive({
	dataListApi: genTemplateGroupPropertyApi.entityPage,
	deleteListApi: genTemplateGroupPropertyApi.deleteList,
	exportApi: genTemplateGroupPropertyApi.export,
	importApi: genTemplateGroupPropertyApi.import,
	mountedGetData: false,
	queryForm: {
		templateGroupId: '',
		propTitle: '',
		propKey: ''
	},
	order: 'propOrder',
	asc: true
} as IHooksOptions<GenTemplateGroupPropertyEntity, GenTemplateGroupPropertyQuery>)

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
	exportHandle,
	importHandle,
	tableIndex
} = useTableAction(state)

const init = () => {
	visible.value = true
	state.queryForm.templateGroupId = props.templateGroupId
	getDataList()
}

const requiredSwitch = useSwitchState({
	field: 'required',
	states: [
		{ value: 1, text: '是', isActive: true },
		{ value: 0, text: '否', isActive: false }
	]
})

const switchUpdateConfig = {
	switchState: requiredSwitch,
	apiFn: (val, row) => genTemplateGroupPropertyApi.changeRequired(row.id, val),
	afterSuccess: getDataList
} as SwitchUpdateConfig

const requiredChangeHandle = useSwitchChangeHandler(switchUpdateConfig)

const orderChangeHandle = (currentValue: number, oldValue: number, row: GenTemplateGroupPropertyEntity) => {
	// 如果没变化，直接返回
	if (currentValue === oldValue) {
		return
	}
	genTemplateGroupPropertyApi
		.changeOrder(row.id, currentValue)
		.then(() => {
			ElMessage.success({
				message: '排序修改成功',
				grouping: true
			})
			getDataList()
		})
		.catch(() => {
			row.propOrder = oldValue
		})
}

const { formRef, formInitHandle } = useInitForm(() => ({ templateGroupId: props.templateGroupId }))

defineExpose({
	init
})
</script>
