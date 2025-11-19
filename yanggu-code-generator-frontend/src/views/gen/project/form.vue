<template>
	<el-dialog v-model="visible" :title="!state.dataForm.id ? '新增' : '修改'" :close-on-click-modal="false" width="60%">
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
						<el-input v-model.number="state.dataForm.projectPort" placeholder="请输入项目端口" clearable @input="projectPortInputHandler"></el-input>
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
							<el-option v-for="item in datasourceList" :key="item.id" :label="item.connName" :value="item.id">
								<span style="font-weight: bold">{{ item.connName }}</span>
								<span v-if="item.datasourceDesc && item.datasourceDesc.trim()" style="color: #999; font-size: 12px">
									（{{ item.datasourceDesc }}）
								</span>
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

			<form-divider title="模板组配置"></form-divider>
			<el-form-item label="表模板组" prop="tableTemplateGroupId">
				<el-select v-model="state.dataForm.tableTemplateGroupId" placeholder="请选择表模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in tableTemplateGroupList" :key="item.id" :label="item.groupName" :value="item.id">
						<span style="font-weight: bold">{{ item.groupName }}</span>
						<span v-if="item.groupDesc && item.groupDesc.trim()" style="color: #999; font-size: 12px">（{{ item.groupDesc }}）</span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="枚举模板组" prop="enumTemplateGroupId">
				<el-select v-model="state.dataForm.enumTemplateGroupId" placeholder="请选择枚举模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in enumTemplateGroupList" :key="item.id" :label="item.groupName" :value="item.id">
						<span style="font-weight: bold">{{ item.groupName }}</span>
						<span v-if="item.groupDesc && item.groupDesc.trim()" style="color: #999; font-size: 12px">（{{ item.groupDesc }}）</span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="项目模板组" prop="projectTemplateGroupId">
				<el-select v-model="state.dataForm.projectTemplateGroupId" placeholder="请选择项目模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in projectTemplateGroupList" :key="item.id" :label="item.groupName" :value="item.id">
						<span style="font-weight: bold">{{ item.groupName }}</span>
						<span v-if="item.groupDesc && item.groupDesc.trim()" style="color: #999; font-size: 12px">（{{ item.groupDesc }}）</span>
					</el-option>
				</el-select>
			</el-form-item>
			<!-- 项目模板组属性表单 -->
			<template v-if="state.dataForm.projectTemplateGroupId && projectTemplateGroupPropertyList!.length > 0">
				<form-divider title="项目模板组属性"></form-divider>
				<template-group-property-form
					:key="state.dataForm.projectTemplateGroupId"
					v-model:form-data="state.dataForm.projectTemplateGroupPropValue"
					model-value-prop="projectTemplateGroupPropValue"
					:property-list="projectTemplateGroupPropertyList"
				></template-group-property-form>
			</template>

			<form-divider title="基类配置"></form-divider>
			<el-form-item prop="entityBaseClassId" label="Entity基类">
				<el-select v-model="state.dataForm.entityBaseClassId" placeholder="请选择Entity基类" style="width: 100%" clearable filterable>
					<el-option v-for="item in baseClassList" :key="item.id" :label="`${item.packageName}.${item.className}`" :value="item.id">
						<span style="font-weight: bold">{{ item.packageName }}.{{ item.className }}</span>
						<span v-if="item.remark && item.remark.trim()" style="color: #999; font-size: 12px">（{{ item.remark }}）</span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-form-item prop="voBaseClassId" label="VO基类">
				<el-select v-model="state.dataForm.voBaseClassId" placeholder="请选择VO基类" style="width: 100%" clearable filterable>
					<el-option v-for="item in baseClassList" :key="item.id" :label="`${item.packageName}.${item.className}`" :value="item.id">
						<span style="font-weight: bold">{{ item.packageName }}.{{ item.className }}</span>
						<span v-if="item.remark && item.remark.trim()" style="color: #999; font-size: 12px">（{{ item.remark }}）</span>
					</el-option>
				</el-select>
			</el-form-item>

			<form-divider title="其他配置"></form-divider>
			<el-form-item label="后端路径" prop="backendPath">
				<el-input v-model="state.dataForm.backendPath" clearable placeholder="请输入后端路径"></el-input>
			</el-form-item>
			<el-form-item label="前端路径" prop="frontendPath">
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
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { genProjectApi, GenProjectEntity } from '@/api/gen/project'
import { genDataSourceApi, GenDatasourceEntity } from '@/api/gen/datasource'
import { genTemplateGroupApi, GenTemplateGroupEntity } from '@/api/gen/template-group'
import { genBaseClassApi, GenBaseClassEntity } from '@/api/gen/base-class'
import { PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close } from '@element-plus/icons-vue'
import TemplateGroupPropertyForm from '@/views/gen/template-group-property/property-form.vue'
import FormDivider from '@/components/form/divider/index.vue'
import FormLabelTooltip from '@/components/form/label-tooltip/index.vue'
import { GenTemplateGroupPropertyEntity } from '@/api/gen/template-group-property'

defineOptions({
	name: 'GenProjectForm'
})

const emit = defineEmits(['refreshDataList'])

const getList = () => {
	// 数据源下拉
	genDataSourceApi.entityList().then(data => {
		datasourceList.value = data
	})

	// 模板组下拉
	genTemplateGroupApi.voList().then(data => {
		projectTemplateGroupList.value = data.filter(item => item.type === 0)
		nextTick(() => {
			addProjectRule()
		})
		tableTemplateGroupList.value = data.filter(item => item.type === 1)
		enumTemplateGroupList.value = data.filter(item => item.type === 2)
	})

	// 基类下拉
	genBaseClassApi.entityList().then(data => {
		baseClassList.value = data
	})
}

const state = reactive({
	submitApi: genProjectApi.submit,
	detailApi: genProjectApi.detail,
	initBefore: () => {
		getList()
		state.dataForm.projectTemplateGroupPropValue = {}
	},
	dataForm: {
		id: '',
		projectName: '',
		projectPackage: '',
		projectPort: '',
		projectVersion: '',
		datasourceId: '',
		projectTemplateGroupId: '',
		projectTemplateGroupPropValue: {},
		tableTemplateGroupId: '',
		enumTemplateGroupId: '',
		backendPath: '',
		frontendPath: '',
		projectDesc: '',
		author: '',
		entityBaseClassId: '',
		voBaseClassId: '',
		generatorType: ''
	},
	submitBefore: () => {
		if (!state.dataForm.id) {
			if (state.dataForm.datasourceId) {
				state.message = '新增成功，已经导入该项目引用数据源下的所有表，请到表管理中进行查看'
				state.duration = 2000
			} else {
				state.message = '新增成功'
				state.duration = 1000
			}
		} else {
			state.message = '修改成功'
			state.duration = 500
		}

		if (!projectTemplateGroupPropertyList.value || projectTemplateGroupPropertyList.value.length === 0) {
			state.dataForm.projectTemplateGroupPropValue = {}
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

watch(
	() => state.dataForm.projectTemplateGroupId,
	(newVal, oldVal) => {
		// 初始化阶段（oldVal 为 undefined 或空），不需要清空
		if (!oldVal) {
			addProjectRule()
			return
		}

		// 只有真正发生变更时才清空
		if (newVal !== oldVal) {
			addProjectRule()

			// 清空而不替换引用
			const obj = state.dataForm.projectTemplateGroupPropValue
			Object.keys(obj).forEach(k => delete obj[k])
		}
	}
)

const addProjectRule = () => {
	// 删除之前的规则
	// 字段是projectTemplateGroupPropValue.xxx的形式
	for (const key in dataRules) {
		if (key.startsWith('projectTemplateGroupPropValue.')) {
			delete dataRules[key]
		}
	}

	// 添加后面的规则
	projectTemplateGroupPropertyList.value?.forEach(item => {
		if (item.required === 1) {
			dataRules[`projectTemplateGroupPropValue.${item.propKey}`] = [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
		}
	})
}

const dataRules = reactive({
	projectName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	projectVersion: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	projectPort: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	projectTemplateGroupId: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	tableTemplateGroupId: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	generatorType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const projectPortInputHandler = (value: number | string) => {
	if (value === '') {
		state.dataForm.projectPort = null
	} else {
		const n = Number(value)
		state.dataForm.projectPort = Number.isNaN(n) ? null : n
	}
}

const datasourceList = ref([] as GenDatasourceEntity[])
const projectTemplateGroupList = ref([] as GenTemplateGroupEntity[])
const tableTemplateGroupList = ref([] as GenTemplateGroupEntity[])
const enumTemplateGroupList = ref([] as GenTemplateGroupEntity[])
const baseClassList = ref([] as GenBaseClassEntity[])

const { visible, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)
defineExpose({
	init
})
</script>
<style lang="scss" scoped>
::v-deep(.el-divider__text) {
	font-size: 15px;
	font-weight: 600; /* 加粗 */
}
</style>
