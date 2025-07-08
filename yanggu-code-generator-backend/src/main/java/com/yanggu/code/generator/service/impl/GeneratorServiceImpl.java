package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.bo.DataSourceBO;
import com.yanggu.code.generator.domain.entity.*;
import com.yanggu.code.generator.domain.model.*;
import com.yanggu.code.generator.domain.query.GeneratorEnumQuery;
import com.yanggu.code.generator.domain.query.GeneratorProjectQuery;
import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewDataVO;
import com.yanggu.code.generator.domain.vo.TemplateContentVO;
import com.yanggu.code.generator.domain.vo.TreeVO;
import com.yanggu.code.generator.enums.TemplateGroupTypeEnum;
import com.yanggu.code.generator.mapstruct.BaseClassMapstruct;
import com.yanggu.code.generator.mapstruct.TableFieldMapstruct;
import com.yanggu.code.generator.service.*;
import com.yanggu.code.generator.util.NameUtil;
import com.yanggu.code.generator.util.TemplateUtil;
import com.yanggu.code.generator.util.TreeUtil;
import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.date.DateFormatPool;
import org.dromara.hutool.core.date.DateUtil;
import org.dromara.hutool.core.io.IoUtil;
import org.dromara.hutool.core.io.file.FileUtil;
import org.dromara.hutool.core.text.NamingCase;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.util.BooleanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.yanggu.code.generator.enums.TemplateTypeEnum.FILE;
import static org.dromara.hutool.core.date.DateFormatPool.PURE_DATETIME_PATTERN;

/**
 * 代码生成服务实现类
 */
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
    private BaseClassMapstruct baseClassMapstruct;

    @Autowired
    private EnumService enumService;

    @Override
    public PreviewDataVO projectPreview(Long projectId) throws Exception {
        GeneratorProjectQuery projectQuery = new GeneratorProjectQuery();
        projectQuery.setProjectId(projectId);
        List<TemplateContentVO> allList = buildProjectPreviewList(projectQuery);

        return buildPreviewData(allList);
    }

    @Override
    public void projectDownloadLocal(GeneratorProjectQuery projectQuery) throws Exception {
        List<TemplateContentVO> list = buildProjectPreviewList(projectQuery);
        downloadLocal(list);
    }

    @Override
    public ResponseEntity<byte[]> projectDownloadZip(GeneratorProjectQuery projectQuery) throws Exception {
        List<TemplateContentVO> list = buildProjectPreviewList(projectQuery);
        return downloadZip(list);
    }

    @Override
    public ResponseEntity<byte[]> projectDownloadSingle(Integer templateGroupType, Long id, Long templateId) throws Exception {
        if (TemplateGroupTypeEnum.PROJECT.getCode().equals(templateGroupType)) {
            ProjectEntity project = projectService.getById(id);
            TemplateContentVO preview = projectPreview(project, List.of(templateId)).getFirst();
            return buildResponseEntity(preview.getFileName(), preview.getContent().getBytes());
        } else if (TemplateGroupTypeEnum.TABLE.getCode().equals(templateGroupType)) {
            return tableDownloadSingle(id, templateId);
        } else if (TemplateGroupTypeEnum.ENUM.getCode().equals(templateGroupType)) {
            return enumDownloadSingle(id, templateId);
        } else {
            throw new BusinessException("模板组类型异常: " + templateGroupType);
        }
    }

    @Override
    public PreviewDataVO tablePreview(Long tableId) {
        GeneratorTableQuery tableQuery = new GeneratorTableQuery();
        tableQuery.setTableId(tableId);
        List<TemplateContentVO> allList = tablePreview(tableQuery);

        return buildPreviewData(allList);
    }

    @Override
    public void tableDownloadLocal(GeneratorTableQuery tableQuery) {
        List<TemplateContentVO> list = getTablePreviewData(tableQuery);
        downloadLocal(list);
    }

    @Override
    public ResponseEntity<byte[]> tableDownloadZip(GeneratorTableQuery tableQuery) {
        List<TemplateContentVO> list = getTablePreviewData(tableQuery);
        return downloadZip(list);
    }

    @Override
    public ResponseEntity<byte[]> tableDownloadSingle(Long tableId, Long templateId) {
        GeneratorTableQuery tableQuery = new GeneratorTableQuery();
        tableQuery.setTableId(tableId);
        tableQuery.setTemplateIdList(List.of(templateId));
        List<TemplateContentVO> list = tablePreview(tableQuery);
        TemplateContentVO preview = list.getFirst();

        return buildResponseEntity(preview.getFileName(), preview.getContent().getBytes());
    }

    @Override
    public PreviewDataVO enumPreview(Long enumId) {
        GeneratorEnumQuery enumQuery = new GeneratorEnumQuery();
        enumQuery.setEnumId(enumId);
        List<TemplateContentVO> list = enumPreview(enumQuery);
        return buildPreviewData(list);
    }

    @Override
    public void enumDownloadLocal(GeneratorEnumQuery enumQuery) {
        List<TemplateContentVO> enumPreviewData = getEnumPreviewData(enumQuery);
        downloadLocal(enumPreviewData);
    }

    @Override
    public ResponseEntity<byte[]> enumDownloadZip(GeneratorEnumQuery enumQuery) {
        List<TemplateContentVO> enumPreviewData = getEnumPreviewData(enumQuery);
        return downloadZip(enumPreviewData);
    }

    @Override
    public ResponseEntity<byte[]> enumDownloadSingle(Long enumId, Long templateId) {
        GeneratorEnumQuery enumQuery = new GeneratorEnumQuery();
        enumQuery.setEnumId(enumId);
        enumQuery.setTemplateIdList(List.of(templateId));
        List<TemplateContentVO> list = enumPreview(enumQuery);
        TemplateContentVO preview = list.getFirst();
        return buildResponseEntity(preview.getFileName(), preview.getContent().getBytes());
    }

    private PreviewDataVO buildPreviewData(List<TemplateContentVO> allList) {
        PreviewDataVO previewData = new PreviewDataVO();

        //构建模板内容列表
        List<TemplateContentVO> templateContentList = allList.stream()
                .filter(templateContentVO -> templateContentVO.getTemplateType().equals(FILE.getCode()))
                .toList();

        previewData.setTemplateContentList(templateContentList);

        //构建树形列表
        List<TreeVO> treeList = buildTree(allList);
        previewData.setTreeList(treeList);

        //重新排序模板列表
        sortTemplateContentList(previewData);
        return previewData;
    }

    private void sortTemplateContentList(PreviewDataVO previewData) {
        List<TemplateContentVO> templateContentList = previewData.getTemplateContentList();
        List<TreeVO> treeList = previewData.getTreeList();

        //生成深度优先遍历的路径顺序
        List<String> dfsOrder = TreeUtil.getDfsOrder(treeList);

        //根据路径顺序对 templateContentList 排序
        List<TemplateContentVO> newList = templateContentList.stream()
                .sorted(Comparator.comparingInt(t -> dfsOrder.indexOf(t.getFilePath())))
                .toList();
        previewData.setTemplateContentList(newList);
    }

    private List<TemplateContentVO> tablePreview(GeneratorTableQuery tableQuery) {
        Long tableId = tableQuery.getTableId();

        // 表信息
        TableEntity table = tableService.getById(tableId);

        ProjectEntity project = projectService.getById(table.getProjectId());

        //获取数据模型
        TableModel tableModel = buildTableDataModel(table, project);

        Long tableTemplateGroupId = tableService.getTableTemplateGroupId(tableId);

        TemplateGroupEntity templateGroup = templateGroupService.getById(tableTemplateGroupId);
        List<TemplateEntity> templateList = templateGroup.getTemplateList();
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
        Long enumId = enumQuery.getEnumId();
        EnumEntity enumEntity = enumService.getById(enumId);
        ProjectEntity project = projectService.getById(enumEntity.getProjectId());
        List<Long> templateIdList = enumQuery.getTemplateIdList();

        //查询项目对应的枚举模板
        TemplateGroupEntity templateGroup = templateGroupService.getById(project.getEnumTemplateGroupId());
        List<TemplateEntity> templateList = templateGroup.getTemplateList();
        List<TemplateContentVO> list = new ArrayList<>();
        for (TemplateEntity templateEntity : templateList) {
            if (CollUtil.isEmpty(templateIdList) || templateIdList.contains(templateEntity.getId())) {
                EnumDataModel enumDataModel = buildEnumDataModel(enumEntity, project);
                TemplateContentVO templateContentVO = getTemplateContentVO(templateGroup, templateEntity, enumDataModel);
                templateContentVO.setEnumId(enumId);
                list.add(templateContentVO);
            }
        }
        return list;
    }

    private List<TemplateContentVO> buildProjectPreviewList(GeneratorProjectQuery projectQuery) throws Exception {
        Long projectId = projectQuery.getProjectId();
        //查询项目
        ProjectEntity project = projectService.getById(projectId);

        //查询该项目下的表
        List<TableEntity> tableList = tableService.list(Wrappers.<TableEntity>lambdaQuery().eq(TableEntity::getProjectId, projectId));
        List<Long> tableIdList = projectQuery.getTableIdList();
        if (CollUtil.isNotEmpty(tableIdList)) {
            tableList = tableList.stream()
                    .filter(table -> tableIdList.contains(table.getId()))
                    .toList();
        }

        List<TemplateContentVO> allPreviewList = new ArrayList<>();

        //获取表预览数据
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

    private List<TemplateContentVO> getTablePreviewData(GeneratorTableQuery tableQuery) {
        return tableQuery.getTableIdList().stream()
                .flatMap(tempTableId -> {
                    GeneratorTableQuery query = new GeneratorTableQuery();
                    query.setTableId(tempTableId);
                    query.setTemplateIdList(tableQuery.getTemplateIdList());
                    return tablePreview(query).stream();
                })
                .toList();
    }

    private List<TemplateContentVO> getEnumPreviewData(GeneratorEnumQuery enumQuery) {
        Long enumId = enumQuery.getEnumId();
        List<Long> enumIdList = enumQuery.getEnumIdList();
        List<TemplateContentVO> list = new ArrayList<>();
        if (enumId != null) {
            list.addAll(enumPreview(enumQuery));
        } else {
            enumIdList.forEach(id -> {
                GeneratorEnumQuery tempEnumQuery = new GeneratorEnumQuery();
                tempEnumQuery.setEnumId(id);
                list.addAll(enumPreview(tempEnumQuery));
            });
        }
        return list;
    }

    private ResponseEntity<byte[]> downloadZip(List<TemplateContentVO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new BusinessException("待写入数据为空");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (TemplateContentVO templateContentVO : list) {
            try {
                // 添加到zip
                zip.putNextEntry(new ZipEntry(templateContentVO.getFilePath()));
                IoUtil.writeUtf8(zip, false, templateContentVO.getContent());
                zip.flush();
                zip.closeEntry();
            } catch (IOException e) {
                throw new BusinessException("模板写入失败：" + templateContentVO.getFilePath(), e);
            }
        }
        IoUtil.closeQuietly(zip);

        // zip压缩包数据
        byte[] data = outputStream.toByteArray();

        String dateTime = DateUtil.format(new Date(), PURE_DATETIME_PATTERN);
        String fileName = "code_generator_" + dateTime + ".zip";
        return buildResponseEntity(fileName, data);
    }

    private void downloadLocal(List<TemplateContentVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (TemplateContentVO templateContentVO : list) {
            Integer templateType = templateContentVO.getTemplateType();
            //写入到文件
            if (FILE.getCode().equals(templateType)) {
                FileUtil.writeUtf8String(templateContentVO.getContent(), templateContentVO.getFilePath());
            } else {
                //生成文件夹
                FileUtil.mkdir(templateContentVO.getFilePath());
            }
        }
    }

    private List<TemplateContentVO> tableListPreview(List<TableEntity> tableList, List<Long> tableTemplateIdList) {
        List<TemplateContentVO> tablePreviewList = new ArrayList<>();
        for (TableEntity tableEntity : tableList) {
            GeneratorTableQuery query = new GeneratorTableQuery();
            query.setTableId(tableEntity.getId());
            query.setTemplateIdList(tableTemplateIdList);
            List<TemplateContentVO> tempList = tablePreview(query);
            tablePreviewList.addAll(tempList);
        }
        return tablePreviewList;
    }

    private List<TemplateContentVO> enumListPreview(ProjectEntity project, List<Long> enmumIdList, List<Long> enumTemplateIdList) {
        List<EnumDataModel> enumDataModelList = new ArrayList<>();
        List<EnumEntity> enumList = enumService.enumList(project.getId());
        enumList = enumList.stream()
                .filter(enumEntity -> CollUtil.isEmpty(enmumIdList) || enmumIdList.contains(enumEntity.getId()))
                .toList();
        for (EnumEntity enumEntity : enumList) {
            EnumDataModel dataModel = buildEnumDataModel(enumEntity, project);
            enumDataModelList.add(dataModel);
        }

        //查询项目对应的枚举模板
        TemplateGroupEntity templateGroup = templateGroupService.getById(project.getEnumTemplateGroupId());
        List<TemplateEntity> templateList = templateGroup.getTemplateList().stream()
                .filter(template -> CollUtil.isEmpty(enumTemplateIdList) || enumTemplateIdList.contains(template.getId()))
                .toList();
        return enumDataModelList.stream()
                .flatMap(enumDataModel -> templateList.stream()
                        .map(template -> {
                            TemplateContentVO templateContentVO = getTemplateContentVO(templateGroup, template, enumDataModel);
                            templateContentVO.setEnumId(enumDataModel.getEnumId());
                            return templateContentVO;
                        })
                )
                .toList();
    }

    private List<TemplateContentVO> projectPreview(ProjectEntity project, List<Long> templateIdList) throws Exception {
        //获取数据源信息
        DataSourceBO dataSource = datasourceService.get(project.getDatasourceId());
        //获取项目模板组信息
        Long projectTemplateGroupId = project.getProjectTemplateGroupId();
        TemplateGroupEntity templateGroup = templateGroupService.getById(projectTemplateGroupId);
        List<TemplateEntity> projecTemplateList = templateGroup.getTemplateList();

        ProjectModel projectModel = buildProjectDataModel(project, dataSource);
        return projecTemplateList.stream()
                .filter(template -> CollUtil.isEmpty(templateIdList) || templateIdList.contains(template.getId()))
                .map(template -> getTemplateContentVO(templateGroup, template, projectModel))
                .toList();
    }

    private TemplateContentVO getTemplateContentVO(TemplateGroupEntity templateGroup, TemplateEntity template, Object dataModel) {
        Integer templateType = template.getTemplateType();
        //内容
        String fileContent;
        TemplateContentVO templateContentVO = new TemplateContentVO();
        String templateName = template.getTemplateName();
        if (FILE.getCode().equals(templateType)) {
            fileContent = TemplateUtil.renderTemplate(templateName, template.getTemplateContent(), dataModel);
        } else {
            fileContent = "";
        }
        //路径
        String filePath = TemplateUtil.renderTemplate(templateName, template.getGeneratorPath(), dataModel);
        //文件名
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        templateContentVO.setContent(fileContent);
        templateContentVO.setFileName(fileName);
        templateContentVO.setFilePath(filePath);
        templateContentVO.setTemplateId(template.getId());
        templateContentVO.setTemplateGroupType(templateGroup.getType());
        templateContentVO.setTemplateType(templateType);
        return templateContentVO;
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
        List<EnumDataModel> enumDataModelList = new ArrayList<>();
        projectModel.setEnumDataModelList(enumDataModelList);

        List<EnumEntity> enumList = enumService.enumList(project.getId());
        for (EnumEntity enumEntity : enumList) {
            EnumDataModel enumDataModel = buildEnumDataModel(enumEntity, project);
            enumDataModelList.add(enumDataModel);
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

    private EnumDataModel buildEnumDataModel(EnumEntity enumEntity, ProjectEntity project) {
        EnumDataModel enumDataModel = new EnumDataModel();

        enumDataModel.setProjectNameDot(NameUtil.toDot(project.getProjectName()));
        enumDataModel.setProjectNameSlash(NameUtil.toSlash(project.getProjectName()));
        enumDataModel.setProjectPackage(project.getProjectPackage());
        enumDataModel.setProjectPackageSlash(StrUtil.replace(project.getProjectPackage(), ".", "/"));
        enumDataModel.setBackendPath(project.getBackendPath());
        enumDataModel.setFrontendPath(project.getFrontendPath());

        enumDataModel.setEnumId(enumEntity.getId());
        enumDataModel.setEnumName(enumEntity.getEnumName());
        enumDataModel.setEnumNamePascal(NameUtil.toPascal(enumEntity.getEnumName()));
        enumDataModel.setEnumNameAllUpper(NameUtil.toAllUpperCase(enumEntity.getEnumName()));
        enumDataModel.setEnumDesc(enumEntity.getEnumDesc());
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
        enumDataModel.setEnumItemList(list);
        return enumDataModel;
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

    private List<TreeVO> buildTree(List<TemplateContentVO> templateContentList) {
        List<TreeVO> treeList = templateContentList.stream()
                .map(templateContentVO -> {
                    TreeVO treeVO = new TreeVO();
                    treeVO.setFilePath(templateContentVO.getFilePath());
                    treeVO.setLabel(templateContentVO.getFileName());
                    treeVO.setTemplateId(templateContentVO.getTemplateId());
                    treeVO.setTemplateType(templateContentVO.getTemplateType());
                    return treeVO;
                })
                .toList();

        return TreeUtil.buildTree(treeList);
    }

    private ResponseEntity<byte[]> buildResponseEntity(String fileName, byte[] data) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(data);
    }

}
