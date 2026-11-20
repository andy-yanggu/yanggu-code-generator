package com.yanggu.code.generator.service.support;

import cn.hutool.v7.core.bean.BeanUtil;
import cn.hutool.v7.core.collection.CollUtil;
import cn.hutool.v7.core.date.DateFormatPool;
import cn.hutool.v7.core.date.DateUtil;
import cn.hutool.v7.core.text.NamingCase;
import cn.hutool.v7.core.text.StrUtil;
import cn.hutool.v7.core.util.BooleanUtil;
import cn.hutool.v7.core.util.EnumUtil;
import com.yanggu.code.generator.domain.entity.*;
import com.yanggu.code.generator.domain.model.*;
import com.yanggu.code.generator.enums.DbType;
import com.yanggu.code.generator.mapstruct.BaseClassMapstruct;
import com.yanggu.code.generator.mapstruct.TableFieldMapstruct;
import com.yanggu.code.generator.service.*;
import com.yanggu.code.generator.util.GenUtil;
import com.yanggu.code.generator.util.NameUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 生成器数据模型组装器
 * 将实体数据组装为 FreeMarker 渲染所需的 ProjectModel/TableModel/EnumModel。
 * 项目公共字段（名称变换系列、包路径、前后端路径）与枚举名称变换统一在此填充，消除原三处重复拷贝。
 */
@Component
public class DataModelBuilder {

    @Autowired
    private TableService tableService;

    @Autowired
    private TableFieldService tableFieldService;

    @Autowired
    private EnumService enumService;

    @Autowired
    private FieldTypeService fieldTypeService;

    @Autowired
    private BaseClassService baseClassService;

    @Autowired
    private TemplateGroupPropertyDataService templateGroupPropertyDataService;

    @Autowired
    private TableFieldMapstruct tableFieldMapstruct;

    @Autowired
    private BaseClassMapstruct baseClassMapstruct;

    @Autowired
    private DatasourceService datasourceService;

    /**
     * 构建项目渲染数据
     * datasource 由本方法按 project.datasourceId 自取，调用方无需预查传入
     */
    public ProjectModel buildProjectDataModel(ProjectEntity project) {
        //项目数据
        ProjectModel projectModel = new ProjectModel();
        fillProjectCommon(projectModel, projectNaming(project));
        projectModel.setProjectNameUpperSpace(NameUtil.toSpaceUpperCase(project.getProjectName()));
        projectModel.setProjectVersion(project.getProjectVersion());
        projectModel.setProjectPort(project.getProjectPort());
        projectModel.setProjectDesc(project.getProjectDesc());
        projectModel.setModuleList(BeanUtil.copyToList(project.getModuleList(), ModuleModel.class));

        //数据源数据：内部解析，null 表示项目未绑数据源
        DatasourceEntity datasource = resolveDatasource(project);
        if (datasource != null) {
            projectModel.setDatabaseDriverClassName(EnumUtil.getBy(DbType::getCode, datasource.getDbType()).getDriverClass());
            projectModel.setDatabaseUrl(datasource.getConnUrl());
            projectModel.setDatabaseUsername(datasource.getUsername());
            projectModel.setDatabasePassword(datasource.getPassword());
        }

        // 项目模板属性数据
        projectModel.setProjectTemplatePropertyData(templateGroupPropertyDataService.getData(project.getProjectTemplateGroupId(), project.getTemplateGroupPropertyData()));

        //表模型数据列表
        List<TableModel> tableModelList = new ArrayList<>();
        projectModel.setTableModelList(tableModelList);
        List<TableEntity> tableList = tableService.getTableListByProjectId(project.getId());
        for (TableEntity table : tableList) {
            TableModel tableModel = buildTableDataModel(table, project);
            tableModelList.add(tableModel);
        }

        //枚举模型数据列表
        List<EnumModel> enumModelList = new ArrayList<>();
        projectModel.setEnumModelList(enumModelList);
        List<EnumEntity> enumList = enumService.enumList(project.getId());
        for (EnumEntity enumEntity : enumList) {
            EnumModel enumModel = buildEnumDataModel(enumEntity, project);
            enumModelList.add(enumModel);
        }

        return projectModel;
    }

    /**
     * 构建表渲染的数据模型
     */
    public TableModel buildTableDataModel(TableEntity table, ProjectEntity project) {
        List<TableFieldEntity> fieldList = tableFieldService.getByTableId(table.getId());
        List<TableFieldModel> fieldModelList = tableFieldMapstruct.toModel(fieldList);

        TableModel tableModel = new TableModel();
        fieldModelList.sort(Comparator.comparing(TableFieldModel::getFieldSort));

        fieldModelList.forEach(fieldModel -> {
            // 属性名首字母大写
            fieldModel.setAttrNamePascal(StrUtil.upperFirst(fieldModel.getAttrName()));
            Long enumId = fieldModel.getEnumId();
            if (enumId != null) {
                EnumEntity enumEntity = enumService.getById(enumId);
                fillEnumNaming(fieldModel, enumEntity.getEnumName());
            }
        });
        tableModel.setFieldList(fieldModelList);

        //项目信息
        fillProjectCommon(tableModel, projectNaming(project));
        tableModel.setProjectId(project.getId());
        tableModel.setVersion(table.getVersion());
        tableModel.setEntityBaseClassId(table.getEntityBaseClassId());
        tableModel.setVoBaseClassId(table.getVoBaseClassId());

        tableModel.setModuleName(table.getModuleName());
        tableModel.setModuleNameCase(NameUtil.toCamel(table.getModuleName()));
        tableModel.setModuleNamePascal(NameUtil.toPascal(table.getModuleName()));
        project.getModuleList().stream()
                .filter(module -> module.getModuleName().equals(table.getModuleName()))
                .findFirst()
                .ifPresent(module -> {
                    tableModel.setModulePath(module.getModulePath());
                    tableModel.setModuleDesc(module.getModuleDesc());
                });
        tableModel.setFunctionName(table.getFunctionName());
        tableModel.setFunctionNamePascal(StrUtil.upperFirst(table.getFunctionName()));
        tableModel.setFunctionNameKebabCase(NamingCase.toKebabCase(table.getFunctionName()));
        tableModel.setPermissionFlag(table.getPermissionFlag());
        tableModel.setFormLayout(table.getFormLayout());
        tableModel.setPopupType(table.getPopupType());

        //开发者信息
        tableModel.setAuthor(table.getAuthor());
        tableModel.setDatetime(DateUtil.format(LocalDateTime.now(), DateFormatPool.NORM_DATETIME_PATTERN));
        tableModel.setDate(DateUtil.format(LocalDateTime.now(), DateFormatPool.NORM_DATE_PATTERN));

        //设置字段分类
        setFieldTypeList(tableModel);

        //设置基类信息
        setBaseClass(tableModel);

        // 导入的包列表
        Set<String> importList = fieldTypeService.getPackageByTableId(table.getId());
        tableModel.setImportList(importList);

        //表信息
        tableModel.setTableName(table.getTableName());
        tableModel.setDatabaseName(table.getDatabaseName());
        tableModel.setTableComment(table.getTableComment());
        tableModel.setClassName(StrUtil.lowerFirst(table.getClassName()));
        tableModel.setClassNameUpper(table.getClassName());
        tableModel.setGeneratorFunction(table.getGeneratorFunction());

        //生成方式
        tableModel.setGeneratorType(project.getGeneratorType());

        //模板属性数据
        tableModel.setProjectTemplatePropertyData(project.getTemplateGroupPropertyData());
        tableModel.setTableTemplatePropertyData(templateGroupPropertyDataService.getData(project.getTableTemplateGroupId(), table.getTemplateGroupPropertyData()));
        return tableModel;
    }

    /**
     * 构建枚举渲染的数据模型
     */
    public EnumModel buildEnumDataModel(EnumEntity enumEntity, ProjectEntity project) {
        EnumModel enumModel = new EnumModel();
        fillProjectCommon(enumModel, projectNaming(project));

        fillEnumNaming(enumModel, enumEntity.getEnumName());
        enumModel.setEnumId(enumEntity.getId());
        enumModel.setEnumDesc(enumEntity.getEnumDesc());
        // 枚举可能未配置枚举项：JOIN 装配时嵌套集合为 null，统一兜底为空列表，避免下游 stream NPE
        List<EnumItemEntity> enumItemList = enumEntity.getEnumItemList();
        if (CollUtil.isEmpty(enumItemList)) {
            enumItemList = List.of();
        }
        List<EnumItemModel> list = enumItemList.stream()
                .map(enumItemEntity -> {
                    EnumItemModel enumItemModel = new EnumItemModel();
                    enumItemModel.setEnumItemName(enumItemEntity.getEnumItemName());
                    enumItemModel.setEnumItemNameAllUpper(NameUtil.toAllUpperCase(enumItemEntity.getEnumItemName()));
                    enumItemModel.setEnumItemCode(GenUtil.convertStringToAppropriateType(enumItemEntity.getEnumItemCode()));
                    enumItemModel.setEnumItemDesc(enumItemEntity.getEnumItemDesc());
                    return enumItemModel;
                }).toList();
        enumModel.setEnumItemList(list);
        enumModel.setEnumCodeType(getEnumCodeType(list));

        enumModel.setProjectTemplatePropertyData(project.getTemplateGroupPropertyData());
        enumModel.setEnumTemplatePropertyData(templateGroupPropertyDataService.getData(project.getEnumTemplateGroupId(), enumEntity.getTemplateGroupPropertyData()));
        return enumModel;
    }

    private String getEnumCodeType(List<EnumItemModel> enumItemList) {
        for (EnumItemModel enumItemModel : enumItemList) {
            if (enumItemModel.getEnumItemCode() instanceof String) {
                return "String";
            }
        }
        return "Integer";
    }

    /**
     * 设置字段分类信息
     *
     * @param tableModel 数据模型
     */
    private void setFieldTypeList(TableModel tableModel) {
        // 主键列表 (支持多主键)
        List<TableFieldModel> primaryList = new ArrayList<>();
        // 表单列表
        List<TableFieldModel> formList = new ArrayList<>();
        // 网格列表
        List<TableFieldModel> gridList = new ArrayList<>();
        // 查询列表
        List<TableFieldModel> queryList = new ArrayList<>();

        for (TableFieldModel field : tableModel.getFieldList()) {
            if (field.getPrimaryPk() == 1) {
                primaryList.add(field);
            }
            if (field.getFormItem() == 1) {
                formList.add(field);
            }
            if (field.getGridItem() == 1) {
                gridList.add(field);
            }
            if (field.getQueryItem() == 1) {
                queryList.add(field);
            }
        }
        tableModel.setPrimaryList(primaryList);
        formList.sort(Comparator.comparingInt(TableFieldModel::getFormFieldSort));
        tableModel.setFormList(formList);
        gridList.sort(Comparator.comparingInt(TableFieldModel::getGridFieldSort));
        tableModel.setGridList(gridList);
        queryList.sort(Comparator.comparingInt(TableFieldModel::getQueryFieldSort));
        tableModel.setQueryList(queryList);
    }

    /**
     * 设置基类信息
     *
     * @param tableModel 数据模型
     */
    private void setBaseClass(TableModel tableModel) {
        List<TableFieldModel> fieldList = tableModel.getFieldList();
        //Entity基类
        fieldList.forEach(field -> field.setEntityBaseField(0));
        if (tableModel.getEntityBaseClassId() != null) {
            BaseClassEntity baseClass = baseClassService.getById(tableModel.getEntityBaseClassId());
            if (baseClass != null) {
                BaseClassModel baseClassModel = baseClassMapstruct.toModel(baseClass);
                tableModel.setEntityBaseClass(baseClassModel);

                // 标注为基类字段
                for (TableFieldModel field : fieldList) {
                    field.setEntityBaseField(BooleanUtil.toInteger(CollUtil.contains(baseClassModel.getFieldList(), field.getAttrName())));
                }
            }
        }

        //VO基类
        fieldList.forEach(field -> field.setVoBaseField(0));
        if (tableModel.getVoBaseClassId() != null) {
            BaseClassEntity voBaseClass = baseClassService.getById(tableModel.getVoBaseClassId());
            if (voBaseClass != null) {
                BaseClassModel baseClassModel = baseClassMapstruct.toModel(voBaseClass);
                tableModel.setVoBaseClass(baseClassModel);
                for (TableFieldModel field : fieldList) {
                    field.setVoBaseField(BooleanUtil.toInteger(CollUtil.contains(baseClassModel.getFieldList(), field.getAttrName())));
                }
            }
        }
    }

    // ==================== 内部辅助（私有） ====================

    /**
     * 解析项目的数据源（无数据源时返回 null）
     */
    private DatasourceEntity resolveDatasource(ProjectEntity project) {
        Long datasourceId = project.getDatasourceId();
        return datasourceId == null ? null : datasourceService.getById(datasourceId);
    }

    /**
     * 项目公共命名信息（一次计算，多处填充）
     */
    private record ProjectNaming(String name, String underline, String pascal, String dot, String slash,
                                 String pkg, String pkgSlash, String backendPath, String frontendPath) {
    }

    private ProjectNaming projectNaming(ProjectEntity project) {
        String projectName = project.getProjectName();
        return new ProjectNaming(projectName, NameUtil.toUnderLine(projectName), NameUtil.toPascal(projectName),
                NameUtil.toDot(projectName), NameUtil.toSlash(projectName),
                project.getProjectPackage(), StrUtil.replace(project.getProjectPackage(), ".", "/"),
                project.getBackendPath(), project.getFrontendPath());
    }

    private void fillProjectCommon(ProjectModel model, ProjectNaming n) {
        model.setProjectName(n.name());
        model.setProjectNameUnderline(n.underline());
        model.setProjectNamePascal(n.pascal());
        model.setProjectNameDot(n.dot());
        model.setProjectNameSlash(n.slash());
        model.setProjectPackage(n.pkg());
        model.setProjectPackageSlash(n.pkgSlash());
        model.setBackendPath(n.backendPath());
        model.setFrontendPath(n.frontendPath());
    }

    private void fillProjectCommon(TableModel model, ProjectNaming n) {
        model.setProjectName(n.name());
        model.setProjectNameUnderline(n.underline());
        model.setProjectNamePascal(n.pascal());
        model.setProjectNameDot(n.dot());
        model.setProjectNameSlash(n.slash());
        model.setProjectPackage(n.pkg());
        model.setProjectPackageSlash(n.pkgSlash());
        model.setBackendPath(n.backendPath());
        model.setFrontendPath(n.frontendPath());
    }

    private void fillProjectCommon(EnumModel model, ProjectNaming n) {
        model.setProjectNameDot(n.dot());
        model.setProjectNameSlash(n.slash());
        model.setProjectPackage(n.pkg());
        model.setProjectPackageSlash(n.pkgSlash());
        model.setBackendPath(n.backendPath());
        model.setFrontendPath(n.frontendPath());
    }

    private void fillEnumNaming(TableFieldModel field, String enumName) {
        field.setEnumName(enumName);
        field.setEnumNamePascal(NameUtil.toPascal(enumName));
        field.setEnumNameAllUpper(NameUtil.toAllUpperCase(enumName));
    }

    private void fillEnumNaming(EnumModel model, String enumName) {
        model.setEnumName(enumName);
        model.setEnumNamePascal(NameUtil.toPascal(enumName));
        model.setEnumNameAllUpper(NameUtil.toAllUpperCase(enumName));
    }
}
