<template>
	<el-dialog v-model="visible" title="导入表" :close-on-click-modal="false" width="70%">
		<query-table :table-hook="tableHook">
			<!-- 项目下拉：需要 option-label 展示项目描述，并禁用未配置数据源的项目。
			     DynamicField 的 select 只按 options 渲染 label/value，没有这两项能力，故用字段插槽覆盖 -->
			<template #query-projectId>
				<el-select
					v-model="tableHook.search.form.projectId"
					style="width: 180px"
					placeholder="请选择项目"
					clearable
					filterable
					:validate-event="false"
				>
					<el-option
						v-for="projectItem in projectList"
						:key="projectItem.id"
						:label="projectItem.projectName"
						:value="projectItem.id"
						:disabled="isBlank(projectItem.datasourceId)"
					>
						<option-label :label="projectItem.projectName" :desc="projectItem.projectDesc"></option-label>
					</el-option>
				</el-select>
			</template>
		</query-table>
		<template #footer>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
			<el-button type="primary" :icon="Upload" :disabled="isEmpty(tableHook.selection.rows)" :loading="submitLoading" @click="submitData()">
				导入
			</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { Close, Upload } from '@element-plus/icons-vue'
import OptionLabel from '@/components/option/label/index.vue'
import QueryTable from '@/components/crud/QueryTable.vue'
import { createBuiltinIndex, useCrudTable, useSubmitHandler } from '@/hooks'
import { genProjectApi, genTableApi } from '@/api'
import { GenProjectEntity, GenTableEntity, GenTableQuery, SubmitOptions, UseCrudTableOptions } from '@/types'
import { isBlank, isEmpty } from '@/utils/tool'

defineOptions({
	name: 'GenTableImport'
})

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const projectList = ref([] as GenProjectEntity[])

const tableConfig: UseCrudTableOptions<GenTableEntity, GenTableQuery> = {
	// /project/tableList 返回的是数据库里的表（TableImportVO），没有 id 字段，只能以表名作行标识：
	// 它同时用于 selection.ids 取值和 el-table 的 row-key
	primaryKey: 'tableName',
	// 接口返回全量数组，不走分页（hook 的 pagination 为 undefined，分页条自动隐藏）
	isPage: false,
	mountedGetData: false,
	// 项目为必填，打开弹窗时不自动查询，等用户选完项目点「查询」
	resetQueryGetData: false,
	dataListApi: genProjectApi.tableList,
	initQueryData: (): GenTableQuery => ({
		projectId: '',
		tableName: ''
	}),
	searchSchema: [
		{ prop: 'projectId', label: '项目', ruleList: [{ required: true, message: '项目不能为空', trigger: ['change', 'blur'] }] },
		{ prop: 'tableName', label: '表名' }
	],
	tableColumns: [
		{
			key: 'selection',
			type: 'selection',
			label: '',
			width: 60,
			align: 'center',
			headerAlign: 'center',
			disabled: true,
			// 已导入过的表不允许再勾选，避免重复导入；表头全选也会跳过这些行
			selectable: (row: any) => !row.exist
		},
		createBuiltinIndex(),
		{ key: 'databaseName', label: '数据库名称' },
		{ key: 'tableName', label: '表名' },
		{ key: 'tableComment', label: '注释' }
	],
	// 弹窗内不需要表格工具栏（搜索/刷新/列设置/最大化），与改造前的裸 el-card 一致
	showTableHeader: false,
	// 原页面表格 max-height 为 450px，比全局默认的 60vh 矮，避免弹窗内出现双层滚动条
	maxHeight: '450px'
}

const tableHook = useCrudTable(tableConfig)

// 初始化操作
const init = () => {
	visible.value = true
	nextTick(() => {
		// 弹窗没有 destroy-on-close，QueryTable 与 hook 会跨次打开存活：
		// 上次的勾选镜像和上次的列表数据都还在，必须显式清掉，
		// 否则这次打开即便一条没勾，「导入」按钮仍是可点状态且提交会带上次的表名。
		tableHook.selection.clear()
		tableHook.table.data = []
		tableHook.query.reset()

		genProjectApi.entityList().then(data => {
			projectList.value = data
		})
	})
}

const submitState = shallowReactive<SubmitOptions>({
	visible,
	successMessage: '导入成功',
	submitApi: genTableApi.importData,
	emit
})

const { submitLoading, submitHandle } = useSubmitHandler(submitState)

// 表单提交
const submitData = () => {
	// selection.ids 按 primaryKey(tableName) 映射，与改造前的 dataListSelections 内容一致
	const tableNameList = [...tableHook.selection.ids]

	if (tableNameList.length === 0) {
		ElMessage.warning('请选择要导入的表')
		return
	}
	const dataForm = {
		tableNameList,
		projectId: tableHook.search.form.projectId
	}
	submitHandle(dataForm)
}

defineExpose({
	init
})
</script>
