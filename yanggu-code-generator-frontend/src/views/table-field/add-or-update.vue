<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
			<el-form-item label="id" prop="id">
				<el-input v-model="dataForm.id" placeholder="id"></el-input>
			</el-form-item>
			<el-form-item label="表ID" prop="tableId">
				<el-input v-model="dataForm.tableId" placeholder="表ID"></el-input>
			</el-form-item>
			<el-form-item label="字段名称" prop="fieldName">
				<el-input v-model="dataForm.fieldName" placeholder="字段名称"></el-input>
			</el-form-item>
			<el-form-item label="字段类型" prop="fieldType">
				<el-input v-model="dataForm.fieldType" placeholder="字段类型"></el-input>
			</el-form-item>
			<el-form-item label="字段说明" prop="fieldComment">
				<el-input v-model="dataForm.fieldComment" placeholder="字段说明"></el-input>
			</el-form-item>
			<el-form-item label="属性名" prop="attrName">
				<el-input v-model="dataForm.attrName" placeholder="属性名"></el-input>
			</el-form-item>
			<el-form-item label="属性类型" prop="attrType">
				<el-input v-model="dataForm.attrType" placeholder="属性类型"></el-input>
			</el-form-item>
			<el-form-item label="属性包名" prop="packageName">
				<el-input v-model="dataForm.packageName" placeholder="属性包名"></el-input>
			</el-form-item>
			<el-form-item label="排序" prop="sort">
				<el-input v-model="dataForm.sort" placeholder="排序"></el-input>
			</el-form-item>
			<el-form-item label="自动填充  DEFAULT、INSERT、UPDATE、INSERT_UPDATE" prop="autoFill">
				<el-input v-model="dataForm.autoFill" placeholder="自动填充  DEFAULT、INSERT、UPDATE、INSERT_UPDATE"></el-input>
			</el-form-item>
			<el-form-item label="主键 0：否  1：是" prop="primaryPk">
				<el-input v-model="dataForm.primaryPk" placeholder="主键 0：否  1：是"></el-input>
			</el-form-item>
			<el-form-item label="逻辑删除 0：否  1：是" prop="logicDelete">
				<el-input v-model="dataForm.logicDelete" placeholder="逻辑删除 0：否  1：是"></el-input>
			</el-form-item>
			<el-form-item label="逻辑删除值" prop="logicDeleteValue">
				<el-input v-model="dataForm.logicDeleteValue" placeholder="逻辑删除值"></el-input>
			</el-form-item>
			<el-form-item label="逻辑未删除值" prop="logicNotDeleteValue">
				<el-input v-model="dataForm.logicNotDeleteValue" placeholder="逻辑未删除值"></el-input>
			</el-form-item>
			<el-form-item label="基类字段 0：否  1：是" prop="baseField">
				<el-input v-model="dataForm.baseField" placeholder="基类字段 0：否  1：是"></el-input>
			</el-form-item>
			<el-form-item label="表单项 0：否  1：是" prop="formItem">
				<el-input v-model="dataForm.formItem" placeholder="表单项 0：否  1：是"></el-input>
			</el-form-item>
			<el-form-item label="表单必填 0：否  1：是" prop="formRequired">
				<el-input v-model="dataForm.formRequired" placeholder="表单必填 0：否  1：是"></el-input>
			</el-form-item>
			<el-form-item label="表单类型" prop="formType">
				<el-input v-model="dataForm.formType" placeholder="表单类型"></el-input>
			</el-form-item>
			<el-form-item label="表单字典类型" prop="formDict">
				<el-input v-model="dataForm.formDict" placeholder="表单字典类型"></el-input>
			</el-form-item>
			<el-form-item label="表单效验" prop="formValidator">
				<el-input v-model="dataForm.formValidator" placeholder="表单效验"></el-input>
			</el-form-item>
			<el-form-item label="列表项 0：否  1：是" prop="gridItem">
				<el-input v-model="dataForm.gridItem" placeholder="列表项 0：否  1：是"></el-input>
			</el-form-item>
			<el-form-item label="列表排序 0：否  1：是" prop="gridSort">
				<el-input v-model="dataForm.gridSort" placeholder="列表排序 0：否  1：是"></el-input>
			</el-form-item>
			<el-form-item label="查询项 0：否  1：是" prop="queryItem">
				<el-input v-model="dataForm.queryItem" placeholder="查询项 0：否  1：是"></el-input>
			</el-form-item>
			<el-form-item label="查询方式" prop="queryType">
				<el-input v-model="dataForm.queryType" placeholder="查询方式"></el-input>
			</el-form-item>
			<el-form-item label="查询表单类型" prop="queryFormType">
				<el-input v-model="dataForm.queryFormType" placeholder="查询表单类型"></el-input>
			</el-form-item>
			<el-form-item label="创建时间" prop="createTime">
				<el-input v-model="dataForm.createTime" placeholder="创建时间"></el-input>
			</el-form-item>
			<el-form-item label="修改时间" prop="updateTime">
				<el-input v-model="dataForm.updateTime" placeholder="修改时间"></el-input>
			</el-form-item>
			<el-form-item label="是否删除（0未删除, 1删除）" prop="isDelete">
				<el-input v-model="dataForm.isDelete" placeholder="是否删除（0未删除, 1删除）"></el-input>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button @click="visible = false">取消</el-button>
			<el-button type="primary" @click="submitHandle()">确定</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus/es'
import { tableFieldDetailApi, tableFieldSubmitApi } from '@/api/tableField'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: '',
	tableId: '',
	fieldName: '',
	fieldType: '',
	fieldComment: '',
	attrName: '',
	attrType: '',
	packageName: '',
	sort: '',
	autoFill: '',
	primaryPk: '',
	logicDelete: '',
	logicDeleteValue: '',
	logicNotDeleteValue: '',
	baseField: '',
	formItem: '',
	formRequired: '',
	formType: '',
	formDict: '',
	formValidator: '',
	gridItem: '',
	gridSort: '',
	queryItem: '',
	queryType: '',
	queryFormType: '',
	createTime: '',
	updateTime: '',
	isDelete: ''
})

const init = (id?: number) => {
	visible.value = true
	dataForm.id = null

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	if (id) {
		getTableField(id)
	}
}

const getTableField = (id: number) => {
	tableFieldDetailApi(id).then(res => {
		Object.assign(dataForm, res.data)
	})
}

const dataRules = ref({})

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		tableFieldSubmitApi(dataForm).then(() => {
			ElMessage.success({
				message: '操作成功',
				duration: 500,
				onClose: () => {
					visible.value = false
					emit('refreshDataList')
				}
			})
		})
	})
}

defineExpose({
	init
})
</script>
