package com.yanggu.code.generator.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.yanggu.code.generator.domain.entity.*;
import com.yanggu.code.generator.domain.model.BaseClassModel;
import com.yanggu.code.generator.domain.model.TableDataModel;
import com.yanggu.code.generator.domain.model.TableFieldModel;
import com.yanggu.code.generator.domain.query.GeneratorTableQuery;
import com.yanggu.code.generator.domain.vo.PreviewVO;
import com.yanggu.code.generator.domain.vo.TreeVO;
import com.yanggu.code.generator.enums.TemplateTypeEnum;
import com.yanggu.code.generator.mapstruct.BaseClassMapstruct;
import com.yanggu.code.generator.mapstruct.TableFieldMapstruct;
import com.yanggu.code.generator.service.*;
import com.yanggu.code.generator.util.TemplateUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.date.DateFormatPool;
import org.dromara.hutool.core.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.ServerException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static cn.hutool.core.date.DatePattern.PURE_DATETIME_PATTERN;

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
    public List<PreviewVO> tablePreview(GeneratorTableQuery tableQuery) {
        Long tableId = tableQuery.getTableId();

        //获取数据模型
        TableDataModel tableDataModel = getDataModel(tableId);

        Long tableTemplateGroupId = tableService.getTableTemplateGroupId(tableId);

        TemplateGroupEntity templateGroup = templateGroupService.getById(tableTemplateGroupId);
        List<TemplateEntity> templateList = templateGroup.getTemplateList();
        List<Long> templateIdList = tableQuery.getTemplateIdList();
        if (CollUtil.isNotEmpty(templateIdList)) {
            templateList = templateList.stream()
                    .filter(template -> templateIdList.contains(template.getId()))
                    .toList();
        }

        return templateList.stream()
                .map(template -> {
                    tableDataModel.setTemplateName(template.getTemplateName());
                    String content;
                    Integer templateType = template.getTemplateType();
                    PreviewVO previewVO = new PreviewVO();
                    if (TemplateTypeEnum.FILE.getCode().equals(templateType)) {
                        content = TemplateUtils.getContent(template.getTemplateContent(), tableDataModel);
                    } else {
                        content = "";
                    }
                    String filePath = TemplateUtils.getContent(template.getGeneratorPath(), tableDataModel);
                    String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                    previewVO.setFilePath(filePath);
                    previewVO.setFileName(fileName);
                    previewVO.setContent(content);
                    previewVO.setTemplateId(template.getId());
                    previewVO.setTableId(tableId);
                    previewVO.setTemplateType(templateType);
                    previewVO.setTemplateGroupType(templateGroup.getType());
                    previewVO.setGeneratorType(tableDataModel.getGeneratorType());
                    return previewVO;
                })
                .toList();
    }

    @Override
    public List<TreeVO> tableTreeData(GeneratorTableQuery tableQuery) {
        Long tableId = tableQuery.getTableId();
        //数据模型
        TableDataModel dataModel = getDataModel(tableId);

        Long templateGroupId = tableService.getTableTemplateGroupId(tableId);

        TemplateGroupEntity templateGroup = templateGroupService.getById(templateGroupId);

        List<TreeVO> treeList = new ArrayList<>();
        //渲染模板并输出
        for (TemplateEntity template : templateGroup.getTemplateList()) {
            dataModel.setTemplateName(template.getTemplateName());

            TreeVO treeVO = new TreeVO();
            //路径
            String path = TemplateUtils.getContent(template.getGeneratorPath(), dataModel);
            treeVO.setFilePath(path);

            //文件名称
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            treeVO.setLabel(fileName);

            //模板ID
            treeVO.setTemplateId(template.getId());
            treeList.add(treeVO);
        }

        return buildTree(treeList);
    }

    @Override
    public void tableDownloadTemplateContent(Long tableId, Long templateId, HttpServletResponse response) throws IOException {
        GeneratorTableQuery tableQuery = new GeneratorTableQuery();
        tableQuery.setTableId(tableId);
        tableQuery.setTemplateIdList(List.of(templateId));
        List<PreviewVO> list = tablePreview(tableQuery);
        PreviewVO preview = list.getFirst();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(preview.getContent().getBytes());
        byte[] data = outputStream.toByteArray();

        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + preview.getFileName());
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IoUtil.write(response.getOutputStream(), false, data);
    }

    @Override
    public void tableDownloadZip(List<Long> tableIds, HttpServletResponse response) throws IOException {
        List<PreviewVO> list = new ArrayList<>();
        tableIds.forEach(tableId -> {
            GeneratorTableQuery generatorTableQuery = new GeneratorTableQuery();
            generatorTableQuery.setTableId(tableId);
            List<PreviewVO> tempList = tablePreview(generatorTableQuery);
            if (CollUtil.isNotEmpty(tempList)) {
                list.addAll(tempList);
            }
        });

        downloadZip2(list, response);
    }

    @Override
    public void tableDownloadLocal(GeneratorTableQuery tableQuery) {
        List<PreviewVO> list = tablePreview(tableQuery);
        downloadLocal(list);
    }

    private void downloadZip2(List<PreviewVO> list, HttpServletResponse response) throws IOException {
        if (CollUtil.isEmpty(list)) {
            return;
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
                throw new ServerException("模板写入失败：" + previewVO.getFilePath(), e);
            }
        }
        IoUtil.close(zip);

        // zip压缩包数据
        byte[] data = outputStream.toByteArray();

        String dateTime = cn.hutool.core.date.DateUtil.format(new Date(), PURE_DATETIME_PATTERN);
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=maku_" + dateTime + ".zip");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IoUtil.write(response.getOutputStream(), false, data);
    }

    private void downloadLocal(List<PreviewVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (PreviewVO previewVO : list) {
            Integer templateType = previewVO.getTemplateType();
            //写入到文件
            if (TemplateTypeEnum.FILE.getCode().equals(templateType)) {
                FileUtil.writeUtf8String(previewVO.getContent(), previewVO.getFilePath());
            } else {
                //生成文件夹
                FileUtil.mkdir(previewVO.getFilePath());
            }
        }
    }

    /**
     * 获取渲染的数据模型
     *
     * @param tableId 表ID
     */
    private TableDataModel getDataModel(Long tableId) {
        // 表信息
        TableEntity table = tableService.getById(tableId);
        List<TableFieldEntity> fieldList = tableFieldService.getByTableId(tableId);
        List<TableFieldModel> fieldModelList = tableFieldMapstruct.toModel(fieldList);

        TableDataModel tableDataModel = new TableDataModel();
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
        tableDataModel.setFormList(formList);
        tableDataModel.setGridList(gridList);
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
            if (ArrayUtil.contains(fields, field.getFieldName())) {
                field.setBaseField(true);
            }
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
                .sorted((tree1, tree2) -> {
                    if (tree1.getIsFile() && !tree2.getIsFile()) {
                        return 1;
                    } else if (!tree1.getIsFile() && tree2.getIsFile()) {
                        return -1;
                    } else {
                        return tree1.getLabel().compareTo(tree2.getLabel());
                    }
                })
                .toList();
    }

    private List<TreeVO> getChildren(TreeVO node, List<TreeVO> treeList) {
        return treeList.stream()
                .filter(treeVO -> treeVO.getFilePath().startsWith(node.getFilePath()))
                .filter(treeVO -> node.getLevel() + 1 == treeVO.getLevel())
                //递归添加子节点
                .peek(treeVO -> treeVO.setChildren(getChildren(treeVO, treeList)))
                //进行排序
                .sorted((tree1, tree2) -> {
                    if (tree1.getIsFile() && !tree2.getIsFile()) {
                        return 1;
                    } else if (!tree1.getIsFile() && tree2.getIsFile()) {
                        return -1;
                    } else {
                        return tree1.getLabel().compareTo(tree2.getLabel());
                    }
                })
                .toList();
    }

}
