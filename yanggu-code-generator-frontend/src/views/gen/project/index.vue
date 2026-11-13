<template>
	<div>
		<crud-page :table-hook="tableHook" :form-hook="formHook">
			<!-- 工具栏左侧：新增 + 批量删除（覆盖默认按钮） -->
			<template #table-toolbar-left="{ tableHook: t }">
				<el-space size="default">
					<el-button type="primary" :icon="Plus" @click="formHook.dialog.open('add')">新增</el-button>
					<el-button type="danger" :loading="t.action.delete.loading" :icon="Delete" @click="t.action.delete.execute()">删除</el-button>
				</el-space>
			</template>

			<!-- 表单字段：项目端口（v-model.number 保证写入数字，配合 type:number 校验） -->
			<template #form-projectPort="{ model }">
				<el-input v-model.number="model.projectPort" placeholder="请输入项目端口" clearable></el-input>
			</template>

			<!-- 表单字段：数据源（自定义 option：名称 + 描述） -->
			<template #form-datasourceId="{ model }">
				<el-select v-model="model.datasourceId" clearable filterable placeholder="请选择数据源" style="width: 100%">
					<el-option v-for="item in datasourceList" :key="item.id!" :label="item.connName" :value="item.id!">
						<option-label :label="item.connName" :desc="item.datasourceDesc"></option-label>
					</el-option>
				</el-select>
			</template>

			<!-- 表单字段：模块列表（动态数组子表单，逐行增删 + 交叉校验，保留原始 UI） -->
			<template #form-moduleList="{ model }">
				<el-row
					v-for="(item, index) in model.moduleList as GenModuleEntity[]"
					:key="index"
					:gutter="10"
					:style="{ marginBottom: index < (model.moduleList as GenModuleEntity[]).length - 1 ? '18px' : '0' }"
				>
					<el-col :span="6.5">
						<el-form-item :prop="`moduleList[${index}].moduleName`">
							<el-input v-model="item.moduleName" clearable placeholder="请输入模块名称" style="width: 180px"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="6.5">
						<el-form-item :prop="`moduleList[${index}].modulePath`">
							<el-input v-model="item.modulePath" clearable placeholder="请输入模块路径" style="width: 180px"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="6.5">
						<el-form-item :prop="`moduleList[${index}].moduleDesc`">
							<el-input v-model="item.moduleDesc" clearable placeholder="请输入模块描述" style="width: 180px"></el-input>
						</el-form-item>
					</el-col>
					<div class="module-actions">
						<el-space size="default">
							<el-button circle :icon="Plus" type="primary" @click="model.moduleList.splice(index + 1, 0, emptyModule())"></el-button>
							<el-button v-show="model.moduleList.length > 1" type="danger" circle :icon="Delete" @click="model.moduleList.splice(index, 1)"></el-button>
						</el-space>
					</div>
				</el-row>
			</template>

			<!-- 表单字段：表 / 枚举 / 项目模板组（自定义 option：名称 + 描述） -->
			<template #form-tableTemplateGroupId="{ model }">
				<el-select v-model="model.tableTemplateGroupId" placeholder="请选择表模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in tableTemplateGroupList" :key="item.id!" :label="item.groupName" :value="item.id!">
						<option-label :label="item.groupName" :desc="item.groupDesc"></option-label>
					</el-option>
				</el-select>
			</template>
			<template #form-enumTemplateGroupId="{ model }">
				<el-select v-model="model.enumTemplateGroupId" placeholder="请选择枚举模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in enumTemplateGroupList" :key="item.id!" :label="item.groupName" :value="item.id!">
						<option-label :label="item.groupName" :desc="item.groupDesc"></option-label>
					</el-option>
				</el-select>
			</template>
			<template #form-projectTemplateGroupId="{ model }">
				<el-select v-model="model.projectTemplateGroupId" placeholder="请选择项目模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in projectTemplateGroupList" :key="item.id!" :label="item.groupName" :value="item.id!">
						<option-label :label="item.groupName" :desc="item.groupDesc"></option-label>
					</el-option>
				</el-select>
			</template>

			<!-- 表单虚拟字段（bare）：分段标题 -->
			<template #form-divider-basic>
				<form-divider title="基础信息"></form-divider>
			</template>
			<template #form-divider-template-config>
				<form-divider title="模板组配置"></form-divider>
			</template>
			<template #form-divider-other>
				<form-divider title="其他配置"></form-divider>
			</template>

			<!-- 表单虚拟字段（bare）：项目模板组属性标题 + 动态子表单（仅当所选项目模板组有属性时显示） -->
			<template #form-templateGroupProperty="{ model }">
				<template v-if="isNotEmpty(projectTemplateGroupPropertyList)">
					<form-divider title="项目模板组属性"></form-divider>
					<template-group-property-form
						:key="model.projectTemplateGroupId"
						v-model:form-data="model.templateGroupPropertyData"
						model-value-prop="templateGroupPropertyData"
						:property-list="projectTemplateGroupPropertyList"
					></template-group-property-form>
				</template>
			</template>

			<!-- 操作列：修改 / 生成 / 预览 / 删除（2×2 网格；行内删除用 Popconfirm 承载级联警告，工具栏批量删除走 MessageBox） -->
			<template #table-action="{ row, tableHook: t, dialog }">
				<el-row :gutter="5">
					<el-col :span="12">
						<el-button type="primary" link :icon="Edit" @click="dialog?.open('update', row[t.refs.primaryKey])">修改</el-button>
					</el-col>
					<el-col :span="12">
						<el-button type="success" link :icon="DocumentAdd" @click="generatorCodeV2(row)">生成</el-button>
					</el-col>
				</el-row>
				<el-row :gutter="5">
					<el-col :span="12">
						<el-button type="primary" link :icon="View" @click="previewHandle(row)">预览</el-button>
					</el-col>
					<el-col :span="12">
						<el-popconfirm title="删除项目会删除项目下的所有表，是否继续？" placement="top" @confirm="t.action.delete.row(row)" @cancel="ElMessage.info('删除已取消')">
							<template #reference>
								<el-button type="danger" link :icon="Delete">删除</el-button>
							</template>
						</el-popconfirm>
					</el-col>
				</el-row>
			</template>
		</crud-page>

		<!-- 预览 -->
		<preview ref="previewRef"></preview>

		<!-- 生成代码步骤条 V2 -->
		<steps-v2 ref="stepsV2Ref"></steps-v2>
	</div>
</template>

<script setup lang="ts">
import Preview from '@/business/preview/index.vue'
import StepsV2 from '@/views/gen/project/steps-v2.vue'
import CrudPage from '@/components/crud/CrudPage.vue'
import FormDivider from '@/components/form/divider/index.vue'
import TemplateGroupPropertyForm from '@/views/gen/template-group-property/property-form.vue'
import OptionLabel from '@/components/option/label/index.vue'
import { GENERATOR_PRODUCT_TYPES, PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { getDictTag } from '@/utils/enum'
import { createBuiltinCreateTime, createBuiltinIndex, createBuiltinOperation, createBuiltinSelection } from '@/hooks'
import { useCrud } from '@/hooks/use-crud'
import { isNotEmpty } from '@/utils/tool'
import { Delete, DocumentAdd, Edit, Plus, View } from '@element-plus/icons-vue'
import type { FormItemRule } from 'element-plus'
import { genDatasourceApi, genProjectApi, genTemplateGroupApi } from '@/api'
import type {
	CrudField,
	GenDatasourceEntity,
	GenModuleEntity,
	GenProjectEntity,
	GenProjectQuery,
	GenTemplateGroupEntity,
	SearchFieldSchema,
	TableFieldSchema,
	UseCrudFormOptions,
	UseCrudTableOptions
} from '@/types'

defineOptions({
	name: 'GenProject'
})

const previewRef = ref()
const stepsV2Ref = ref()

const datasourceList = ref([] as GenDatasourceEntity[])
const projectTemplateGroupList = ref([] as GenTemplateGroupEntity[])
const tableTemplateGroupList = ref([] as GenTemplateGroupEntity[])
const enumTemplateGroupList = ref([] as GenTemplateGroupEntity[])

onMounted(() => {
	// 数据源下拉
	genDatasourceApi.entityList().then(data => {
		datasourceList.value = data
	})
	// 模板组下拉（按 type 分流：0 项目 / 1 表 / 2 枚举；voList 已内联 propertyList）
	genTemplateGroupApi.voList().then(data => {
		projectTemplateGroupList.value = data.filter(item => item.type === 0)
		tableTemplateGroupList.value = data.filter(item => item.type === 1)
		enumTemplateGroupList.value = data.filter(item => item.type === 2)
	})
})

const emptyModule = (): GenModuleEntity => ({
	moduleName: '',
	modulePath: '',
	moduleDesc: ''
})

// ====== 表单字段声明（fields 仅驱动 formSchema；表格列/查询因顺序与表单冲突，改由 tableConfig 显式声明） ======
const fields: CrudField[] = [
	{ key: 'divider-basic', label: '基础信息', inForm: true, form: { display: 'bare' } },
	{
		key: 'projectName',
		label: '项目名称',
		tooltip: "使用英文小写字母，单词之间使用'-'拼接；该字段具有唯一性",
		inForm: true,
		form: { ruleList: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }] }
	},
	{
		key: 'projectVersion',
		label: '项目版本',
		inForm: true,
		form: { ruleList: [{ required: true, message: '项目版本不能为空', trigger: 'blur' }] }
	},
	{
		key: 'generatorType',
		label: '生成方式',
		inForm: true,
		component: 'radio',
		options: PROJECT_GENERATE_TYPES.items,
		form: { ruleList: [{ required: true, message: '生成类型不能为空', trigger: 'blur' }] }
	},
	{
		key: 'projectPort',
		label: '项目端口',
		inForm: true,
		form: {
			ruleList: [
				{ required: true, message: '项目端口不能为空', trigger: 'blur' },
				{ type: 'number', message: '项目端口必须为数字', trigger: 'change' }
			]
		}
	},
	{ key: 'projectPackage', label: '项目包名', inForm: true },
	{ key: 'datasourceId', label: '数据源', inForm: true },
	{ key: 'author', label: '作者', inForm: true },
	{ key: 'projectDesc', label: '项目描述', inForm: true },
	// 模块列表：普通字段（保留 label「模块列表」），内容由 form-moduleList 插槽承载动态数组 UI
	{ key: 'moduleList', label: '模块列表', inForm: true },

	{ key: 'divider-template-config', label: '模板组配置', inForm: true, form: { display: 'bare' } },
	{ key: 'tableTemplateGroupId', label: '表模板组', inForm: true },
	{ key: 'enumTemplateGroupId', label: '枚举模板组', inForm: true },
	{
		key: 'projectTemplateGroupId',
		label: '项目模板组',
		inForm: true,
		form: { ruleList: [{ required: true, message: '项目模板组不能为空', trigger: 'blur' }] }
	},
	// 表单虚拟字段（bare）：项目模板组属性标题 + 动态子表单容器
	{ key: 'templateGroupProperty', label: '项目模板组属性', inForm: true, form: { display: 'bare' } },

	{ key: 'divider-other', label: '其他配置', inForm: true, form: { display: 'bare' } },
	{ key: 'backendPath', label: '后端路径', tooltip: "以 '/' 分隔", inForm: true },
	{ key: 'frontendPath', label: '前端路径', tooltip: "以 '/' 分隔", inForm: true }
]

/**
 * 动态校验规则（getter 形态传入，理由见 use-crud-form extraRules）：
 * 1. 项目模板组属性 required===1 的项 → templateGroupPropertyData.{propKey} 必填；
 * 2. moduleList 逐行校验器（沿用旧逻辑：moduleList 整体非必填，单空行跳过，非空行各字段必填）。
 * 体内引用的 projectTemplateGroupPropertyList / formHook 为闭包惰性访问，仅在表单校验时调用，
 * 届时 setup 已执行完毕，不存在 TDZ 问题。
 */
const buildExtraRules = (): Record<string, FormItemRule[]> => {
	const rules: Record<string, FormItemRule[]> = {}

	// 1. 模板组属性必填
	projectTemplateGroupPropertyList.value?.forEach(item => {
		if (item.required === 1) {
			rules[`templateGroupPropertyData.${item.propKey}`] = [{ required: true, message: `${item.propTitle}不能为空`, trigger: 'blur' }]
		}
	})

	// 2. moduleList 逐行校验
	const list = formHook.form.data.moduleList || []
	const moduleFieldLabels: Record<string, string> = { moduleName: '模块名称', modulePath: '模块路径', moduleDesc: '模块描述' }

	// 整个 list 只有 1 个且全空 → 视为未填写，跳过校验
	const isSingleEmptyModule = () => {
		if (list.length !== 1) return false
		const item = list[0]
		return !item.moduleName && !item.modulePath && !item.moduleDesc
	}

	const makeValidator = (_: number) => (field: string) => ({
		validator(_: any, value: any, callback: any) {
			if (isSingleEmptyModule()) {
				callback()
				return
			}
			if (!value || !value.trim()) {
				callback(new Error(`${moduleFieldLabels[field]}不能为空`))
				return
			}
			callback()
		},
		trigger: 'blur'
	})

	list.forEach((_, index) => {
		const validator = makeValidator(index)
		rules[`moduleList[${index}].moduleName`] = [validator('moduleName')]
		rules[`moduleList[${index}].modulePath`] = [validator('modulePath')]
		rules[`moduleList[${index}].moduleDesc`] = [validator('moduleDesc')]
	})

	return rules
}

// ====== 表单配置 ======
const formConfig = reactive({
	initFormData: (): GenProjectEntity => ({
		id: '',
		projectName: '',
		projectPackage: '',
		projectPort: '',
		projectVersion: '',
		datasourceId: '',
		moduleList: [emptyModule()],
		projectTemplateGroupId: '',
		templateGroupPropertyData: {},
		tableTemplateGroupId: '',
		enumTemplateGroupId: '',
		backendPath: '',
		frontendPath: '',
		projectDesc: '',
		author: '',
		generatorType: ''
	}),
	submitApi: genProjectApi.submit,
	detailApi: genProjectApi.detail,
	extraRules: buildExtraRules,
	labelWidth: '120px',
	dialogWidth: '65%',
	// 新增且选择了数据源时，后端会导入该数据源下所有表，给出专门提示
	successMessage: (mode: string, data: GenProjectEntity) => {
		if (mode === 'add' && data.datasourceId) {
			return '新增成功，已经导入该项目引用数据源下的所有表，请到表管理中进行查看'
		}
		return `项目${mode === 'update' ? '修改' : '新增'}成功`
	}
} as UseCrudFormOptions)

// ====== 查询字段（顺序独立于表单，显式声明） ======
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

// ====== 表格列（顺序独立于表单，显式声明；原页无 updateTime 列） ======
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

// ====== 表格配置 ======
// deleteConfirmMessage 只驱动工具栏"批量删除"的 MessageBox；行内单条删除走 Popconfirm（文案见 #table-action 插槽）。
const tableConfig = reactive({
	deleteNameKey: 'projectName',
	dataListApi: genProjectApi.voPage,
	deleteApi: genProjectApi.deleteList,
	deleteConfirmMessage: '删除项目会删除项目下的所有表，是否继续？',
	initQueryData: (): GenProjectQuery => ({
		projectName: '',
		generatorType: '',
		dateTimeRange: []
	}),
	searchSchema,
	tableColumns
} as UseCrudTableOptions)

// ====== 统一 hook ======
const { tableHook, formHook } = useCrud<GenProjectEntity, GenProjectQuery, GenProjectEntity>({
	subject: '项目',
	fields,
	tableConfig,
	formConfig
})

// 所选项目模板组的属性清单（voList 已内联 propertyList，无需额外请求）；驱动动态子表单显隐与必填校验
const projectTemplateGroupPropertyList = computed(() => {
	const find = projectTemplateGroupList.value.find(item => item.id === formHook.form.data.projectTemplateGroupId)
	return find?.propertyList ?? []
})

const previewHandle = (row: GenProjectEntity) => {
	previewRef.value.init(row.id, row.projectName, row.id, row.generatorType, GENERATOR_PRODUCT_TYPES.Project)
}

const generatorCodeV2 = (row: GenProjectEntity) => {
	stepsV2Ref.value.init(row)
}
</script>

<style lang="scss" scoped>
.module-actions {
	width: 60px; /* 固定宽度，解决大屏抖动问题 */
	display: flex;
	align-items: center;
	padding-left: 10px;
}
</style>
