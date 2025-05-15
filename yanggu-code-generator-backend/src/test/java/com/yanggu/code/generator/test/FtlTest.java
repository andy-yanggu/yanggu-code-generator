package com.yanggu.code.generator.test;


import com.yanggu.code.generator.domain.model.EnumDataModel;
import com.yanggu.code.generator.domain.model.ProjectDataModel;
import com.yanggu.code.generator.util.TemplateUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

class FtlTest {

    @Test
    void test1() {
        ProjectDataModel projectDataModel = new ProjectDataModel();
        EnumDataModel enumDataModel = new EnumDataModel();

        projectDataModel.setEnumDataModelList(List.of(enumDataModel));
        enumDataModel.setTableName("student");
        enumDataModel.setFieldName("sex");
        enumDataModel.setFieldComment("性别");

        EnumDataModel.EnumValueModel enumValueModel = new EnumDataModel.EnumValueModel();
        enumValueModel.setLabel("男");
        enumValueModel.setValue("0");
        enumValueModel.setLabel("女");
        enumValueModel.setValue("1");
        enumDataModel.setValueList(List.of(enumValueModel));

        projectDataModel.setEnumDataModelList(projectDataModel.getEnumDataModelList());
        projectDataModel.setTemplateName("enum.ftl");

        String templateContent = "<#list enumDataModelList as enumDataModel>\n" +
                "// ${enumDataModel.fieldComment}枚举\n" +
                "export const ${enumDataModel.tableName}_${enumDataModel.fieldName}_enum = [\n" +
                "<#list enumDataModel.valueList as enumData>\n" +
                "   { label: '${enumData.label}', value: '${enumData.value}' }<#if enumData_has_next>,</#if>\n" +
                "</#list>\n" +
                "]\n" +
                "</#list>";
        System.out.println(templateContent);
        String content = TemplateUtils.getContent(templateContent, projectDataModel);
        System.out.println(content);
    }

}
