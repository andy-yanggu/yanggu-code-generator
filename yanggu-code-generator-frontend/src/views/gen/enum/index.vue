<template>
	<div>
		<crud-page :table-hook="tableHook" :form-hook="formHook">
			<!-- 工具栏左侧：新增 + 删除 + 生成（覆盖默认按钮） -->
			<template #table-toolbar-left="{ tableHook: t }">
				<el-space size="default">
					<el-button type="primary" :icon="Plus" @click="formHook.dialog.open('add')">新增</el-button>
					<el-button type="danger" :loading="t.action.delete.loading" :icon="Delete" @click="t.action.delete.execute()">删除</el-button>
					<el-button type="success" :icon="DocumentAdd" @click="generatorBatchHandler()">生成</el-button>
				</el-space>
			</template>

			<!-- 查询字段：项目选择器 -->
			<template #query-projectId>
				<el-select v-model="tableHook.search.form.projectId" style="width: 180px" placeholder="请选择项目" clearable filterable>
					<el-option v-for="projectItem in projectList" :key="projectItem.id" :label="projectItem.projectName" :value="projectItem.id">
						<option-label :label="projectItem.projectName" :desc="projectItem.projectDesc"></option-label>
					</el-option>
				</el-select>
			</template>

			<!-- 表格列：枚举项数量 -->
			<template #table-enumItemCount="{ row }">
				<el-button type="primary" link @click="configEnumItemHandle(row)">{{ row.enumItemCount }}个</el-button>
			</template>

			<!-- 表单字段：项目选择器（编辑态禁用） -->
			<template #form-projectId="{ model, mode }">
				<el-select v-model="model.projectId" clearable filterable :disabled="mode === 'update'" placeholder="请选择项目">
					<el-option v-for="projectItem in projectList" :key="projectItem.id" :label="projectItem.projectName" :value="projectItem.id">
						<option-label :label="projectItem.projectName" :desc="projectItem.projectDesc"></option-label>
					</el-option>
				</el-select>
			</template>

			<!-- 表单虚拟字段（bare）：基础属性分段标题，仅当存在枚举模板组属性时显示 -->
			<template #form-divider-basic>
				<form-divider v-if="isNotEmpty(enumTemplateGroupPropertyList)" title="基础属性"></form-divider>
			</template>

			<!-- 表单虚拟字段（bare）：枚举模板组属性标题 + 动态子表单 -->
			<template #form-templateGroupProperty="{ model }">
				<template v-if="isNotEmpty(enumTemplateGroupPropertyList)">
					<form-divider title="枚举模板组属性"></form-divider>
					<template-group-property-form :key="model.projectId" v-model:form-data="model.templateGroupPropertyData" :property-list="enumTemplateGroupPropertyList">
					</template-group-property-form>
				</template>
			</template>

			<!-- 操作列：配置 + 修改 / 预览 + 更多（生成、删除） -->
			<template #table-action="{ row, tableHook: t, dialog }">
				<el-row :gutter="5">
					<el-col :span="12">
						<el-button type="primary" link :icon="Setting" @click="configEnumItemHandle(row)">配置</el-button>
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
									<el-dropdown-item :icon="DocumentAdd" @click="generatorHandler(row)">生成</el-dropdown-item>
									<el-dropdown-item :icon="Delete" @click="t.action.delete.execute(row)">删除</el-dropdown-item>
								</el-dropdown-menu>
							</template>
						</el-dropdown>
					</el-col>
				</el-row>
			</template>
		</crud-page>

		<!-- 枚举项配置弹窗 -->
		<enum-item-index ref="enumItemIndexRef" @refresh-data-list="tableHook.query.getDataList()"></enum-item-index>

		<preview ref="previewRef"></preview>

		<template-index ref="templateIndexRef" @clear-selection="clearSelectionHandler()"></template-index>
	</div>
</template>

<script setup lang="ts">
import EnumItemIndex from '@/views/gen/enum-item/index.vue'
import TemplateIndex from '@/views/gen/enum/template-index.vue'
import Preview from '@/business/preview/index.vue'
import FormDivider from '@/components/form/divider/index.vue'
import TemplateGroupPropertyForm from '@/views/gen/template-group-property/property-form.vue'
import OptionLabel from '@/components/option/label/index.vue'
import CrudPage from '@/components/crud/CrudPage.vue'
import { genEnumApi, genProjectApi, genTemplateGroupPropertyApi } from '@/api'
import type { CrudField, GenEnumEntity, GenEnumQuery, GenProjectEntity, GenTemplateGroupPropertyEntity, UseCrudFormOptions, UseCrudTableOptions } from '@/types'
import { useCrud } from '@/hooks/use-crud'
import { getDictTag } from '@/utils/enum'
import { GENERATOR_PRODUCT_TYPES, PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { isBlank, isNotEmpty } from '@/utils/tool'
import { Delete, DocumentAdd, Edit, More, Plus, Setting, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

defineOptions({
	name: 'GenEnum'
})

const enumItemIndexRef = ref()
const previewRef = ref()
const templateIndexRef = ref()
const projectList = ref([] as GenProjectEntity[])
const enumTemplateGroupPropertyList = ref([] as GenTemplateGroupPropertyEntity[])

const getProjectList = () => {
	genProjectApi.entityList().then(data => {
		projectList.value = data
	})
}

onMounted(() => {
	getProjectList()
})

// ====== 统一字段声明 ======
const fields: CrudField[] = [
	// 表单虚拟字段（bare）：基础属性分段标题
	{ key: 'divider-basic', label: '基础属性', inForm: true, form: { display: 'bare' } },
	{
		key: 'projectId',
		label: '项目',
		inForm: true,
		inSearch: true,
		form: { ruleList: [{ required: true, message: '项目不能为空', trigger: 'blur' }] }
	},
	// 项目名称：仅表格展示列（VO 关联字段，非表单/查询）
	{ key: 'projectName', label: '项目名称', inTable: true },
	{
		key: 'enumName',
		label: '枚举名称',
		tooltip: "使用英文小写字母，单词之间使用'-'拼接；在同一个项目下唯一",
		inForm: true,
		inSearch: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: '枚举名称不能为空', trigger: 'blur' }] }
	},
	{ key: 'generatorType', label: '生成类型', inTable: true, table: { formatter: getDictTag(PROJECT_GENERATE_TYPES) } },
	{
		key: 'enumDesc',
		label: '枚举描述',
		inForm: true,
		inTable: true,
		form: { ruleList: [{ required: true, message: '枚举描述不能为空', trigger: 'blur' }] }
	},
	{ key: 'enumItemCount', label: '枚举项数量', inTable: true },
	// 表单虚拟字段（bare）：枚举模板组属性标题 + 动态子表单容器
	{ key: 'templateGroupProperty', label: '枚举模板组属性', inForm: true, form: { display: 'bare' } }
]

// ====== 表单配置 ======
const formConfig = reactive({
	initFormData: (): GenEnumEntity => ({
		id: '',
		projectId: '',
		enumName: '',
		enumDesc: '',
		templateGroupPropertyData: {}
	}),
	submitApi: genEnumApi.submit,
	detailApi: genEnumApi.detail,
	labelWidth: '140px'
} as UseCrudFormOptions)

// ====== 表格配置 ======
const tableConfig = reactive({
	deleteNameKey: 'enumName',
	dataListApi: genEnumApi.voPage,
	deleteApi: genEnumApi.deleteList,
	initQueryForm: (): GenEnumQuery => ({
		enumName: '',
		projectId: ''
	})
} as UseCrudTableOptions)

// ====== 统一 hook ======
const { tableHook, formHook } = useCrud<GenEnumEntity, GenEnumQuery, GenEnumEntity>({
	subject: '枚举',
	fields,
	tableConfig,
	formConfig
})

// 选择项目后加载该项目枚举模板组的属性清单（驱动表单动态子表单显隐）
watch(
	() => formHook.form.data.projectId,
	projectId => {
		enumTemplateGroupPropertyList.value = []
		if (!projectId) {
			return
		}

		const project = projectList.value.find(item => item.id === projectId)
		if (!project || isBlank(project.enumTemplateGroupId)) {
			return
		}

		genTemplateGroupPropertyApi
			.entityList({
				templateGroupId: project.enumTemplateGroupId
			})
			.then(data => {
				enumTemplateGroupPropertyList.value = data
			})
	}
)

const configEnumItemHandle = ({ id, enumName }: { id: string; enumName: string }) => {
	enumItemIndexRef.value.init(id, enumName)
}

const previewHandle = (row: any) => {
	previewRef.value.init(row.id, row.enumName, row.projectId, row.generatorType, GENERATOR_PRODUCT_TYPES.Enum)
}

const generatorBatchHandler = () => {
	const data = tableHook.selection.rows
	if (data.length === 0) {
		ElMessage.warning('请选择要生成代码的枚举')
		return
	}
	genEnumApi.generateCheck(tableHook.selection.ids).then(resData => {
		const { checkResult, enumTemplateGroupId, generatorType } = resData
		if (!checkResult) {
			ElMessage.warning('当前选择的枚举不是同一个项目')
		} else {
			templateIndexRef.value.init(enumTemplateGroupId, generatorType, data)
		}
	})
}

const clearSelectionHandler = () => {
	tableHook.selection.clear()
}

const generatorHandler = (row: any) => {
	templateIndexRef.value.init(row.enumTemplateGroupId, row.generatorType, [row.id])
}
</script>

<style scoped>
:deep(.el-dropdown) {
	vertical-align: middle;
}
</style>
