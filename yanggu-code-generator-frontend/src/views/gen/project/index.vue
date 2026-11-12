<template>
	<div>
		<query-table :table-hook="tableHook">
			<!-- 工具栏左侧：新增 + 批量删除 -->
			<template #toolbar-left>
				<el-space size="default">
					<el-button type="primary" :icon="Plus" @click="formInitHandle()">新增</el-button>
					<el-button type="danger" :loading="tableHook.action.delete.loading" :icon="Delete" @click="tableHook.action.delete.execute()">删除</el-button>
				</el-space>
			</template>

			<!-- 操作列：修改 / 生成 / 预览 / 删除（2×2 网格；行内删除用 Popconfirm 承载级联警告，工具栏批量删除仍走 MessageBox） -->
			<template #operation="{ row }">
				<el-row>
					<el-col :span="12">
						<el-button type="primary" link :icon="Edit" @click="formInitHandle(row.id)">修改</el-button>
					</el-col>
					<el-col :span="12">
						<el-button type="success" link :icon="DocumentAdd" @click="generatorCodeV2(row)">生成</el-button>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="12">
						<el-button type="primary" link :icon="View" @click="previewHandle(row)">预览</el-button>
					</el-col>
					<el-col :span="12">
						<el-popconfirm
							title="删除项目会删除项目下的所有表，是否继续？"
							placement="top"
							@confirm="tableHook.action.delete.row(row)"
							@cancel="ElMessage.info('删除已取消')"
						>
							<template #reference>
								<el-button type="danger" link :icon="Delete">删除</el-button>
							</template>
						</el-popconfirm>
					</el-col>
				</el-row>
			</template>
		</query-table>

		<!-- 弹窗, 新增 / 修改 -->
		<project-form ref="formRef" @refresh-data-list="tableHook.query.getDataList"></project-form>

		<!-- 预览 -->
		<preview ref="previewRef"></preview>

		<!-- 生成代码步骤条 V2 -->
		<steps-v2 ref="stepsV2Ref"></steps-v2>
	</div>
</template>

<script setup lang="ts">
import Preview from '@/business/preview/index.vue'
import ProjectForm from '@/views/gen/project/form.vue'
import StepsV2 from '@/views/gen/project/steps-v2.vue'
import QueryTable from '@/components/crud/QueryTable.vue'
import { GENERATOR_PRODUCT_TYPES, PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { getDictTag } from '@/utils/enum'
import { createBuiltinCreateTime, createBuiltinIndex, createBuiltinOperation, createBuiltinSelection, useCrudTable, useInitForm } from '@/hooks'
import { ElMessage } from 'element-plus'
import { Delete, DocumentAdd, Edit, Plus, View } from '@element-plus/icons-vue'
import { genProjectApi } from '@/api'
import type { GenProjectEntity, GenProjectQuery, SearchFieldSchema, TableFieldSchema } from '@/types'

defineOptions({
	name: 'GenProject'
})

const previewRef = ref()
const stepsV2Ref = ref()

const previewHandle = (row: GenProjectEntity) => {
	previewRef.value.init(row.id, row.projectName, row.id, row.generatorType, GENERATOR_PRODUCT_TYPES.Project)
}

const generatorCodeV2 = (row: GenProjectEntity) => {
	stepsV2Ref.value.init(row)
}

// ====== 搜索字段 ======
// dateTimeRange：后端契约字段是 startTime / endTime，靠 rangeFields 让 hook 拆数组
// defaultTime 保证末日晚间记录不被后端 between(createTime, start, end) 漏掉
const searchSchema: SearchFieldSchema[] = [
	{ key: 'projectName', label: '项目名称', width: 140 },
	{ key: 'generatorType', label: '生成方式', component: 'select', options: PROJECT_GENERATE_TYPES.items, width: 150 },
	{
		key: 'dateTimeRange',
		label: '创建时间',
		component: 'datetime-range',
		rangeFields: ['startTime', 'endTime'],
		defaultTime: [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]
	}
]

// ====== 表格列 ======
// 原页无 updateTime 列，此处不加 createBuiltinUpdateTime()
const tableColumns: TableFieldSchema[] = [
	createBuiltinSelection(),
	createBuiltinIndex(),
	{ key: 'projectName', label: '项目名称' },
	{ key: 'projectPort', label: '项目端口' },
	{ key: 'author', label: '作者' },
	{ key: 'projectDesc', label: '项目描述' },
	{ key: 'generatorType', label: '生成方式', formatter: getDictTag(PROJECT_GENERATE_TYPES), minWidth: 90 },
	{ key: 'projectPackage', label: '项目包名' },
	{ key: 'projectTemplateGroupName', label: '项目模板组' },
	createBuiltinCreateTime(),
	{ ...createBuiltinOperation(), width: 160 }
]

// ====== 表格 Hook ======
// deleteConfirmMessage 只驱动工具栏"批量删除"的 MessageBox；行内单条删除走 Popconfirm（文案见 #operation 插槽）。
// 修正旧 bug：原代码写的是 state.deleteMessage，use-table-action 读的是 state.deleteConfirmMessage，
// 导致这段"删除会级联删表"的警告文案从未弹出。
const tableHook = useCrudTable<GenProjectEntity, GenProjectQuery>({
	subject: '项目',
	deleteNameKey: 'projectName',
	dataListApi: genProjectApi.voPage,
	deleteApi: genProjectApi.deleteList,
	deleteConfirmMessage: '删除项目会删除项目下的所有表，是否继续？',
	initQueryForm: (): GenProjectQuery => ({
		projectName: '',
		generatorType: '',
		dateTimeRange: []
	}),
	searchSchema,
	tableColumns
})

// form.vue 保留旧式 ref 弹窗
const { formRef, formInitHandle } = useInitForm()
</script>
