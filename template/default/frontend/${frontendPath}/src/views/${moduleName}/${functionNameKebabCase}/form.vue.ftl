<template>
<#function getPopupType>
    <#if popupType == 0>
        <#return 'dialog'>
    <#else>
        <#return 'drawer'>
    </#if>
</#function>
    <el-${getPopupType()} v-model="visible" :title="dialogTitle()" :close-on-click-modal="false">
        <el-form ref="dataFormRef" :model="state.dataForm" :rules="dataRules" label-width="100px" @keyup.enter="submitHandle()">
            <#macro formItem field>
            <el-form-item label="${field.fieldComment!}" prop="${field.attrName}">
                <#if field.formType == 'text'>
                <el-input v-model="state.dataForm.${field.attrName}" clearable placeholder="请输入${field.fieldComment!}"></el-input>
                <#elseif field.formType == 'textarea'>
                <el-input type="textarea" v-model="state.dataForm.${field.attrName}"></el-input>
                <#elseif field.formType == 'editor'>
                <el-input type="textarea" v-model="state.dataForm.${field.attrName}"></el-input>
                <#elseif field.formType == 'select'>
                <el-select
                    v-model="state.dataForm.${field.attrName}"
                    :options="${field.enumNameAllUpper}_ENUM"
                    clearable
                    filterable
                    placeholder="请选择${field.fieldComment!}"
                ></el-select>
                <#elseif field.formType == 'radio'>
                <el-radio-group
                    v-model="state.dataForm.${field.attrName}"
                    :options="${field.enumNameAllUpper}_ENUM"
                ></el-radio-group>
                <#elseif field.formType == 'checkbox'>
                <el-checkbox-group
                    v-model="state.dataForm.${field.attrName}"
                    :options="${field.enumNameAllUpper}_ENUM"
                ></el-checkbox-group>
                <#elseif field.formType == 'date'>
                <el-date-picker
                    type="date"
                    placeholder="请选择${field.fieldComment!}"
                    v-model="state.dataForm.${field.attrName}"
                ></el-date-picker>
                <#elseif field.formType == 'datetime'>
                <el-date-picker
                    type="datetime"
                    placeholder="请选择${field.fieldComment!}"
                    v-model="state.dataForm.${field.attrName}"
                ></el-date-picker>
                <#elseif field.formType == 'number'>
                <el-input-number
                    v-model="state.dataForm.${field.attrName}"
                    size="small"
                ></el-input-number>
                <#else>
                <el-input
                    v-model="state.dataForm.${field.attrName}"
                    clearable
                    placeholder="请输入${field.fieldComment!}"
                ></el-input>
                </#if>
            </el-form-item>
            </#macro>
            <#if formLayout == 2>
                <#list formList?chunk(2) as row>
                    <#if row?size == 2>
                        <el-row :gutter="20">
                            <el-col :span="12">
                                <@formItem field=row[0] />
                            </el-col>
                            <el-col :span="12">
                                <@formItem field=row[1] />
                            </el-col>
                        </el-row>
                    <#else>
                        <!-- 单个时不使用 el-row -->
                        <@formItem field=row[0] />
                    </#if>
                </#list>
            <#else>
                <#list formList as field>
                    <@formItem field=field />
                </#list>
            </#if>
        </el-form>
    		<template #footer>
      			<el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitHandle()">确定</el-button>
      			<el-button :icon="Close" @click="visible = false">取消</el-button>
    		</template>
    </el-${getPopupType()}>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ${moduleNameCase}${functionNamePascal}Api } from '@/api'
import { FormOptions, ${moduleNamePascal}${functionNamePascal}Entity } from '@/types'
import { useSubmitForm } from '@/hooks'
import { Check, Close } from '@element-plus/icons-vue'
import { FormItemRule } from 'element-plus'
<#list formList as field>
    <#if field.formType == 'select' || field.formType == 'checkbox' || field.formType == 'radio'>
import { ${field.enumNameAllUpper}_ENUM } from '@/constant/enum'
    </#if>
</#list>

defineOptions({
	name: '${moduleNamePascal}${functionNamePascal}Form'
})
                      
const emit = defineEmits(['refreshDataList'])

// 初始化表单数据方法
const initFormData = (): ${moduleNamePascal}${functionNamePascal}Entity => ({
<#list fieldList as field>
  <#if field.entityBaseField == 0>                                              
    <#if field.formType == 'date'>
  ${field.attrName}: []<#sep>, </#sep>
    <#elseif field.formType == 'datetime'>
  ${field.attrName}: []<#sep>, </#sep>
    <#elseif field.formType == 'checkbox'>
  ${field.attrName}: []<#sep>, </#sep>
    <#elseif field.formType == 'number'>
  ${field.attrName}: -1<#sep>, </#sep>
    <#else>
  ${field.attrName}: ''<#sep>, </#sep>
    </#if>
  </#if>                    
</#list>
})                      

const state = reactive({
    // 标题主体
	  titleSubject: '${tableComment}',
    // 提交API
    submitApi: ${moduleNameCase}${functionNamePascal}Api.submit,
    // 详情API
    detailApi: ${moduleNameCase}${functionNamePascal}Api.detail,
    // 初始化表单数据方法
	  initFormData,
    // 表单数据
	  dataForm: initFormData(),
    emit
} as FormOptions<${moduleNamePascal}${functionNamePascal}Entity>)

const dataRules = reactive({
    <#list formList as field>
        <#if field.formRequired == 1>
    ${field.attrName}: [
        { required: true, message: '${field.fieldComment!}不能为空', trigger: 'blur' }
    ]<#if field_has_next>,</#if>
        </#if>
    </#list>
} as Record<keyof ${moduleNamePascal}${functionNamePascal}Entity, FormItemRule[]>)

const { visible, dataFormRef, dialogTitle, init, submitHandle, submitLoading } = useSubmitForm(state)

defineExpose({
	init
})
</script>
