<template>
	<div>
		<crud-page :table-hook="tableHook" :form-hook="formHook">
			<!-- 工具栏左侧：新增 + 删除 + 导入 + 导出（覆盖默认按钮） -->
			<template #table-toolbar-left="{ tableHook: t }">
				<el-space size="default">
					<el-button type="primary" :icon="Plus" @click="formHook.dialog.open('add')">新增</el-button>
					<el-button type="danger" :loading="t.action.delete.loading" :icon="Delete" @click="t.action.delete.execute()">删除</el-button>
					<el-upload :limit="1" :show-file-list="false" :http-request="({ file }) => t.action.import.execute(file)">
						<el-button type="success" :icon="Upload">导入</el-button>
					</el-upload>
					<el-button type="info" :icon="Download" @click="t.action.export.execute()">导出</el-button>
				</el-space>
			</template>

			<!-- 属性数量列：可点击链接 -->
			<template #table-groupPropertyCount="{ row }">
				<el-button type="primary" link @click="propertyHandler(row)">{{ row.groupPropertyCount }}个</el-button>
			</template>

			<!-- 操作列：模板 + 模板v2 + 属性 + 修改 + 更多（2×2 网格 + 对照切换） -->
			<template #table-action="{ row, tableHook: t, dialog }">
				<el-row>
					<el-col :span="12">
						<el-button type="primary" link :icon="Files" @click="treeData(row)">模板</el-button>
					</el-col>
					<el-col :span="12">
						<!-- v2 并行对照入口：走拆分后的 tree-v2 组件集，v1 冻结不动 -->
						<el-button type="primary" link :icon="Files" @click="treeDataV2(row)">模板v2</el-button>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="12">
						<el-button type="primary" link :icon="List" @click="propertyHandler(row)">属性</el-button>
					</el-col>
					<el-col :span="12">
						<el-button type="primary" link :icon="Edit" @click="dialog?.open('update', row[t.refs.primaryKey])">修改</el-button>
					</el-col>
				</el-row>
				<el-row>
					<el-col :span="12">
						<el-dropdown>
							<el-button type="primary" link :icon="More">更多</el-button>
							<template #dropdown>
								<el-dropdown-menu>
									<el-dropdown-item :icon="CopyDocument" @click="dialog?.open('copy', row[t.refs.primaryKey])">复制</el-dropdown-item>
									<el-dropdown-item :icon="Download" @click="t.action.export.execute(row[t.refs.primaryKey])">导出</el-dropdown-item>
									<el-dropdown-item :icon="Delete" @click="t.action.delete.execute(row)">删除</el-dropdown-item>
								</el-dropdown-menu>
							</template>
						</el-dropdown>
					</el-col>
				</el-row>
			</template>

			<!-- 表单字段 type：编辑模式下禁用 -->
			<template #form-type="{ model, field }">
				<el-select
					v-model="model[field.prop]"
					:options="field.options"
					:disabled="formHook.dialog.mode !== 'add'"
					clearable
					filterable
					placeholder="请选择模板组类型"
				></el-select>
			</template>
		</crud-page>

		<!-- 模板树弹窗（v1，冻结） -->
		<template-tree
			ref="treeUpdateRef"
			:template-group-id="currentTemplateGroup.id"
			:template-group-name="currentTemplateGroup.groupName"
			:template-group-type="currentTemplateGroup.type"
		></template-tree>

		<!-- 模板树弹窗（v2，拆分对照） -->
		<template-tree-v2
			ref="treeV2UpdateRef"
			:template-group-id="currentTemplateGroup.id"
			:template-group-name="currentTemplateGroup.groupName"
			:template-group-type="currentTemplateGroup.type"
		></template-tree-v2>

		<!-- 属性管理弹窗 -->
		<template-group-property
			ref="propertyRef"
			:template-group-id="currentTemplateGroup.id"
			:template-group-name="currentTemplateGroup.groupName"
			@refresh-data-list="tableHook.query.getDataList"
		></template-group-property>
	</div>
</template>

<script setup lang="ts">
import { genTemplateGroupApi } from '@/api'
import { TEMPLATE_GROUP_TYPES } from '@/constant/enum'
import { getDictTag } from '@/utils/enum'
import type { CrudField, GenTemplateGroupCopyDTO, GenTemplateGroupEntity, GenTemplateGroupQuery, UseCrudFormOptions, UseCrudTableOptions } from '@/types'
import { useCrud } from '@/hooks/use-crud'
import CrudPage from '@/components/crud/CrudPage.vue'
import TemplateTree from '@/views/gen/template/tree.vue'
import TemplateTreeV2 from '@/views/gen/template/tree-v2.vue'
import TemplateGroupProperty from '@/views/gen/template-group-property/index.vue'
import { CopyDocument, Delete, Download, Edit, Files, List, More, Plus, Upload } from '@element-plus/icons-vue'

defineOptions({
	name: 'GenTemplateGroup'
})

// 统一字段声明
const fields: CrudField[] = [
	{
		key: 'groupName',
		label: '模板组名称',
		tooltip: '模板组名称具有唯一性，不能重复',
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			ruleList: [{ required: true, message: '模板组名称不能为空', trigger: 'blur' }]
		}
	},
	{
		key: 'type',
		label: '模板组类型',
		component: 'select',
		options: TEMPLATE_GROUP_TYPES.items,
		inForm: true,
		inSearch: true,
		inTable: true,
		form: {
			ruleList: [{ required: true, message: '模板组类型不能为空', trigger: 'blur' }]
		},
		table: { formatter: getDictTag(TEMPLATE_GROUP_TYPES) }
	},
	{ key: 'groupDesc', label: '模板组描述', inForm: true, inTable: true },
	{ key: 'groupPropertyCount', label: '属性数量', inTable: true }
]

// 表单配置
const formConfig = reactive({
	initFormData: (): GenTemplateGroupEntity => ({
		id: null,
		groupName: '',
		type: '',
		groupDesc: ''
	}),
	submitApi: genTemplateGroupApi.submit,
	detailApi: genTemplateGroupApi.detail,
	beforeOpen: () => {
		if (formHook.dialog.mode === 'copy') {
			formConfig.submitApi = genTemplateGroupApi.copy
			formConfig.successMessage = '模板组和下的所有模板、属性已复制'
		} else {
			formConfig.submitApi = genTemplateGroupApi.submit
			delete formConfig.successMessage
		}
	},
	afterDataAssign: data => {
		if (formHook.dialog.mode === 'copy') {
			formHook.form.data.groupName += '_复制'
			// hook 在 copy 模式下会把主键置空（通用复制语义），后端复制接口需要源模板组ID，故另用 oldGroupId 携带
			;(formHook.form.data as GenTemplateGroupCopyDTO).oldGroupId = data.id
		}
	},
	labelWidth: '120px'
} as UseCrudFormOptions)

// 表格配置
const tableConfig = reactive({
	dataListApi: genTemplateGroupApi.page,
	deleteApi: genTemplateGroupApi.deleteList,
	deleteNameKey: 'groupName',
	deleteConfirmMessage: '删除模板组，模板组下面的所有模板、属性都会删除',
	exportApi: genTemplateGroupApi.export,
	importApi: genTemplateGroupApi.import,
	importSuccessMessage: '模板组导入成功',
	initQueryData: (): GenTemplateGroupQuery => ({
		groupName: '',
		type: ''
	})
} as UseCrudTableOptions)

// 统一 hook
const { tableHook, formHook } = useCrud<GenTemplateGroupEntity, GenTemplateGroupQuery, GenTemplateGroupEntity>({
	subject: '模板组',
	fields,
	formConfig,
	tableConfig
})

// 子弹窗状态
const currentTemplateGroup = reactive({ id: -1, groupName: '', type: -1 })
const treeUpdateRef = ref()
const treeV2UpdateRef = ref()
const propertyRef = ref()

const treeData = (row: any) => {
	Object.assign(currentTemplateGroup, row)
	nextTick(() => {
		treeUpdateRef.value.init(row.id)
	})
}

// v2 入口：共用 currentTemplateGroup 绑定，区别只在挂载的组件集
const treeDataV2 = (row: any) => {
	Object.assign(currentTemplateGroup, row)
	nextTick(() => {
		treeV2UpdateRef.value.init(row.id)
	})
}

const propertyHandler = (row: any) => {
	Object.assign(currentTemplateGroup, row)
	nextTick(() => {
		propertyRef.value.init(row.id)
	})
}
</script>

<style scoped>
:deep(.el-dropdown) {
	vertical-align: middle;
}
</style>
