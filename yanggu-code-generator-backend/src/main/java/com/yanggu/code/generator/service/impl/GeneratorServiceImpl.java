package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.bo.DataSourceBO;
import com.yanggu.code.generator.domain.entity.*;
import com.yanggu.code.generator.domain.model.BaseClassModel;
import com.yanggu.code.generator.domain.model.ProjectDataModel;
import com.yanggu.code.generator.domain.model.TableDataModel;
import com.yanggu.code.generator.domain.model.TableFieldModel;
import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewVO;
import com.yanggu.code.generator.domain.vo.TreeVO;
import com.yanggu.code.generator.mapstruct.BaseClassMapstruct;
import com.yanggu.code.generator.mapstruct.TableFieldMapstruct;
import com.yanggu.code.generator.service.*;
import com.yanggu.code.generator.util.TemplateUtils;
import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.date.DateFormatPool;
import org.dromara.hutool.core.date.DateUtil;
import org.dromara.hutool.core.io.IoUtil;
import org.dromara.hutool.core.io.file.FileUtil;
import org.dromara.hutool.core.text.NamingCase;
import org.dromara.hutool.core.text.StrUtil;
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

import static com.yanggu.code.generator.domain.vo.TreeVO.TREE_COMPARATOR;
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

    @Override
    public List<PreviewVO> tablePreview2(GeneratorTableQuery tableQuery) {
        return tablePreview(tableQuery).stream()
                //过滤出文件
                .filter(previewVO -> previewVO.getTemplateType().equals(FILE.getCode()))
                .toList();
    }

    @Override
    public List<PreviewVO> tablePreview(GeneratorTableQuery tableQuery) {
        Long tableId = tableQuery.getTableId();

        //获取数据模型
        TableDataModel tableDataModel = buildTableDataModel(tableId);

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
                    tableDataModel.setTemplateName(template.getTemplateName());
                    PreviewVO previewVO = new PreviewVO();

                    String content = "";
                    if (template.getTemplateType().equals(FILE.getCode())) {
                        content = TemplateUtils.getContent(template.getTemplateContent(), tableDataModel);
                    }
                    //文件路径
                    String filePath = TemplateUtils.getContent(template.getGeneratorPath(), tableDataModel);
                    //文件名称
                    String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

                    previewVO.setTableId(tableId);
                    previewVO.setTemplateId(template.getId());
                    previewVO.setTemplateGroupType(templateGroup.getType());
                    previewVO.setTemplateType(template.getTemplateType());
                    previewVO.setGeneratorType(tableDataModel.getGeneratorType());
                    previewVO.setFileName(fileName);
                    previewVO.setContent(content);
                    previewVO.setFilePath(filePath);
                    return previewVO;
                })
                .toList();
    }

    @Override
    public List<TreeVO> tableTreeData(GeneratorTableQuery tableQuery) {
        Long tableId = tableQuery.getTableId();
        //数据模型
        TableDataModel dataModel = buildTableDataModel(tableId);

        Long templateGroupId = tableService.getTableTemplateGroupId(tableId);

        TemplateGroupEntity templateGroup = templateGroupService.getById(templateGroupId);

        List<TreeVO> treeList = new ArrayList<>();
        //渲染模板并输出
        for (TemplateEntity template : templateGroup.getTemplateList()) {

            TreeVO treeVO = new TreeVO();
            //路径
            String filePath = TemplateUtils.getContent(template.getGeneratorPath(), dataModel);
            treeVO.setFilePath(filePath);

            //文件名称
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            treeVO.setLabel(fileName);

            //模板ID
            treeVO.setTemplateId(template.getId());
            treeList.add(treeVO);
        }

        return buildTree(treeList);
    }

    @Override
    public ResponseEntity<byte[]> tableDownloadTemplateContent(Long tableId, Long templateId) throws IOException {
        GeneratorTableQuery tableQuery = new GeneratorTableQuery();
        tableQuery.setTableId(tableId);
        tableQuery.setTemplateIdList(List.of(templateId));
        List<PreviewVO> list = tablePreview(tableQuery);
        PreviewVO preview = list.getFirst();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(preview.getContent().getBytes());
        byte[] data = outputStream.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + preview.getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @Override
    public ResponseEntity<byte[]> tableBatchDownloadZip(List<Long> tableIds) {
        List<PreviewVO> list = new ArrayList<>();
        tableIds.forEach(tableId -> {
            GeneratorTableQuery generatorTableQuery = new GeneratorTableQuery();
            generatorTableQuery.setTableId(tableId);
            List<PreviewVO> tempList = tablePreview(generatorTableQuery);
            if (CollUtil.isNotEmpty(tempList)) {
                list.addAll(tempList);
            }
        });

        return downloadZip(list);
    }

    @Override
    public void tableDownloadLocal(GeneratorTableQuery tableQuery) {
        List<PreviewVO> list = getPreviewData(tableQuery);
        downloadLocal(list);
    }

    @Override
    public ResponseEntity<byte[]> tableDownloadZip(GeneratorTableQuery tableQuery) {
        List<PreviewVO> list = getPreviewData(tableQuery);
        return downloadZip(list);
    }

    @Override
    public List<PreviewVO> buildProjectPreviewList2(Long projectId) throws Exception {
        return buildProjectPreviewList(projectId).stream()
                .filter(previewVO -> previewVO.getTemplateType().equals(FILE.getCode()))
                .toList();
    }

    @Override
    public List<PreviewVO> buildProjectPreviewList(Long projectId) throws Exception {
        //查询项目
        ProjectEntity project = projectService.getById(projectId);

        //查询该项目下的表
        List<TableEntity> tableList = tableService.list(Wrappers.<TableEntity>lambdaQuery().eq(TableEntity::getProjectId, projectId));

        List<PreviewVO> allPreviewList = new ArrayList<>();

        //获取表预览数据
        List<PreviewVO> tablePreviewList = tableListPreview(tableList);
        allPreviewList.addAll(tablePreviewList);

        //获取项目预览数据
        List<PreviewVO> projectPreviewList = projectPreview(project);
        allPreviewList.addAll(projectPreviewList);

        return allPreviewList;
    }

    @Override
    public List<TreeVO> treeData(Long projectId) throws Exception {
        List<PreviewVO> previewList = buildProjectPreviewList(projectId);

        List<TreeVO> treeList = new ArrayList<>();
        // 渲染模板并输出
        for (PreviewVO previewVO : previewList) {
            TreeVO treeVO = new TreeVO();
            treeVO.setFilePath(previewVO.getFilePath());
            treeVO.setLabel(previewVO.getFileName());
            treeVO.setTemplateId(previewVO.getTemplateId());
            treeList.add(treeVO);
        }

        return buildTree(treeList);
    }

    private List<PreviewVO> getPreviewData(GeneratorTableQuery tableQuery) {
        Long tableId = tableQuery.getTableId();
        List<Long> tableIdList = tableQuery.getTableIdList();
        List<PreviewVO> list = new ArrayList<>();
        if (tableId != null) {
            list.addAll(tablePreview(tableQuery));
        } else {
            tableIdList.forEach(id -> {
                GeneratorTableQuery generatorTableQuery = new GeneratorTableQuery();
                generatorTableQuery.setTableId(id);
                list.addAll(tablePreview(generatorTableQuery));
            });
        }
        return list;
    }

    private ResponseEntity<byte[]> downloadZip(List<PreviewVO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new BusinessException("暂不支持");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (PreviewVO previewVO : list) {
            try {
                // 添加到zip
                zip.putNextEntry(new ZipEntry(previewVO.getFilePath()));
                IoUtil.writeUtf8(zip, false, previewVO.getContent());
                zip.flush();
                zip.closeEntry();
            } catch (IOException e) {
                throw new BusinessException("模板写入失败：" + previewVO.getFilePath(), e);
            }
        }
        IoUtil.closeQuietly(zip);

        // zip压缩包数据
        byte[] data = outputStream.toByteArray();

        String dateTime = DateUtil.format(new Date(), PURE_DATETIME_PATTERN);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=maku_" + dateTime + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    private void downloadLocal(List<PreviewVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (PreviewVO previewVO : list) {
            Integer templateType = previewVO.getTemplateType();
            //写入到文件
            if (FILE.getCode().equals(templateType)) {
                FileUtil.writeUtf8String(previewVO.getContent(), previewVO.getFilePath());
            } else {
                //生成文件夹
                FileUtil.mkdir(previewVO.getFilePath());
            }
        }
    }

    private List<PreviewVO> tableListPreview(List<TableEntity> tableList) {
        List<PreviewVO> tablePreviewList = new ArrayList<>();
        for (TableEntity tableEntity : tableList) {
            GeneratorTableQuery query = new GeneratorTableQuery();
            query.setTableId(tableEntity.getId());
            List<PreviewVO> tempList = tablePreview(query);
            tablePreviewList.addAll(tempList);
        }
        return tablePreviewList;
    }

    private List<PreviewVO> projectPreview(ProjectEntity project) throws Exception {
        //获取数据源信息
        DataSourceBO dataSource = datasourceService.get(project.getDatasourceId());
        //获取项目模板组信息
        Long projectTemplateGroupId = project.getProjectTemplateGroupId();
        TemplateGroupEntity templateGroup = templateGroupService.getById(projectTemplateGroupId);
        List<TemplateEntity> projecTemplateList = templateGroup.getTemplateList();

        List<PreviewVO> previewList = new ArrayList<>();
        for (TemplateEntity template : projecTemplateList) {
            ProjectDataModel projectDataModel = buildProjectDataModel(project, dataSource);
            projectDataModel.setTemplateName(template.getTemplateName());
            Integer templateType = template.getTemplateType();
            //内容
            String fileContent;
            PreviewVO previewVO = new PreviewVO();
            if (FILE.getCode().equals(templateType)) {
                fileContent = TemplateUtils.getContent(template.getTemplateContent(), projectDataModel);
            } else {
                fileContent = "";
            }
            //路径
            String filePath = TemplateUtils.getContent(template.getGeneratorPath(), projectDataModel);
            //文件名
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            previewVO.setContent(fileContent);
            previewVO.setFileName(fileName);
            previewVO.setFilePath(filePath);
            previewVO.setTemplateId(template.getId());
            previewVO.setTemplateGroupType(templateGroup.getType());
            previewVO.setTemplateType(templateType);
            previewList.add(previewVO);
        }
        return previewList;
    }

    /**
     * 构建项目渲染数据
     */
    private ProjectDataModel buildProjectDataModel(ProjectEntity project, DataSourceBO dataSource) {
        //项目数据
        ProjectDataModel projectDataModel = new ProjectDataModel();
        projectDataModel.setProjectName(project.getProjectName());
        projectDataModel.setProjectNameUnderline(StrUtil.replace(project.getProjectName(), "-", "_"));
        projectDataModel.setProjectNamePascal(NamingCase.toPascalCase(projectDataModel.getProjectNameUnderline()));
        projectDataModel.setProjectPackage(project.getProjectPackage());
        projectDataModel.setProjectPackageSlash(projectDataModel.getProjectPackage().replace(".", "/"));
        projectDataModel.setProjectVersion(project.getProjectVersion());
        projectDataModel.setBackendPath(project.getBackendPath());
        projectDataModel.setFrontendPath(project.getFrontendPath());
        projectDataModel.setProjectDesc(project.getProjectDesc());

        //数据源数据
        projectDataModel.setDatabaseDriverClassName(dataSource.getDbType().getDriverClass());
        projectDataModel.setDataBaseUrl(dataSource.getConnUrl());
        projectDataModel.setDataBaseUsername(dataSource.getUsername());
        projectDataModel.setDataBasePassword(dataSource.getPassword());

        return projectDataModel;
    }

    /**
     * 构建表渲染的数据模型
     *
     * @param tableId 表ID
     */
    private TableDataModel buildTableDataModel(Long tableId) {
        // 表信息
        TableEntity table = tableService.getById(tableId);
        List<TableFieldEntity> fieldList = tableFieldService.getByTableId(tableId);
        List<TableFieldModel> fieldModelList = tableFieldMapstruct.toModel(fieldList);

        TableDataModel tableDataModel = new TableDataModel();
        fieldModelList.sort(Comparator.comparing(TableFieldModel::getFieldSort));
        tableDataModel.setFieldList(fieldModelList);

        ProjectEntity project = projectService.getById(table.getProjectId());

        // 获取数据库类型
        String dbType = datasourceService.getDatabaseProductName(project.getDatasourceId());
        tableDataModel.setDbType(dbType);

        //项目信息
        tableDataModel.setProjectId(project.getId());
        tableDataModel.setProjectName(project.getProjectName());
        tableDataModel.setProjectNameUnderline(StrUtil.replace(project.getProjectName(), "-", "_"));
        tableDataModel.setProjectNamePascal(NamingCase.toPascalCase(tableDataModel.getProjectNameUnderline()));
        tableDataModel.setProjectPackage(project.getProjectPackage());
        tableDataModel.setProjectPackageSlash(project.getProjectPackage().replace(".", "/"));
        tableDataModel.setVersion(table.getVersion());

        //
        tableDataModel.setFunctionName(table.getFunctionName());
        tableDataModel.setFunctionNamePascal(StrUtil.upperFirst(table.getFunctionName()));
        tableDataModel.setFunctionNameKebabCase(NamingCase.toKebabCase(table.getFunctionName()));
        tableDataModel.setFormLayout(table.getFormLayout());

        //开发者信息
        tableDataModel.setAuthor(table.getAuthor());
        tableDataModel.setDatetime(DateUtil.format(new Date(), DateFormatPool.NORM_DATETIME_PATTERN));
        tableDataModel.setDate(DateUtil.format(new Date(), DateFormatPool.NORM_DATE_PATTERN));

        //设置字段分类
        setFieldTypeList(tableDataModel);

        //设置基类信息
        setBaseClass(tableDataModel);

        // 导入的包列表
        Set<String> importList = fieldTypeService.getPackageByTableId(table.getId());
        tableDataModel.setImportList(importList);

        //表信息
        tableDataModel.setTableName(table.getTableName());
        tableDataModel.setDatabaseName(table.getDatabaseName());
        tableDataModel.setTableComment(table.getTableComment());
        tableDataModel.setClassName(StrUtil.lowerFirst(table.getClassName()));
        tableDataModel.setClassNameUpper(table.getClassName());

        //前后端生成路径
        tableDataModel.setBackendPath(project.getBackendPath());
        tableDataModel.setFrontendPath(project.getFrontendPath());

        //生成方式
        tableDataModel.setGeneratorType(project.getGeneratorType());
        return tableDataModel;
    }

    /**
     * 设置字段分类信息
     *
     * @param tableDataModel 数据模型
     */
    private void setFieldTypeList(TableDataModel tableDataModel) {
        // 主键列表 (支持多主键)
        List<TableFieldModel> primaryList = new ArrayList<>();
        // 表单列表
        List<TableFieldModel> formList = new ArrayList<>();
        // 网格列表
        List<TableFieldModel> gridList = new ArrayList<>();
        // 查询列表
        List<TableFieldModel> queryList = new ArrayList<>();

        for (TableFieldModel field : tableDataModel.getFieldList()) {
            if (field.getPrimaryPk()) {
                primaryList.add(field);
            }
            if (field.getFormItem()) {
                formList.add(field);
            }
            if (field.getGridItem()) {
                gridList.add(field);
            }
            if (field.getQueryItem()) {
                queryList.add(field);
            }
        }
        tableDataModel.setPrimaryList(primaryList);
        formList.sort(Comparator.comparingInt(TableFieldModel::getFormFieldSort));
        tableDataModel.setFormList(formList);
        gridList.sort(Comparator.comparingInt(TableFieldModel::getGridFieldSort));
        tableDataModel.setGridList(gridList);
        queryList.sort(Comparator.comparingInt(TableFieldModel::getQueryFieldSort));
        tableDataModel.setQueryList(queryList);
    }

    /**
     * 设置基类信息
     *
     * @param tableDataModel 数据模型
     */
    private void setBaseClass(TableDataModel tableDataModel) {
        Long projectId = tableDataModel.getProjectId();
        ProjectEntity project = projectService.getById(projectId);

        // 基类
        BaseClassEntity baseClass = baseClassService.getById(project.getBaseClassId());
        if (baseClass == null) {
            return;
        }

        BaseClassModel baseClassModel = baseClassMapstruct.toModel(baseClass);
        tableDataModel.setBaseClass(baseClassModel);

        // 基类字段
        String[] fields = baseClassModel.getFields().split(",");

        // 标注为基类字段
        for (TableFieldModel field : tableDataModel.getFieldList()) {
            field.setBaseField(ArrayUtil.contains(fields, field.getFieldName()));
        }
    }

    private List<TreeVO> buildTree(List<TreeVO> treeList) {
        Map<String, TreeVO> nodeMap = new HashMap<>();

        for (TreeVO treeVO : treeList) {
            String[] pathParts = treeVO.getFilePath().split("/");
            for (int i = 0; i < pathParts.length; i++) {
                StringBuilder pathBuilder = new StringBuilder();
                for (int j = 0; j <= i; j++) {
                    pathBuilder.append(pathParts[j]);
                    if (j < i) {
                        pathBuilder.append("/");
                    }
                }
                String path = pathBuilder.toString();
                TreeVO tempTreeVO = nodeMap.get(path);
                if (tempTreeVO == null) {
                    tempTreeVO = new TreeVO();
                    tempTreeVO.setLabel(pathParts[i]);
                    tempTreeVO.setFilePath(path);
                    tempTreeVO.setLevel(i);
                    if (i == pathParts.length - 1) {
                        tempTreeVO.setTemplateId(treeVO.getTemplateId());
                        tempTreeVO.setIsFile(true);
                    } else {
                        tempTreeVO.setIsFile(false);
                    }
                }
                nodeMap.put(path, tempTreeVO);
            }
        }

        List<TreeVO> values = new ArrayList<>(nodeMap.values());
        return values.stream()
                //找到层级为0的节点
                .filter(treeVO -> treeVO.getLevel() == 0)
                //添加子节点
                .peek(treeVO -> treeVO.setChildren(getChildren(treeVO, values)))
                //进行排序
                .sorted(TREE_COMPARATOR)
                .toList();
    }

    private List<TreeVO> getChildren(TreeVO node, List<TreeVO> treeList) {
        return treeList.stream()
                .filter(treeVO -> treeVO.getFilePath().startsWith(node.getFilePath()))
                .filter(treeVO -> node.getLevel() + 1 == treeVO.getLevel())
                //递归添加子节点
                .peek(treeVO -> treeVO.setChildren(getChildren(treeVO, treeList)))
                //进行排序
                .sorted(TREE_COMPARATOR)
                .toList();
    }

}
