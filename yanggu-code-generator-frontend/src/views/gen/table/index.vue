<template>
	<div>
		<crud-page :table-hook="tableHook" :form-hook="formHook">
			<!-- 工具栏左侧：导入 + 删除 + 生成（覆盖默认按钮；表无新增，均由导入产生） -->
			<template #table-toolbar-left="{ tableHook: t }">
				<el-space size="default">
					<el-button type="primary" :icon="Upload" @click="importHandle()">导入</el-button>
					<el-button type="danger" :loading="t.action.delete.loading" :icon="Delete" @click="t.action.delete.execute()">删除</el-button>
					<el-button type="success" :icon="DocumentAdd" @click="generatorCodeBatch()">生成</el-button>
				</el-space>
			</template>

			<!-- 查询字段：项目下拉。需自定义 el-option（label + desc），走插槽手写 -->
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

			<!-- 表单字段：所属项目（禁用，随导入确定） -->
			<template #form-projectId="{ model }">
				<el-select v-model="model.projectId" :options="projectList" :props="{ label: 'projectName', value: 'id' }" clearable placeholder="请选择项目" disabled></el-select>
			</template>

			<!-- 表单字段：所属模块（依赖所选项目的 moduleList，需 option-label） -->
			<template #form-moduleName="{ model }">
				<el-select v-model="model.moduleName" clearable filterable placeholder="请选择所属模块" style="width: 100%">
					<el-option v-for="item in projectList.find(temp => temp.id === model.projectId)?.moduleList" :key="item.moduleName" :label="item.moduleName" :value="item.moduleName">
						<option-label :label="item.moduleName" :desc="item.moduleDesc"></option-label>
					</el-option>
				</el-select>
			</template>

			<!-- 表单字段：表名（禁用，来自数据库） -->
			<template #form-tableName="{ model }">
				<el-input v-model="model.tableName" clearable placeholder="请输入表名" disabled></el-input>
			</template>

			<!-- 表单字段：Entity 基类（自定义 label，需 option-label） -->
			<template #form-entityBaseClassId="{ model }">
				<el-select v-model="model.entityBaseClassId" placeholder="请选择Entity基类" style="width: 100%" clearable filterable>
					<el-option v-for="item in baseClassList" :key="item.id!" :label="`${item.packageName}.${item.className}`" :value="item.id!">
						<option-label :label="`${item.packageName}.${item.className}`" :desc="item.remark"></option-label>
					</el-option>
				</el-select>
			</template>

			<!-- 表单字段：VO 基类（自定义 label，需 option-label） -->
			<template #form-voBaseClassId="{ model }">
				<el-select v-model="model.voBaseClassId" placeholder="请选择VO基类" style="width: 100%" clearable filterable>
					<el-option v-for="item in baseClassList" :key="item.id!" :label="`${item.packageName}.${item.className}`" :value="item.id!">
						<option-label :label="`${item.packageName}.${item.className}`" :desc="item.remark"></option-label>
					</el-option>
				</el-select>
			</template>

			<!-- 表单虚拟字段（bare）：分段标题 -->
			<template #form-divider-basic>
				<form-divider title="基础信息"></form-divider>
			</template>
			<template #form-divider-func>
				<form-divider title="功能配置"></form-divider>
			</template>
			<template #form-divider-base>
				<form-divider title="基类配置"></form-divider>
			</template>

			<!-- 表单虚拟字段（bare）：表模板组属性标题 + 动态子表单（仅当该表所属模板组有属性时显示） -->
			<template #form-templateGroupProperty="{ model }">
				<template v-if="isNotEmpty(tableTemplateGroupPropertyList)">
					<form-divider title="表模板组属性"></form-divider>
					<template-group-property-form
						:key="model.tableTemplateGroupId"
						v-model:form-data="model.templateGroupPropertyData"
						:property-list="tableTemplateGroupPropertyList"
					></template-group-property-form>
				</template>
			</template>

			<!-- 操作列：配置 / 修改 / 预览 / 更多（生成、同步、删除） -->
			<template #table-action="{ row, tableHook: t, dialog }">
				<el-row :gutter="5">
					<el-col :span="12">
						<el-button type="primary" link :icon="Setting" @click="editHandle(row)">配置</el-button>
					</el-col>
					<el-col :span="12">
						<el-button type="primary" link :icon="Edit" @click="dialog?.open('update', row[t.refs.primaryKey])">修改</el-button>
					</el-col>
				</el-row>
				<el-row :gutter="5">
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
									<el-dropdown-item :icon="Delete" @click="t.action.delete.execute(row)">删除</el-dropdown-item>
								</el-dropdown-menu>
							</template>
						</el-dropdown>
					</el-col>
				</el-row>
			</template>
		</crud-page>

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
import Preview from '@/business/preview/index.vue'
import FieldConfig from '@/views/gen/table/field-config.vue'
import TemplateIndex from '@/views/gen/table/template-index.vue'
import CrudPage from '@/components/crud/CrudPage.vue'
import FormDivider from '@/components/form/divider/index.vue'
import TemplateGroupPropertyForm from '@/views/gen/template-group-property/property-form.vue'
import OptionLabel from '@/components/option/label/index.vue'
import { FORM_LAYOUT_TYPES, GENERATOR_PRODUCT_TYPES, PROJECT_GENERATE_TYPES, TABLE_GENERATOR_FUNCTION_TYPES, TABLE_POPUP_TYPES } from '@/constant/enum'
import { getDictTag } from '@/utils/enum'
import { useCrud } from '@/hooks/use-crud'
import { isNotEmpty } from '@/utils/tool'
import { Delete, DocumentAdd, Edit, More, Refresh, Setting, Upload, View } from '@element-plus/icons-vue'
import type { FormItemRule } from 'element-plus'
import { ElMessage } from 'element-plus'
import { genBaseClassApi, genProjectApi, genTableApi, genTemplateGroupPropertyApi } from '@/api'
import type {
	CrudField,
	GenBaseClassEntity,
	GenProjectEntity,
	GenTableEntity,
	GenTableQuery,
	GenTemplateGroupPropertyEntity,
	KeyArray,
	UseCrudFormOptions,
	UseCrudTableOptions
} from '@/types'

defineOptions({
	name: 'GenTable'
})

const importRef = ref()
const editRef = ref()
const previewRef = ref()
const templateIndexRef = ref()
const projectList = ref([] as GenProjectEntity[])
const baseClassList = ref([] as GenBaseClassEntity[])
const tableTemplateGroupPropertyList = ref([] as GenTemplateGroupPropertyEntity[])

onMounted(() => {
	// 项目下拉（查询插槽 + 表单项目/模块联动共用）
	genProjectApi.entityList().then(data => {
		projectList.value = data
	})
	// 基类下拉（表单 Entity/VO 基类选择）
	genBaseClassApi.entityList().then(data => {
		baseClassList.value = data
	})
})

// ====== 统一字段声明 ======
// 单份 fields 同时派生表单/查询/表格：表单是天然单列（每个字段一整行 el-form-item）。
// 表格列的相对顺序由 inTable 字段在数组中的先后决定，需与旧表格列顺序一致。
const fields: CrudField[] = [
	{ key: 'divider-basic', label: '基础信息', inForm: true, form: { display: 'bare' } },
	{
		key: 'projectId',
		label: '所属项目',
		inSearch: true,
		inForm: true,
		form: { ruleList: [{ required: true, message: '所属项目不能为空', trigger: 'blur' }] }
	},
	{ key: 'moduleName', label: '所属模块', inForm: true },
	{ key: 'projectName', label: '项目名称', inTable: true },
	{
		key: 'databaseName',
		label: '数据库名',
		inSearch: true,
		inForm: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: '数据库名不能为空', trigger: 'blur' }] }
	},
	{
		key: 'tableName',
		label: '表名',
		inSearch: true,
		inForm: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: '表名不能为空', trigger: 'blur' }] }
	},
	{
		key: 'className',
		label: '类名',
		inForm: true,
		form: { ruleList: [{ required: true, message: '类名不能为空', trigger: 'blur' }] }
	},
	{ key: 'generatorType', label: '生成方式', inTable: true, table: { width: 120, formatter: getDictTag(PROJECT_GENERATE_TYPES) } },
	{
		key: 'tableComment',
		label: '注释',
		inForm: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: '注释不能为空', trigger: 'blur' }] }
	},
	{ key: 'author', label: '作者', inForm: true },
	{ key: 'version', label: '版本', inForm: true },

	{ key: 'divider-func', label: '功能配置', inForm: true, form: { display: 'bare' } },
	{
		key: 'functionName',
		label: '功能名',
		inForm: true,
		form: { ruleList: [{ required: true, message: '功能名不能为空', trigger: 'blur' }] }
	},
	{ key: 'permissionFlag', label: '权限标识', inForm: true },
	{
		key: 'formLayout',
		label: '表单布局',
		inForm: true,
		component: 'radio',
		options: FORM_LAYOUT_TYPES.items,
		form: { ruleList: [{ required: true, message: '表单布局不能为空', trigger: 'blur' }] }
	},
	{
		key: 'popupType',
		label: '弹窗方式',
		inForm: true,
		component: 'radio',
		options: TABLE_POPUP_TYPES.items,
		form: { ruleList: [{ required: true, message: '弹窗方式不能为空', trigger: 'blur' }] }
	},
	{
		key: 'generatorFunction',
		label: '生成功能',
		inForm: true,
		component: 'checkbox',
		options: TABLE_GENERATOR_FUNCTION_TYPES.items,
		form: { ruleList: [{ required: true, message: '生成功能不能为空', trigger: 'blur' }] }
	},

	{ key: 'divider-base', label: '基类配置', inForm: true, form: { display: 'bare' } },
	{ key: 'entityBaseClassId', label: 'Entity基类', inForm: true },
	{ key: 'voBaseClassId', label: 'VO基类', inForm: true },
	{ key: 'fieldCount', label: '字段数量', inTable: true, table: { width: 100 } },

	// 表单虚拟字段（bare）：表模板组属性标题 + 动态子表单容器
	{ key: 'templateGroupProperty', label: '表模板组属性', inForm: true, form: { display: 'bare' } }
]

// 动态必填：模板组属性清单里 required===1 的项，追加 templateGroupPropertyData.{propKey} 校验。
// 用 getter 形态（而非 computed）传入：useCrud 内部会展开 formConfig，computed 会被拍平成快照丢失响应性；
// getter 在 rules computed 内每次调用，读到 tableTemplateGroupPropertyList.value 从而保持追踪。
const buildExtraRules = (): Record<string, FormItemRule[]> => {
	const rules: Record<string, FormItemRule[]> = {}
	tableTemplateGroupPropertyList.value.forEach(item => {
		if (item.required === 1) {
			rules[`templateGroupPropertyData.${item.propKey}`] = [{ required: true, message: `${item.propTitle}不能为空`, trigger: 'blur' }]
		}
	})
	return rules
}

// ====== 表单配置 ======
const formConfig = reactive({
	initFormData: (): GenTableEntity => ({
		id: null,
		tableName: '',
		databaseName: '',
		className: '',
		tableComment: '',
		projectId: -1,
		author: '',
		version: '',
		moduleName: '',
		functionName: '',
		voBaseClassId: '',
		entityBaseClassId: '',
		permissionFlag: '',
		formLayout: '',
		popupType: '',
		tableTemplateGroupId: '',
		templateGroupPropertyData: {},
		generatorFunction: [0, 1, 2, 3, 4, 5]
	}),
	submitApi: genTableApi.submit,
	detailApi: genTableApi.detail,
	extraRules: buildExtraRules,
	labelWidth: '130px',
	dialogWidth: '60%'
} as UseCrudFormOptions)

// ====== 表格配置 ======
const tableConfig = reactive({
	deleteNameKey: 'tableName',
	dataListApi: genTableApi.voPage,
	deleteApi: genTableApi.deleteList,
	initQueryForm: (): GenTableQuery => ({
		tableName: '',
		projectId: '',
		databaseName: ''
	})
} as UseCrudTableOptions)

// ====== 统一 hook ======
const { tableHook, formHook } = useCrud<GenTableEntity, GenTableQuery, GenTableEntity>({
	subject: '表',
	fields,
	tableConfig,
	formConfig
})

// 详情回填 tableTemplateGroupId 后加载该表模板组的属性清单（驱动动态子表单显隐）；
// 关闭/新增时 tableTemplateGroupId 变化亦会触发，重置为空避免残留上一次的属性。
watch(
	() => formHook.form.data.tableTemplateGroupId,
	templateGroupId => {
		tableTemplateGroupPropertyList.value = []
		if (!templateGroupId) {
			return
		}
		genTemplateGroupPropertyApi.entityList({ templateGroupId }).then(data => {
			tableTemplateGroupPropertyList.value = data
		})
	}
)

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
</script>

<style scoped>
:deep(.el-dropdown) {
	vertical-align: middle; /* 行内对齐中线 */
}
</style>
