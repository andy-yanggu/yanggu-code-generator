<template>
	<el-dialog v-model="visible" :title="dialogTitle()" :close-on-click-modal="false" width="65%">
		<el-form
			ref="dataFormRef"
			:model="state.dataForm"
			:rules="dataRules"
			label-width="120px"
			:validate-on-rule-change="false"
			@keyup.enter="submitHandle()"
		>
			<form-divider title="基础信息"></form-divider>
			<el-row>
				<el-col :span="12">
					<el-form-item prop="projectName">
						<template #label>
							<form-label-tooltip label="项目名称" tooltip="使用英文小写字母，单词之间使用'-'拼接；该字段具有唯一性"></form-label-tooltip>
						</template>
						<el-input v-model="state.dataForm.projectName" clearable placeholder="请输入项目名称"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="项目版本" prop="projectVersion">
						<el-input v-model="state.dataForm.projectVersion" clearable placeholder="请输入项目版本"></el-input>
					</el-form-item>
				</el-col>
			</el-row>
			<el-row>
				<el-col :span="12">
					<el-form-item label="生成方式" prop="generatorType">
						<el-radio-group v-model="state.dataForm.generatorType" :options="PROJECT_GENERATE_TYPES"></el-radio-group>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="项目端口" prop="projectPort">
						<el-input v-model.number="state.dataForm.projectPort" placeholder="请输入项目端口" clearable></el-input>
					</el-form-item>
				</el-col>
			</el-row>
			<el-row>
				<el-col :span="12">
					<el-form-item label="项目包名" prop="projectPackage">
						<el-input v-model="state.dataForm.projectPackage" clearable placeholder="请输入项目包名"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="数据源" prop="datasourceId">
						<el-select v-model="state.dataForm.datasourceId" clearable filterable placeholder="请选择数据源" style="width: 100%">
							<el-option v-for="item in datasourceList" :key="item.id as number" :label="item.connName" :value="item.id as number">
								<option-label :label="item.connName" :desc="item.datasourceDesc"></option-label>
							</el-option>
						</el-select>
					</el-form-item>
				</el-col>
			</el-row>
			<el-row>
				<el-col :span="12">
					<el-form-item label="作者" prop="author">
						<el-input v-model="state.dataForm.author" clearable placeholder="请输入作者"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="项目描述" prop="projectDesc">
						<el-input v-model="state.dataForm.projectDesc" clearable placeholder="请输入项目描述"></el-input>
					</el-form-item>
				</el-col>
			</el-row>
			<el-form-item label="模块列表" prop="moduleList">
				<el-row
					v-for="(item, index) in state.dataForm.moduleList"
					:key="index"
					:gutter="10"
					:style="{ marginBottom: index < state.dataForm.moduleList.length - 1 ? '18px' : '0' }"
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
							<el-button circle :icon="Plus" type="primary" @click="() => state.dataForm.moduleList.splice(index + 1, 0, emptyModule())"></el-button>
							<el-button
								v-show="state.dataForm.moduleList.length > 1"
								type="danger"
								circle
								:icon="Delete"
								@click="() => state.dataForm.moduleList.splice(index, 1)"
							></el-button>
						</el-space>
					</div>
				</el-row>
			</el-form-item>

			<form-divider title="模板组配置"></form-divider>
			<el-form-item label="表模板组" prop="tableTemplateGroupId">
				<el-select v-model="state.dataForm.tableTemplateGroupId" placeholder="请选择表模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in tableTemplateGroupList" :key="item.id as number" :label="item.groupName" :value="item.id as number">
						<option-label :label="item.groupName" :desc="item.groupDesc"></option-label>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="枚举模板组" prop="enumTemplateGroupId">
				<el-select v-model="state.dataForm.enumTemplateGroupId" placeholder="请选择枚举模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in enumTemplateGroupList" :key="item.id as number" :label="item.groupName" :value="item.id as number">
						<option-label :label="item.groupName" :desc="item.groupDesc"></option-label>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="项目模板组" prop="projectTemplateGroupId">
				<el-select v-model="state.dataForm.projectTemplateGroupId" placeholder="请选择项目模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in projectTemplateGroupList" :key="item.id as number" :label="item.groupName" :value="item.id as number">
						<option-label :label="item.groupName" :desc="item.groupDesc"></option-label>
					</el-option>
				</el-select>
			</el-form-item>

			<!-- 项目模板组属性表单 -->
			<template v-if="state.dataForm.projectTemplateGroupId && projectTemplateGroupPropertyList!.length > 0">
				<form-divider title="项目模板组属性"></form-divider>
				<template-group-property-form
					:key="state.dataForm.projectTemplateGroupId"
					v-model:form-data="state.dataForm.templateGroupPropertyData"
					model-value-prop="templateGroupPropertyData"
					:property-list="projectTemplateGroupPropertyList!"
				></template-group-property-form>
			</template>

			<form-divider title="其他配置"></form-divider>
			<el-form-item prop="backendPath">
				<template #label>
					<form-label-tooltip label="后端路径" tooltip="以 '/' 分隔"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.backendPath" clearable placeholder="请输入后端路径"></el-input>
			</el-form-item>
			<el-form-item prop="frontendPath">
				<template #label>
					<form-label-tooltip label="前端路径" tooltip="以 '/' 分隔"></form-label-tooltip>
				</template>
				<el-input v-model="state.dataForm.frontendPath" clearable placeholder="请输入前端路径"></el-input>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
			<el-button :icon="Close" @click="visible = false">取消</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { useSubmitForm } from '@/hooks'
import { Check, Close, Delete, Plus } from '@element-plus/icons-vue'
import TemplateGroupPropertyForm from '@/views/gen/template-group-property/property-form.vue'
import FormDivider from '@/components/form/divider/index.vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'
import OptionLabel from '@/components/option/label/index.vue'
import { genDatasourceApi, genProjectApi, genTemplateGroupApi } from '@/api'
import { FormOptions, GenDatasourceEntity, GenModuleEntity, GenProjectEntity, GenTemplateGroupEntity, GenTemplateGroupPropertyEntity } from '@/types'
import { FormItemRule } from 'element-plus'

defineOptions({
	name: 'GenProjectForm'
})

const emit = defineEmits(['refreshDataList'])

const getList = () => {
	// 数据源下拉
	genDatasourceApi.entityList().then(data => {
		datasourceList.value = data
	})

	// 模板组下拉
	genTemplateGroupApi.voList().then(data => {
		projectTemplateGroupList.value = data.filter(item => item.type === 0)
		tableTemplateGroupList.value = data.filter(item => item.type === 1)
		enumTemplateGroupList.value = data.filter(item => item.type === 2)
	})
}

const emptyModule = (): GenModuleEntity => ({
	moduleName: '',
	modulePath: '',
	moduleDesc: ''
})

// 初始化表单数据
const initFormData = (): GenProjectEntity => ({
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
})

const state = reactive({
	// 标题主体
	titleSubject: '项目',
	submitApi: genProjectApi.submit,
	detailApi: genProjectApi.detail,
	initBefore: getList,
	initFormData,
	dataForm: initFormData(),
	submitBefore: () => {
		if (formType.value === 'add' && state.dataForm.datasourceId) {
			state.message = '新增成功，已经导入该项目引用数据源下的所有表，请到表管理中进行查看'
			state.duration = 2000
		} else {
			state.message = ''
			state.duration = 500
		}
	},
	emit
} as FormOptions<GenProjectEntity>)

const projectTemplateGroupPropertyList = computed(() => {
	const find = projectTemplateGroupList.value.find(item => item.id === state.dataForm.projectTemplateGroupId)
	if (find) {
		return find.propertyList
	} else {
		return [] as GenTemplateGroupPropertyEntity[]
	}
})

const dataRules = computed(() => {
	const rules: Record<keyof GenProjectEntity | string, FormItemRule[]> = {
		projectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
		projectVersion: [{ required: true, message: '项目版本不能为空', trigger: 'blur' }],
		projectPort: [
			{ required: true, message: '项目端口不能为空', trigger: 'blur' },
			{ type: 'number', message: '项目端口必须为数字', trigger: 'change' }
		],
		projectTemplateGroupId: [{ required: true, message: '必填项不能为空', trigger: 'change' }],
		generatorType: [{ required: true, message: '请选择生成类型', trigger: 'blur' }]
		// moduleList 是否必填由外层 form-item 控制，若 required: true 则会出现在 rules['moduleList']
	}

	// 模板组属性校验
	projectTemplateGroupPropertyList.value?.forEach(item => {
		if (item.required === 1) {
			rules[`templateGroupPropertyData.${item.propKey}`] = [{ required: true, message: `${item.propTitle}不能为空`, trigger: 'blur' }]
		}
	})

	// === 判定 moduleList 是否必填 ===
	const moduleListRequired = rules['moduleList']?.some((r: any) => r.required)

	const list = state.dataForm.moduleList || []

	// === 工具方法：判断是否只有一个空模块 ===
	const isSingleEmptyModule = () => {
		if (list.length !== 1) {
			return false
		}
		const item = list[0]
		return !item.moduleName && !item.modulePath && !item.moduleDesc
	}

	// === 动态字段校验生成器 ===
	const makeValidator = (index: number) => {
		return (_: string) => ({
			validator(_: any, value: any, callback: any) {
				const item = list[index]
				const name = (item.moduleName || '').trim()
				const path = (item.modulePath || '').trim()
				const desc = (item.moduleDesc || '').trim()

				const allEmpty = !name && !path && !desc

				// ================================
				//  moduleList 非必填的处理逻辑
				// ================================
				if (!moduleListRequired) {
					// 整个 list 只有 1 个且为空 → 完全跳过校验
					if (isSingleEmptyModule()) {
						callback()
						return
					}

					// 当前行非空 → 所有字段必须填写
					if (!value || !value.trim()) {
						callback(new Error('必填项不能为空'))
						return
					}

					callback()
					return
				}

				// ================================
				//  moduleList 为必填（严格模式）
				// ================================
				// 当前行全空 → 报错
				if (allEmpty) {
					callback(new Error('模块内容不能为空'))
					return
				}

				// 当前字段为空 → 报错
				if (!value || !value.trim()) {
					callback(new Error('必填项不能为空'))
					return
				}

				callback()
			},
			trigger: 'blur'
		})
	}

	// === 生成动态规则 ===
	list.forEach((_, index) => {
		const validator = makeValidator(index)

		rules[`moduleList[${index}].moduleName`] = [validator('moduleName')]
		rules[`moduleList[${index}].modulePath`] = [validator('modulePath')]
		rules[`moduleList[${index}].moduleDesc`] = [validator('moduleDesc')]
	})

	return rules
})

const datasourceList = ref([] as GenDatasourceEntity[])
const projectTemplateGroupList = ref([] as GenTemplateGroupEntity[])
const tableTemplateGroupList = ref([] as GenTemplateGroupEntity[])
const enumTemplateGroupList = ref([] as GenTemplateGroupEntity[])

const { visible, dataFormRef, init, submitHandle, submitLoading, formType, dialogTitle } = useSubmitForm(state)
defineExpose({
	init
})
</script>
<style lang="scss" scoped>
.module-actions {
	width: 60px; /* ⭐ 固定宽度，解决大屏抖动问题 */
	display: flex;
	align-items: center;
	padding-left: 10px;
}
</style>
