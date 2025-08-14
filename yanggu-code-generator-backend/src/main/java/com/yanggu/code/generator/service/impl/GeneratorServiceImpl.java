package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.bo.DataSourceBO;
import com.yanggu.code.generator.domain.entity.*;
import com.yanggu.code.generator.domain.model.*;
import com.yanggu.code.generator.domain.query.*;
import com.yanggu.code.generator.domain.vo.PreviewTemplateVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import com.yanggu.code.generator.enums.GeneratorProductTypeEnum;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.enums.TemplateTypeEnum;
import com.yanggu.code.generator.mapstruct.BaseClassMapstruct;
import com.yanggu.code.generator.mapstruct.TableFieldMapstruct;
import com.yanggu.code.generator.mapstruct.TemplateMapstruct;
import com.yanggu.code.generator.service.*;
import com.yanggu.code.generator.util.NameUtil;
import com.yanggu.code.generator.util.TemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.core.codec.binary.Base64;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.date.DateFormatPool;
import org.dromara.hutool.core.date.DateUtil;
import org.dromara.hutool.core.io.IoUtil;
import org.dromara.hutool.core.io.file.FileUtil;
import org.dromara.hutool.core.text.NamingCase;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.util.BooleanUtil;
import org.dromara.hutool.core.util.CharsetUtil;
import org.dromara.hutool.core.util.EnumUtil;
import org.dromara.hutool.http.meta.HttpHeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.dromara.hutool.core.date.DateFormatPool.PURE_DATETIME_PATTERN;

/**
 * 代码生成服务实现类
 */
@Slf4j
@Service
public class GeneratorServiceImpl implements GeneratorService {

    @Autowired
    private TableService tableService;

    @Autowired
    private TableFieldService tableFieldService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DatasourceService datasourceService;

    @Autowired
    private BaseClassService baseClassService;

    @Autowired
    private FieldTypeService fieldTypeService;

    @Autowired
    private TemplateGroupService templateGroupService;

    @Autowired
    private TableFieldMapstruct tableFieldMapstruct;

    @Autowired
    private TemplateMapstruct templateMapstruct;

    @Autowired
    private BaseClassMapstruct baseClassMapstruct;

    @Autowired
    private EnumService enumService;

    @Override
    public List<PreviewTemplateVO> preview(CodePreviewQuery codePreviewQuery) throws Exception {
        Integer generatorProductType = codePreviewQuery.getGeneratorProductType();
        Long previewProductId = codePreviewQuery.getPreviewProductId();

        List<TemplateContentVO> templateContentList;
        switch (EnumUtil.getBy(GeneratorProductTypeEnum::getProductType, generatorProductType)) {
            case PROJECT -> {
                GeneratorProjectQuery projectQuery = new GeneratorProjectQuery();
                projectQuery.setProjectId(previewProductId);
                templateContentList = buildProjectPreview(projectQuery);
            }
            case TABLE -> {
                GeneratorTableQuery tableQuery = new GeneratorTableQuery();
                tableQuery.setTableIdList(List.of(previewProductId));
                templateContentList = tablePreview(tableQuery);
            }
            case ENUM -> {
                GeneratorEnumQuery enumQuery = new GeneratorEnumQuery();
                enumQuery.setEnumIdList(List.of(previewProductId));
                templateContentList = enumPreview(enumQuery);
            }
            default -> throw new BusinessException("Invalid generator product type: " + generatorProductType);
        }
        return buildTree(templateContentList);
    }

    @Override
    public ResponseEntity<byte[]> downloadSingle(CodeSingleGeneratorQuery singleGeneratorQuery) throws Exception {
        Long id = singleGeneratorQuery.getId();
        Integer templateGroupType = singleGeneratorQuery.getTemplateGroupType();
        List<Long> templateIdList = List.of(singleGeneratorQuery.getTemplateId());

        TemplateContentVO preview;
        switch (EnumUtil.getBy(TemplateGroupTypeEnum::getCode, templateGroupType)) {
            case PROJECT -> {
                ProjectEntity project = projectService.getById(id);
                preview = projectPreview(project, templateIdList).getFirst();
            }
            case TABLE -> {
                GeneratorTableQuery tableQuery = new GeneratorTableQuery();
                tableQuery.setTableIdList(List.of(id));
                tableQuery.setTemplateIdList(templateIdList);
                preview = tablePreview(tableQuery).getFirst();
            }
            case ENUM -> {
                GeneratorEnumQuery enumQuery = new GeneratorEnumQuery();
                enumQuery.setEnumIdList(List.of(id));
                enumQuery.setTemplateIdList(templateIdList);
                preview = enumPreview(enumQuery).getFirst();
            }
            default -> throw new BusinessException("Invalid template group type: " + templateGroupType);
        }
        return buildResponseEntity(preview.getFileName(), preview.getContent().getBytes());
    }

    @Override
    public void singleLocal(CodeSingleGeneratorQuery singleGeneratorQuery) throws Exception {
        Long id = singleGeneratorQuery.getId();
        Integer templateGroupType = singleGeneratorQuery.getTemplateGroupType();
        List<Long> templateIdList = List.of(singleGeneratorQuery.getTemplateId());

        switch (EnumUtil.getBy(TemplateGroupTypeEnum::getCode, templateGroupType)) {
            case PROJECT -> {
                GeneratorProjectQuery projectQuery = new GeneratorProjectQuery();
                projectQuery.setProjectId(id);
                projectQuery.setProjectTemplateIdList(templateIdList);
                projectDownloadLocal(projectQuery);
            }
            case TABLE -> {
                GeneratorTableQuery tableQuery = new GeneratorTableQuery();
                tableQuery.setTableIdList(List.of(id));
                tableQuery.setTemplateIdList(templateIdList);
                tableDownloadLocal(tableQuery);
            }
            case ENUM -> {
                GeneratorEnumQuery enumQuery = new GeneratorEnumQuery();
                enumQuery.setEnumIdList(List.of(id));
                enumQuery.setTemplateIdList(templateIdList);
                enumDownloadLocal(enumQuery);
            }
            default -> throw new BusinessException("模板组类型异常: " + templateGroupType);
        }
    }

    @Override
    public void projectDownloadLocal(GeneratorProjectQuery projectQuery) throws Exception {
        List<TemplateContentVO> list = buildProjectPreview(projectQuery);
        downloadLocal(list);
    }

    @Override
    public ResponseEntity<byte[]> projectDownloadZip(GeneratorProjectQuery projectQuery) throws Exception {
        List<TemplateContentVO> list = buildProjectPreview(projectQuery);

        //构建zip文件名
        ProjectEntity project = projectService.getById(projectQuery.getProjectId());

        //生成zip文件
        return downloadZip(project.getProjectName(), list);
    }

    @Override
    public void tableDownloadLocal(GeneratorTableQuery tableQuery) {
        List<TemplateContentVO> list = tablePreview(tableQuery);
        downloadLocal(list);
    }

    @Override
    public ResponseEntity<byte[]> tableDownloadZip(GeneratorTableQuery tableQuery) {
        List<TemplateContentVO> list = tablePreview(tableQuery);

        // 拼接表名
        String tableNameConcat = tableService.listByIds(tableQuery.getTableIdList()).stream()
                .map(TableEntity::getTableName)
                .collect(Collectors.joining("_"));

        return downloadZip(tableNameConcat, list);
    }

    @Override
    public void enumDownloadLocal(GeneratorEnumQuery enumQuery) {
        List<TemplateContentVO> enumPreviewData = enumPreview(enumQuery);
        downloadLocal(enumPreviewData);
    }

    @Override
    public ResponseEntity<byte[]> enumDownloadZip(GeneratorEnumQuery enumQuery) {
        List<TemplateContentVO> enumPreviewData = enumPreview(enumQuery);

        //拼接枚举名称
        List<Long> enumIdList = enumQuery.getEnumIdList();
        String enumNameConcat = enumService.listByIds(enumIdList).stream()
                .map(EnumEntity::getEnumName)
                .collect(Collectors.joining("_"));

        return downloadZip(enumNameConcat, enumPreviewData);
    }

    private List<TemplateContentVO> buildProjectPreview(GeneratorProjectQuery projectQuery) throws Exception {
        Long projectId = projectQuery.getProjectId();
        //查询项目
        ProjectEntity project = projectService.getById(projectId);

        List<TemplateContentVO> allPreviewList = new ArrayList<>();

        //获取表预览数据
        //查询该项目下的表
        List<TableEntity> tableList = tableService.list(Wrappers.<TableEntity>lambdaQuery().eq(TableEntity::getProjectId, projectId));
        List<Long> tableIdList = projectQuery.getTableIdList();
        if (CollUtil.isNotEmpty(tableIdList)) {
            tableList = tableList.stream()
                    .filter(table -> tableIdList.contains(table.getId()))
                    .toList();
        }
        List<TemplateContentVO> tablePreviewList = tableListPreview(tableList, projectQuery.getTableTemplateIdList());
        allPreviewList.addAll(tablePreviewList);

        //获取枚举预览数据
        List<TemplateContentVO> enumPreviewList = enumListPreview(project, projectQuery.getEnumIdList(), projectQuery.getEnumTemplateIdList());
        allPreviewList.addAll(enumPreviewList);

        //获取项目预览数据
        List<TemplateContentVO> projectPreviewList = projectPreview(project, projectQuery.getProjectTemplateIdList());
        allPreviewList.addAll(projectPreviewList);

        return allPreviewList;
    }

    private List<TemplateContentVO> tablePreview(GeneratorTableQuery tableQuery) {
        Long tableId = tableQuery.getTableIdList().getFirst();

        // 表信息
        TableEntity table = tableService.getById(tableId);

        ProjectEntity project = projectService.getById(table.getProjectId());

        //获取数据模型
        TableModel tableModel = buildTableDataModel(table, project);

        Long tableTemplateGroupId = tableService.getTableTemplateGroupId(tableId);

        TemplateGroupEntity templateGroup = templateGroupService.getById(tableTemplateGroupId);
        List<TemplateVO> templateList = buildTemplateTreeWithPaths(templateGroup.getTemplateList());
        List<Long> templateIdList = tableQuery.getTemplateIdList();
        if (CollUtil.isNotEmpty(templateIdList)) {
            templateList = templateList.stream()
                    .filter(template -> templateIdList.contains(template.getId()))
                    .toList();
        }

        //渲染模板并输出
        return templateList.stream()
                .map(template -> {
                    TemplateContentVO templateContentVO = getTemplateContentVO(templateGroup, template, tableModel);
                    templateContentVO.setTableId(tableId);
                    return templateContentVO;
                })
                .toList();
    }

    private List<TemplateContentVO> enumPreview(GeneratorEnumQuery enumQuery) {
        Long enumId = enumQuery.getEnumIdList().getFirst();
        EnumEntity enumEntity = enumService.getById(enumId);
        ProjectEntity project = projectService.getById(enumEntity.getProjectId());
        List<Long> templateIdList = enumQuery.getTemplateIdList();

        //查询项目对应的枚举模板
        TemplateGroupEntity templateGroup = templateGroupService.getById(project.getEnumTemplateGroupId());
        List<TemplateVO> templateList = buildTemplateTreeWithPaths(templateGroup.getTemplateList());
        List<TemplateContentVO> list = new ArrayList<>();
        templateList.stream()
                .filter(template -> CollUtil.isEmpty(templateIdList) || templateIdList.contains(template.getId()))
                .forEach(template -> {
                    EnumModel enumModel = buildEnumDataModel(enumEntity, project);
                    TemplateContentVO templateContentVO = getTemplateContentVO(templateGroup, template, enumModel);
                    templateContentVO.setEnumId(enumId);
                    list.add(templateContentVO);
                });
        return list;
    }

    private List<TemplateContentVO> projectPreview(ProjectEntity project, List<Long> templateIdList) throws Exception {
        //获取数据源信息
        DataSourceBO dataSource = datasourceService.get(project.getDatasourceId());
        //获取项目模板组信息
        Long projectTemplateGroupId = project.getProjectTemplateGroupId();
        TemplateGroupEntity templateGroup = templateGroupService.getById(projectTemplateGroupId);
        List<TemplateVO> projecTemplateList = buildTemplateTreeWithPaths(templateGroup.getTemplateList());

        ProjectModel projectModel = buildProjectDataModel(project, dataSource);
        return projecTemplateList.stream()
                .filter(template -> CollUtil.isEmpty(templateIdList) || templateIdList.contains(template.getId()))
                .map(template -> getTemplateContentVO(templateGroup, template, projectModel))
                .toList();
    }

    private List<TemplateContentVO> tableListPreview(List<TableEntity> tableList, List<Long> tableTemplateIdList) {
        return tableList.stream()
                .map(
                        table -> {
                            GeneratorTableQuery tableQuery = new GeneratorTableQuery();
                            tableQuery.setTableIdList(List.of(table.getId()));
                            tableQuery.setTemplateIdList(tableTemplateIdList);
                            return tablePreview(tableQuery);
                        }
                )
                .flatMap(Collection::stream)
                .toList();
    }

    private List<TemplateContentVO> enumListPreview(ProjectEntity project, List<Long> enmumIdList, List<Long> enumTemplateIdList) {
        List<EnumModel> enumModelList = new ArrayList<>();
        List<EnumEntity> enumList = enumService.enumList(project.getId());
        enumList = enumList.stream()
                .filter(enumEntity -> CollUtil.isEmpty(enmumIdList) || enmumIdList.contains(enumEntity.getId()))
                .toList();
        for (EnumEntity enumEntity : enumList) {
            EnumModel dataModel = buildEnumDataModel(enumEntity, project);
            enumModelList.add(dataModel);
        }

        //查询项目对应的枚举模板
        TemplateGroupEntity templateGroup = templateGroupService.getById(project.getEnumTemplateGroupId());
        List<TemplateVO> templateList = buildTemplateTreeWithPaths(templateGroup.getTemplateList()).stream()
                .filter(template -> CollUtil.isEmpty(enumTemplateIdList) || enumTemplateIdList.contains(template.getId()))
                .toList();
        return enumModelList.stream()
                .flatMap(enumModel -> templateList.stream()
                        .map(template -> {
                            TemplateContentVO templateContentVO = getTemplateContentVO(templateGroup, template, enumModel);
                            templateContentVO.setEnumId(enumModel.getEnumId());
                            return templateContentVO;
                        })
                )
                .toList();
    }

    /**
     * 构建项目渲染数据
     */
    private ProjectModel buildProjectDataModel(ProjectEntity project, DataSourceBO dataSource) {
        //项目数据
        ProjectModel projectModel = new ProjectModel();
        String projectName = project.getProjectName();
        projectModel.setProjectName(projectName);
        projectModel.setProjectNameUnderline(NameUtil.toUnderLine(projectName));
        projectModel.setProjectNamePascal(NameUtil.toPascal(projectName));
        projectModel.setProjectNameUpperSpace(NameUtil.toSpaceUpperCase(projectName));
        projectModel.setProjectNameDot(NameUtil.toDot(projectName));
        projectModel.setProjectNameSlash(NameUtil.toSlash(projectName));
        projectModel.setProjectPackage(project.getProjectPackage());
        projectModel.setProjectPackageSlash(StrUtil.replace(project.getProjectPackage(), ".", "/"));
        projectModel.setProjectVersion(project.getProjectVersion());
        projectModel.setBackendPath(project.getBackendPath());
        projectModel.setFrontendPath(project.getFrontendPath());
        projectModel.setProjectDesc(project.getProjectDesc());

        //数据源数据
        projectModel.setDatabaseDriverClassName(dataSource.getDbType().getDriverClass());
        projectModel.setDatabaseUrl(dataSource.getConnUrl());
        projectModel.setDatabaseUsername(dataSource.getUsername());
        projectModel.setDatabasePassword(dataSource.getPassword());

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
    private TableModel buildTableDataModel(TableEntity table, ProjectEntity project) {
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
                fieldModel.setEnumName(enumEntity.getEnumName());
                fieldModel.setEnumNamePascal(NameUtil.toPascal(enumEntity.getEnumName()));
                fieldModel.setEnumNameAllUpper(NameUtil.toAllUpperCase(enumEntity.getEnumName()));
            }
        });
        tableModel.setFieldList(fieldModelList);

        // 获取数据库类型
        String dbType = datasourceService.getDatabaseProductName(project.getDatasourceId());
        tableModel.setDbType(dbType);

        //项目信息
        tableModel.setProjectId(project.getId());
        tableModel.setProjectName(project.getProjectName());
        tableModel.setProjectNameUnderline(NameUtil.toUnderLine(project.getProjectName()));
        tableModel.setProjectNamePascal(NameUtil.toPascal(project.getProjectName()));
        tableModel.setProjectNameDot(NameUtil.toDot(project.getProjectName()));
        tableModel.setProjectNameSlash(NameUtil.toSlash(project.getProjectName()));
        tableModel.setProjectPackage(project.getProjectPackage());
        tableModel.setProjectPackageSlash(StrUtil.replace(project.getProjectPackage(), ".", "/"));
        tableModel.setVersion(table.getVersion());

        tableModel.setFunctionName(table.getFunctionName());
        tableModel.setFunctionNamePascal(StrUtil.upperFirst(table.getFunctionName()));
        tableModel.setFunctionNameKebabCase(NamingCase.toKebabCase(table.getFunctionName()));
        tableModel.setFormLayout(table.getFormLayout());

        //开发者信息
        tableModel.setAuthor(table.getAuthor());
        tableModel.setDatetime(DateUtil.format(new Date(), DateFormatPool.NORM_DATETIME_PATTERN));
        tableModel.setDate(DateUtil.format(new Date(), DateFormatPool.NORM_DATE_PATTERN));

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

        //前后端生成路径
        tableModel.setBackendPath(project.getBackendPath());
        tableModel.setFrontendPath(project.getFrontendPath());

        //生成方式
        tableModel.setGeneratorType(project.getGeneratorType());
        return tableModel;
    }

    private EnumModel buildEnumDataModel(EnumEntity enumEntity, ProjectEntity project) {
        EnumModel enumModel = new EnumModel();

        enumModel.setProjectNameDot(NameUtil.toDot(project.getProjectName()));
        enumModel.setProjectNameSlash(NameUtil.toSlash(project.getProjectName()));
        enumModel.setProjectPackage(project.getProjectPackage());
        enumModel.setProjectPackageSlash(StrUtil.replace(project.getProjectPackage(), ".", "/"));
        enumModel.setBackendPath(project.getBackendPath());
        enumModel.setFrontendPath(project.getFrontendPath());

        enumModel.setEnumId(enumEntity.getId());
        enumModel.setEnumName(enumEntity.getEnumName());
        enumModel.setEnumNamePascal(NameUtil.toPascal(enumEntity.getEnumName()));
        enumModel.setEnumNameAllUpper(NameUtil.toAllUpperCase(enumEntity.getEnumName()));
        enumModel.setEnumDesc(enumEntity.getEnumDesc());
        List<EnumItemEntity> enumItemList = enumEntity.getEnumItemList();
        List<EnumItemModel> list = enumItemList.stream()
                .map(enumItemEntity -> {
                    EnumItemModel enumItemModel = new EnumItemModel();
                    enumItemModel.setEnumItemName(enumItemEntity.getEnumItemName());
                    enumItemModel.setEnumItemNameAllUpper(NameUtil.toAllUpperCase(enumItemEntity.getEnumItemName()));
                    enumItemModel.setEnumItemCode(enumItemEntity.getEnumItemCode());
                    enumItemModel.setEnumItemDesc(enumItemEntity.getEnumItemDesc());
                    return enumItemModel;
                }).toList();
        enumModel.setEnumItemList(list);
        return enumModel;
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
        Long projectId = tableModel.getProjectId();
        ProjectEntity project = projectService.getById(projectId);

        //Entity基类
        BaseClassEntity baseClass = baseClassService.getById(project.getEntityBaseClassId());
        List<TableFieldModel> fieldList = tableModel.getFieldList();
        fieldList.forEach(field -> field.setEntityBaseField(0));
        if (baseClass != null) {
            BaseClassModel baseClassModel = baseClassMapstruct.toModel(baseClass);
            tableModel.setEntityBaseClass(baseClassModel);

            // 基类字段
            String[] fields = baseClassModel.getFields().split(",");

            // 标注为基类字段
            for (TableFieldModel field : fieldList) {
                field.setEntityBaseField(BooleanUtil.toInteger(ArrayUtil.contains(fields, field.getAttrName())));
            }
        }

        //VO基类
        BaseClassEntity voBaseClass = baseClassService.getById(project.getVoBaseClassId());
        fieldList.forEach(field -> field.setVoBaseField(0));
        if (voBaseClass != null) {
            BaseClassModel baseClassModel = baseClassMapstruct.toModel(voBaseClass);
            tableModel.setVoBaseClass(baseClassModel);
            // 基类字段
            String[] fields = baseClassModel.getFields().split(",");
            for (TableFieldModel field : fieldList) {
                field.setVoBaseField(BooleanUtil.toInteger(ArrayUtil.contains(fields, field.getAttrName())));
            }
        }
    }

    private TemplateContentVO getTemplateContentVO(TemplateGroupEntity templateGroup, TemplateVO template, Object dataModel) {
        String templateName = template.getTemplateName();
        Integer templateType = template.getTemplateType();
        String templateContent = template.getTemplateContent();
        String fileContent;
        switch (EnumUtil.getBy(TemplateTypeEnum::getCode, templateType)) {
            case DIRECTORY -> fileContent = "";
            // 渲染成文件内容
            case TEMPLATE_FILE -> fileContent = TemplateUtil.renderTemplate(templateName, templateContent, dataModel);
            // 二进制文件
            case BINARY_FILE -> fileContent = templateContent;
            default -> throw new RuntimeException("未知模板类型");
        }
        //路径
        String filePath = TemplateUtil.renderTemplate(templateName, template.getGeneratorPath(), dataModel);
        //文件名
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        TemplateContentVO templateContentVO = new TemplateContentVO();
        templateContentVO.setContent(fileContent);
        templateContentVO.setFileName(fileName);
        templateContentVO.setFilePath(filePath);
        templateContentVO.setTemplateId(template.getId());
        templateContentVO.setTemplateGroupType(templateGroup.getType());
        templateContentVO.setTemplateType(templateType);
        return templateContentVO;
    }

    private ResponseEntity<byte[]> downloadZip(String originFileName, List<TemplateContentVO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new BusinessException("待写入数据为空");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (TemplateContentVO templateContentVO : list) {
            try {
                // 获取文件路径
                String filePath = templateContentVO.getFilePath();

                Integer templateType = templateContentVO.getTemplateType();
                switch (EnumUtil.getBy(TemplateTypeEnum::getCode, templateType)) {
                    case DIRECTORY -> {
                        // 文件夹：创建以'/'结尾的目录条目
                        if (!filePath.endsWith("/")) {
                            filePath += "/";
                        }
                        zip.putNextEntry(new ZipEntry(filePath));
                    }
                    case TEMPLATE_FILE -> {
                        // 文件：添加条目并写入内容
                        zip.putNextEntry(new ZipEntry(filePath));
                        IoUtil.writeUtf8(zip, false, templateContentVO.getContent());
                        zip.flush();
                    }
                    case BINARY_FILE -> {
                        // 二进制文件：添加条目并写入内容
                        zip.putNextEntry(new ZipEntry(filePath));
                        IoUtil.write(zip, false, Base64.decode(templateContentVO.getContent().split(",")[1]));
                        zip.flush();
                    }
                    default -> throw new BusinessException("未知模板类型" + templateType);
                }
                zip.closeEntry();
            } catch (IOException e) {
                throw new BusinessException("模板写入失败：" + templateContentVO.getFilePath(), e);
            }
        }
        IoUtil.closeQuietly(zip);

        // zip压缩包数据
        byte[] data = outputStream.toByteArray();

        String fileName = originFileName + "_" + DateUtil.format(new Date(), PURE_DATETIME_PATTERN) + ".zip";

        return buildResponseEntity(fileName, data);
    }

    private void downloadLocal(List<TemplateContentVO> list) {
        if (CollUtil.isEmpty(list)) {
            log.warn("待写入数据为空");
            return;
        }
        for (TemplateContentVO templateContentVO : list) {
            Integer templateType = templateContentVO.getTemplateType();
            String filePath = templateContentVO.getFilePath();
            String content = templateContentVO.getContent();
            switch (EnumUtil.getBy(TemplateTypeEnum::getCode, templateType)) {
                //生成文件夹
                case DIRECTORY -> FileUtil.mkdir(filePath);
                //直接使用覆盖的方式生成文件
                case TEMPLATE_FILE -> FileUtil.writeUtf8String(content, filePath);
                //生成二进制文件
                case BINARY_FILE -> FileUtil.writeBytes(Base64.decode(content.split(",")[1]), filePath);
                default -> throw new BusinessException("未知模板类型：" + templateType);
            }
        }
    }

    private List<TemplateVO> buildTemplateTreeWithPaths(List<TemplateEntity> templateList) {
        // 1. 转换为 VO 并构建 ID 到 VO 的映射
        List<TemplateVO> allTemplates = templateMapstruct.entityToVO(templateList);
        Map<Long, TemplateVO> templateMap = allTemplates.stream()
                .collect(Collectors.toMap(TemplateVO::getId, Function.identity()));

        // 2. 构建父节点到子节点的映射关系
        Map<Long, List<TemplateVO>> parentChildrenMap = new HashMap<>();
        for (TemplateVO template : allTemplates) {
            Long parentId = template.getParentId();
            parentChildrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(template);
        }

        // 3. 使用队列进行层次遍历，设置 generatorPath
        Queue<TemplateVO> queue = new LinkedList<>();
        for (TemplateVO template : allTemplates) {
            if (template.getParentId() == 0) {
                template.setGeneratorPath(template.getFileName());
                queue.offer(template);
            }
        }

        while (!queue.isEmpty()) {
            TemplateVO current = queue.poll();
            List<TemplateVO> children = parentChildrenMap.getOrDefault(current.getId(), Collections.emptyList());
            for (TemplateVO child : children) {
                child.setGeneratorPath(current.getGeneratorPath() + "/" + child.getFileName());
                queue.offer(child);
            }
        }

        // 4. 筛选叶子节点（无子节点的节点）
        return allTemplates.stream()
                .filter(t -> parentChildrenMap.getOrDefault(t.getId(), Collections.emptyList()).isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 构建树形结构
     */
    public static List<PreviewTemplateVO> buildTree(List<TemplateContentVO> expandTreeList) {
        if (CollUtil.isEmpty(expandTreeList)) {
            return Collections.emptyList();
        }

        Map<String, PreviewTemplateVO> nodeMap = buildNodeMap(expandTreeList);

        //将平铺的数据，按照树形进行组装
        List<PreviewTemplateVO> allTreeList = new ArrayList<>(nodeMap.values());
        return allTreeList.stream()
                //找到层级为0的节点
                .filter(treeVO -> treeVO.getLevel() == 0)
                //添加子节点
                .peek(treeVO -> treeVO.setChildren(getChildren(treeVO, allTreeList)))
                //进行排序
                .sorted(PreviewTemplateVO.TREE_COMPARATOR)
                .toList();
    }

    private static Map<String, PreviewTemplateVO> buildNodeMap(List<TemplateContentVO> expandTreeList) {
        Map<String, PreviewTemplateVO> nodeMap = new HashMap<>();

        //将树形数据，平铺成Map
        for (TemplateContentVO treeVO : expandTreeList) {
            String[] pathParts = treeVO.getFilePath().split("/");
            int pathLength = pathParts.length;
            for (int level = 0; level < pathLength; level++) {
                //当前路径
                String currentPath = String.join("/", Arrays.copyOfRange(pathParts, 0, level + 1));
                if (nodeMap.containsKey(currentPath)) {
                    continue;
                }
                PreviewTemplateVO tempTreeVO = new PreviewTemplateVO();
                //节点名称
                tempTreeVO.setFileName(pathParts[level]);
                //文件路径
                tempTreeVO.setFilePath(currentPath);
                //层级
                tempTreeVO.setLevel(level);
                //内容
                tempTreeVO.setTemplateContent(treeVO.getContent());
                //是否为模板
                if (level == pathLength - 1) {
                    tempTreeVO.setId(treeVO.getTemplateId());
                    tempTreeVO.setEnumId(treeVO.getEnumId());
                    tempTreeVO.setTableId(treeVO.getTableId());
                    tempTreeVO.setTemplateType(treeVO.getTemplateType());
                    tempTreeVO.setTemplateGroupType(treeVO.getTemplateGroupType());
                } else {
                    tempTreeVO.setTemplateType(TemplateTypeEnum.DIRECTORY.getCode());
                }
                nodeMap.put(currentPath, tempTreeVO);
            }
        }
        return nodeMap;
    }

    private static List<PreviewTemplateVO> getChildren(PreviewTemplateVO parent, List<PreviewTemplateVO> treeList) {
        return treeList.stream()
                //判断是否是直接子节点
                .filter(child -> isDirectChild(parent, child))
                //递归添加子节点
                .peek(child -> child.setChildren(getChildren(child, treeList)))
                //进行排序
                .sorted(PreviewTemplateVO.TREE_COMPARATOR)
                .toList();
    }

    /**
     * 判断是否是直接子节点
     */
    private static boolean isDirectChild(PreviewTemplateVO parent, PreviewTemplateVO child) {
        return child.getLevel() == parent.getLevel() + 1
                && child.getFilePath().startsWith(parent.getFilePath() + "/");
    }

    private ResponseEntity<byte[]> buildResponseEntity(String fileName, byte[] data) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpHeaderUtil.createAttachmentDisposition(fileName, CharsetUtil.UTF_8))
                .body(data);
    }

}
