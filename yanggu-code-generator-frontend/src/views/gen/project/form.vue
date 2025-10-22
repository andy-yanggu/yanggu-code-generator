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
			<el-divider content-position="left" class="form-section-title">基础信息</el-divider>
			<el-row>
				<el-col :span="12">
					<el-form-item prop="projectName">
						<template #label>
							<div style="display: flex; align-items: center">
								<span>项目名称</span>
								<el-tooltip content="使用英文小写字母，单词之间使用'-'拼接；该字段具有唯一性" effect="dark" placement="top">
									<el-icon style="margin-left: 5px; cursor: pointer"><InfoFilled></InfoFilled></el-icon>
								</el-tooltip>
							</div>
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
						<el-radio-group v-model="state.dataForm.generatorType">
							<el-radio v-for="item in PROJECT_GENERATE_TYPES" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
						</el-radio-group>
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
			<el-form-item label="项目包名" prop="projectPackage">
				<el-input v-model="state.dataForm.projectPackage" clearable placeholder="请输入项目包名"></el-input>
			</el-form-item>

			<el-divider content-position="left" style="margin: 30px 0">项目模板组配置</el-divider>
			<el-form-item label="项目模板组" prop="projectTemplateGroupId">
				<el-select v-model="state.dataForm.projectTemplateGroupId" placeholder="请选择项目模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in projectTemplateGroupList" :key="item.id" :label="item.groupName" :value="item.id">
						<span style="font-weight: bold">{{ item.groupName }}</span>
						<span v-if="item.groupDesc && item.groupDesc.trim()" style="color: #999; font-size: 12px">（{{ item.groupDesc }}）</span>
					</el-option>
				</el-select>
			</el-form-item>
			<template-group-property-form
				:model-value="state.dataForm.projectTemplateGroupPropValue"
				model-value-prop="projectTemplateGroupPropValue"
				:property-list="projectTemplateGroupPropertyList"
			></template-group-property-form>

			<el-divider content-position="left" style="margin: 30px 0">表模板组</el-divider>
			<el-form-item label="表模板组" prop="tableTemplateGroupId">
				<el-select v-model="state.dataForm.tableTemplateGroupId" placeholder="请选择表模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in tableTemplateGroupList" :key="item.id" :label="item.groupName" :value="item.id">
						<span style="font-weight: bold">{{ item.groupName }}</span>
						<span v-if="item.groupDesc && item.groupDesc.trim()" style="color: #999; font-size: 12px">（{{ item.groupDesc }}）</span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-divider content-position="left" style="margin: 30px 0">枚举模板组</el-divider>
			<el-form-item label="枚举模板组" prop="enumTemplateGroupId">
				<el-select v-model="state.dataForm.enumTemplateGroupId" placeholder="请选择枚举模板组" style="width: 100%" clearable filterable>
					<el-option v-for="item in enumTemplateGroupList" :key="item.id" :label="item.groupName" :value="item.id">
						<span style="font-weight: bold">{{ item.groupName }}</span>
						<span v-if="item.groupDesc && item.groupDesc.trim()" style="color: #999; font-size: 12px">（{{ item.groupDesc }}）</span>
					</el-option>
				</el-select>
			</el-form-item>
			<el-divider content-position="left" style="margin: 30px 0">基类配置</el-divider>
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
			<el-divider content-position="left" style="margin: 30px 0">其他配置</el-divider>
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
import { genProjectApi } from '@/api/gen/project'
import { genDataSourceApi } from '@/api/gen/datasource'
import { genTemplateGroupApi } from '@/api/gen/template-group'
import { genBaseClassApi } from '@/api/gen/base-class'
import { PROJECT_GENERATE_TYPES } from '@/constant/enum'
import { FormOptions, useSubmitForm } from '@/hooks/use-submit-form'
import { Check, Close, InfoFilled } from '@element-plus/icons-vue'
import TemplateGroupPropertyForm, { PropertyItem } from '@/views/gen/template-group-property/property-form.vue'

interface Datasource {
	id: number
	connName: string
	datasourceDesc: string
}

interface BaseClass {
	id: number
	packageName: string
	className: string
	remark: string
}

interface TemplateGroup {
	id: number
	type: number
	groupName: string
	groupDesc: string
	propertyList: PropertyItem[]
}

const emit = defineEmits(['refreshDataList'])

const getList = () => {
	// 数据源下拉
	genDataSourceApi.entityList().then((data: Datasource[]) => {
		datasourceList.value = data
	})

	// 模板组下拉
	genTemplateGroupApi.voList().then((data: TemplateGroup[]) => {
		projectTemplateGroupList.value = data.filter((item: TemplateGroup) => item.type === 0)
		nextTick(() => {
			addProjectRule()
		})
		tableTemplateGroupList.value = data.filter((item: TemplateGroup) => item.type === 1)
		nextTick(() => {
			addTableRule()
		})
		enumTemplateGroupList.value = data.filter((item: TemplateGroup) => item.type === 2)
		nextTick(() => {
			addEnumRule()
		})
	})

	// 基类下拉
	genBaseClassApi.entityList().then((data: BaseClass[]) => {
		baseClassList.value = data
	})
}

const state: FormOptions = reactive({
	submitApi: genProjectApi.submit,
	detailApi: genProjectApi.detail,
	initBefore: () => {
		getList()
		state.dataForm.projectTemplateGroupPropValue = {}
		state.dataForm.tableTemplateGroupPropValue = {}
		state.dataForm.enumTemplateGroupPropValue = {}
	},
	dataForm: {
		id: null,
		projectName: '',
		projectPackage: '',
		projectVersion: '',
		datasourceId: '',
		projectTemplateGroupId: '',
		projectTemplateGroupPropValue: {},
		tableTemplateGroupId: '',
		tableTemplateGroupPropValue: {},
		enumTemplateGroupId: '',
		enumTemplateGroupPropValue: {},
		backendPath: '',
		frontendPath: '',
		projectDesc: '',
		author: '',
		entityBaseClassId: '',
		voBaseClassId: '',
		generatorType: null
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
})

const projectTemplateGroupPropertyList = computed(() => {
	const find = projectTemplateGroupList.value.find(item => item.id === state.dataForm.projectTemplateGroupId)
	if (find) {
		return find.propertyList
	} else {
		return []
	}
})

const tableTemplateGroupPropertyList = computed(() => {
	return tableTemplateGroupList.value.find(item => item.id === state.dataForm.tableTemplateGroupId)?.propertyList
})

const enumTemplateGroupPropertyList = computed(() => {
	return enumTemplateGroupList.value.find(item => item.id === state.dataForm.enumTemplateGroupId)?.propertyList
})

watch(
	() => state.dataForm.projectTemplateGroupId,
	() => {
		addProjectRule()
		if (!state.dataForm.id) {
			state.dataForm.projectTemplateGroupPropValue = {}
		}
	}
)

watch(
	() => state.dataForm.tableTemplateGroupId,
	() => {
		addTableRule()
		if (!state.dataForm.id) {
			state.dataForm.tableTemplateGroupPropValue = {}
		}
	}
)

watch(
	() => state.dataForm.enumTemplateGroupId,
	() => {
		addEnumRule()
		if (!state.dataForm.id) {
			state.dataForm.enumTemplateGroupPropValue = {}
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

const addTableRule = () => {
	// 删除之前的规则
	// 字段是tableTemplateGroupPropValue.xxx的形式
	for (const key in dataRules) {
		if (key.startsWith('tableTemplateGroupPropValue.')) {
			delete dataRules[key]
		}
	}

	// 添加后面的规则
	tableTemplateGroupPropertyList.value?.forEach(item => {
		if (item.required === 1) {
			dataRules[`tableTemplateGroupPropValue.${item.propKey}`] = [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
		}
	})
}

const addEnumRule = () => {
	// 删除之前的规则
	// 字段是enumTemplateGroupPropValue.xxx的形式
	for (const key in dataRules) {
		if (key.startsWith('enumTemplateGroupPropValue.')) {
			delete dataRules[key]
		}
	}

	// 添加后面的规则
	enumTemplateGroupPropertyList.value?.forEach(item => {
		if (item.required === 1) {
			dataRules[`enumTemplateGroupPropValue.${item.propKey}`] = [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
		}
	})
}

const dataRules = reactive({
	projectName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	projectVersion: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	projectTemplateGroupId: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	tableTemplateGroupId: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	generatorType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const datasourceList = ref([] as Datasource[])
const projectTemplateGroupList = ref([] as TemplateGroup[])
const tableTemplateGroupList = ref([] as TemplateGroup[])
const enumTemplateGroupList = ref([] as TemplateGroup[])
const baseClassList = ref([] as BaseClass[])

const { visible, dataFormRef, init, submitHandle, submitLoading } = useSubmitForm(state)
defineExpose({
	init
})
</script>
<style lang="scss" scoped>
.form-section-title {
	margin: 36px 0 26px;
}
::v-deep(.el-divider__text) {
	font-size: 15px;
	font-weight: 600; /* 加粗 */
}
</style>
