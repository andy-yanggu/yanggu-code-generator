<template>
	<div>
		<query-table :table-hook="tableHook">
			<!-- 工具栏左侧：导入 + 批量删除 + 批量生成 -->
			<template #toolbar-left>
				<el-space size="default">
					<el-button type="primary" :icon="Upload" @click="importHandle()">导入</el-button>
					<el-button type="danger" :loading="tableHook.action.delete.loading" :icon="Delete" @click="tableHook.action.delete.execute()">删除</el-button>
					<el-button type="success" :icon="DocumentAdd" @click="generatorCodeBatch()">生成</el-button>
				</el-space>
			</template>

			<!-- 查询字段：项目下拉。DynamicField 不支持自定义 el-option 内渲染，走插槽手写 -->
			<template #query-projectId>
				<el-select v-model="tableHook.search.form.projectId" style="width: 140px" placeholder="请选择项目" filterable clearable>
					<el-option v-for="item in projectList" :key="item.id!" :label="item.projectName" :value="item.id!">
						<option-label :label="item.projectName" :desc="item.projectDesc"></option-label>
					</el-option>
				</el-select>
			</template>

			<!-- 表格列：字段数量渲染为可点击链接，点击打开字段配置弹窗 -->
			<template #table-fieldCount="{ row }">
				<el-button type="primary" link @click="editHandle(row)">{{ row.fieldCount }}个</el-button>
			</template>

			<!-- 操作列：配置 / 修改 / 预览 / 更多（生成、同步、删除） -->
			<template #operation="{ row }">
				<el-row>
					<el-col :span="12">
						<el-button type="primary" link :icon="Setting" @click="editHandle(row)">配置</el-button>
					</el-col>
					<el-col :span="12">
						<el-button type="primary" link :icon="Edit" @click="formInitHandle(row.id)">修改</el-button>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="12">
						<el-button type="primary" link :icon="View" @click="previewHandle(row)">预览</el-button>
					</el-col>
					<el-col :span="12">
						<el-dropdown>
							<el-button type="primary" link :icon="More">更多</el-button>
							<template #dropdown>
								<el-dropdown-menu>
									<el-dropdown-item :icon="DocumentAdd" @click="generatorCode(row)">生成</el-dropdown-item>
									<el-dropdown-item :icon="Refresh" @click="syncHandle(row)">同步</el-dropdown-item>
									<el-popconfirm
										:title="`确定删除${tableHook.refs.subject}【${row[tableHook.refs.deleteNameKey] || '该记录'}】吗？`"
										placement="top"
										@confirm="tableHook.action.delete.row(row)"
										@cancel="ElMessage.info('删除已取消')"
									>
										<template #reference>
											<span>
												<el-dropdown-item :icon="Delete">删除</el-dropdown-item>
											</span>
										</template>
									</el-popconfirm>
								</el-dropdown-menu>
							</template>
						</el-dropdown>
					</el-col>
				</el-row>
			</template>
		</query-table>

		<!-- 弹窗, 新增 / 修改 -->
		<gen-table-form ref="formRef" @refresh-data-list="tableHook.query.getDataList"></gen-table-form>

		<!-- 导入表组件 -->
		<import ref="importRef" @refresh-data-list="tableHook.query.getDataList"></import>

		<!-- 预览 -->
		<preview ref="previewRef"></preview>

		<!-- 字段配置 -->
		<field-config ref="editRef" @refresh-data-list="tableHook.query.getDataList"></field-config>

		<!-- 代码生成 -->
		<template-index ref="templateIndexRef" @clear-selection="clearSelectionHandler()"></template-index>
	</div>
</template>

<script setup lang="ts">
import Import from '@/views/gen/table/import.vue'
import GenTableForm from '@/views/gen/table/form.vue'
import Preview from '@/business/preview/index.vue'
import FieldConfig from '@/views/gen/table/field-config.vue'
import TemplateIndex from '@/views/gen/table/template-index.vue'
import QueryTable from '@/components/crud/QueryTable.vue'
import OptionLabel from '@/components/option/label/index.vue'
import { GENERATOR_PRODUCT_TYPES, PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { getDictTag } from '@/utils/enum'
import { createBuiltinCreateTime, createBuiltinIndex, createBuiltinOperation, createBuiltinSelection, createBuiltinUpdateTime, useCrudTable, useInitForm } from '@/hooks'
import { Delete, DocumentAdd, Edit, More, Refresh, Setting, Upload, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { genProjectApi, genTableApi } from '@/api'
import type { GenProjectEntity, GenTableEntity, GenTableQuery, KeyArray, SearchFieldSchema, TableFieldSchema } from '@/types'

defineOptions({
	name: 'GenTable'
})

onMounted(() => {
	getProjectList()
})

const importRef = ref()
const editRef = ref()
const previewRef = ref()
const templateIndexRef = ref()
const projectList = ref([] as GenProjectEntity[])

const getProjectList = () => {
	genProjectApi.entityList().then(data => {
		projectList.value = data
	})
}

const importHandle = () => {
	importRef.value.init()
}

const previewHandle = (row: GenTableEntity) => {
	previewRef.value.init(row.id, row.tableName, row.projectId, (row as any).generatorType, GENERATOR_PRODUCT_TYPES.Table)
}

const editHandle = (row: GenTableEntity) => {
	editRef.value.init(row)
}

// 生成代码：单行
const generatorCode = (row: GenTableEntity) => {
	templateIndexRef.value.init(row.tableTemplateGroupId, (row as any).generatorType, [row.id])
}

// 生成代码：批量。先做同项目校验，通过后打开生成弹窗
const generatorCodeBatch = () => {
	const ids: KeyArray = tableHook.selection.ids
	if (ids.length === 0) {
		ElMessage.warning('请选择要生成代码的表')
		return
	}
	genTableApi.generateCheck(ids).then(data => {
		const { checkResult, tableTemplateGroupId, generatorType } = data
		if (!checkResult) {
			ElMessage.warning('当前选择的表不是同一个项目')
		} else {
			templateIndexRef.value.init(tableTemplateGroupId, generatorType, ids)
		}
	})
}

const syncHandle = (row: GenTableEntity) => {
	ElMessageBox.confirm(`确定同步项目${row.projectName}下表${row.tableName}的字段吗?`, '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	}).then(() => {
		genTableApi.sync(row.id as number).then(() => {
			ElMessage.success('字段同步成功')
		})
	})
}

// 生成弹窗完成后清空勾选，避免下次批量生成带上已生成的表
const clearSelectionHandler = () => {
	tableHook.selection.clear()
}

// ====== 搜索字段 ======
const searchSchema: SearchFieldSchema[] = [
	{ key: 'projectId', label: '项目', component: 'select' },
	{ key: 'databaseName', label: '数据库名称' },
	{ key: 'tableName', label: '表名' }
]

// ====== 表格列（选择/序号/时间/操作走 use-crud-table 内置列工厂；操作列按业务放宽到 200） ======
const tableColumns: TableFieldSchema[] = [
	createBuiltinSelection(),
	createBuiltinIndex(),
	{ key: 'projectName', label: '项目名称' },
	{ key: 'databaseName', label: '数据库名称', width: 100 },
	{ key: 'tableName', label: '表名' },
	{ key: 'generatorType', label: '生成方式', width: 120, formatter: getDictTag(PROJECT_GENERATE_TYPES) },
	{ key: 'tableComment', label: '注释' },
	{ key: 'fieldCount', label: '字段数量', width: 100 },
	createBuiltinCreateTime(),
	createBuiltinUpdateTime(),
	{ ...createBuiltinOperation(), width: 160 }
]

// ====== 表格 Hook（承载搜索/分页/排序/多选/删除，页面自身不再持有 state） ======
const tableHook = useCrudTable<GenTableEntity, GenTableQuery>({
	subject: '表',
	deleteNameKey: 'tableName',
	dataListApi: genTableApi.voPage,
	deleteApi: genTableApi.deleteList,
	initQueryForm: (): GenTableQuery => ({
		tableName: '',
		projectId: '',
		databaseName: ''
	}),
	searchSchema,
	tableColumns
})

// form.vue 暂保留旧式 ref 弹窗，本 hook 只提供 formRef 与 formInitHandle(id)
const { formRef, formInitHandle } = useInitForm()
</script>

<style scoped>
:deep(.el-dropdown) {
	vertical-align: middle; /* 行内对齐中线 */
}
</style>
