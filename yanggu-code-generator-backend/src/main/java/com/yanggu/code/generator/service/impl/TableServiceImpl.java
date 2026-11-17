package com.yanggu.code.generator.service.impl;

import cn.hutool.v7.core.collection.CollUtil;
import cn.hutool.v7.core.text.NamingCase;
import cn.hutool.v7.core.text.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.bo.DataSourceBO;
import com.yanggu.code.generator.domain.dto.TableDTO;
import com.yanggu.code.generator.domain.dto.TableImportDTO;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.query.TableQuery;
import com.yanggu.code.generator.domain.vo.TableGenerateCheckVO;
import com.yanggu.code.generator.domain.vo.TableVO;
import com.yanggu.code.generator.enums.FormLayoutEnum;
import com.yanggu.code.generator.mapper.TableMapper;
import com.yanggu.code.generator.mapstruct.TableMapstruct;
import com.yanggu.code.generator.service.*;
import com.yanggu.code.generator.util.DbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.rmi.ServerException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 表Service实现类
 */
@Service
public class TableServiceImpl extends ServiceImpl<TableMapper, TableEntity> implements TableService {

    @Autowired
    private TableMapper tableMapper;

    @Autowired
    private TableMapstruct tableMapstruct;

    @Autowired
    private DatasourceService datasourceService;

    @Autowired
    private TableFieldService tableFieldService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TemplateGroupPropertyDataService templateGroupPropertyDataService;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(TableDTO dto) {
        TableEntity entity = tableMapstruct.dtoToEntity(dto);
        tableMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TableDTO dto) {
        TableEntity formEntity = tableMapstruct.dtoToEntity(dto);
        TableEntity dbEntity = selectById(dto.getId());
        tableMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TableEntity dbEntity = selectById(id);
        deleteList(List.of(id));
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteList(List<Long> idList) {
        //关联删除
        tableMapper.deleteByIds(idList);
        tableFieldService.deleteByTableIdList(idList);
    }

    /**
     * 详情
     */
    @Override
    public TableVO detail(Long id) {
        TableEntity dbEntity = selectById(id);
        TableVO tableVO = tableMapstruct.entityToVO(dbEntity);
        ProjectEntity project = projectService.getById(tableVO.getProjectId());
        tableVO.setTableTemplateGroupId(project.getTableTemplateGroupId());
        tableVO.setTemplateGroupPropertyData(templateGroupPropertyDataService.getData(tableVO.getTableTemplateGroupId(), tableVO.getTemplateGroupPropertyData()));
        return tableVO;
    }

    /**
     * 批量查询
     */
    @Override
    public List<TableVO> detailList(List<Long> idList) {
        List<TableEntity> entityList = tableMapper.selectByIds(idList);
        return tableMapstruct.entityToVO(entityList);
    }

    /**
     * 分页（完整VO，含关联字段）
     */
    @Override
    public PageVO<TableVO> page(TableQuery query) {
        //需要JOIN关联字段，使用xml
        Page<TableVO> page = query.toPage();
        tableMapper.voPage(page, query);
        return tableMapstruct.voToPageVO(page);
    }

    /**
     * 列表（完整VO，含关联字段）
     */
    @Override
    public List<TableVO> list(TableQuery query) {
        //需要JOIN关联字段，使用xml
        return tableMapper.voList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTable(TableImportDTO importDTO) throws Exception {
        Long projectId = importDTO.getProjectId();
        List<String> tableNameList = importDTO.getTableNameList();

        //初始化配置信息
        ProjectEntity project = projectService.getById(projectId);

        //批量导入,try-with-resources确保连接在任何路径下都被释放
        try (DataSourceBO dataSource = datasourceService.get(project.getDatasourceId())) {
            for (String tempTableName : tableNameList) {
                importTable(project, dataSource, tempTableName);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByProjectId(List<Long> idList) {
        LambdaQueryWrapper<TableEntity> queryWrapper = Wrappers.lambdaQuery(TableEntity.class).in(TableEntity::getProjectId, idList);
        List<TableEntity> tableList = tableMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(tableList)) {
            return;
        }
        List<Long> tableIdList = tableList.stream().map(TableEntity::getId).toList();
        deleteList(tableIdList);
    }

    @Override
    public Long getTableTemplateGroupId(Long id) {
        TableEntity table = this.getById(id);
        if (table == null) {
            throw new BusinessException("表不存在");
        }
        ProjectEntity project = projectService.getById(table.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        Long tableTemplateGroupId = project.getTableTemplateGroupId();
        if (tableTemplateGroupId == null) {
            throw new BusinessException("模板组不存在");
        }
        return tableTemplateGroupId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncTableField(Long id) throws Exception {
        TableEntity table = this.getById(id);
        Long projectId = table.getProjectId();
        ProjectEntity project = projectService.getById(projectId);

        // 初始化配置信息,try-with-resources确保连接在任何路径下都被释放
        List<TableFieldEntity> dbTableFieldList;
        try (DataSourceBO datasource = datasourceService.get(project.getDatasourceId())) {
            // 从数据库获取表字段列表
            dbTableFieldList = DbUtil.getTableFieldList(datasource, table.getId(), table.getTableName());
        }
        if (dbTableFieldList.isEmpty()) {
            throw new ServerException("同步失败，请检查数据库表：" + table.getTableName());
        }

        List<String> dbTableFieldNameList = dbTableFieldList.stream()
                .map(TableFieldEntity::getFieldName)
                .toList();

        // 表字段列表
        List<TableFieldEntity> tableFieldList = tableFieldService.getByTableId(id);

        Map<String, TableFieldEntity> tableFieldMap = tableFieldList.stream()
                .collect(Collectors.toMap(TableFieldEntity::getFieldName, Function.identity()));

        // 初始化字段数据
        tableFieldService.initFieldList(dbTableFieldList);

        // 同步表结构字段
        dbTableFieldList.forEach(field -> {
            // 新增字段
            if (!tableFieldMap.containsKey(field.getFieldName())) {
                tableFieldService.save(field);
                return;
            }

            // 修改字段
            TableFieldEntity updateField = tableFieldMap.get(field.getFieldName());
            updateField.setPrimaryPk(field.getPrimaryPk());
            updateField.setFieldComment(field.getFieldComment());
            updateField.setFieldType(field.getFieldType());
            updateField.setAttrType(field.getAttrType());

            tableFieldService.updateById(updateField);
        });

        // 删除数据库表中没有的字段
        List<TableFieldEntity> delFieldList = tableFieldList.stream().filter(field -> !dbTableFieldNameList.contains(field.getFieldName())).toList();
        if (!delFieldList.isEmpty()) {
            List<Long> fieldIds = delFieldList.stream().map(TableFieldEntity::getId).collect(Collectors.toList());
            tableFieldService.removeBatchByIds(fieldIds);
        }
    }

    @Override
    public TableGenerateCheckVO generateCheck(List<Long> idList) {
        TableGenerateCheckVO checkVO = new TableGenerateCheckVO();
        List<Long> projectIdList = tableMapper.distinctProjectIdList(idList);
        if (CollUtil.size(projectIdList) == 1) {
            checkVO.setCheckResult(true);
            ProjectEntity project = projectService.getById(projectIdList.getFirst());
            checkVO.setTableTemplateGroupId(project.getTableTemplateGroupId());
            checkVO.setGeneratorType(project.getGeneratorType());
        } else {
            checkVO.setCheckResult(false);
        }
        return checkVO;
    }

    @Override
    public List<TableEntity> getTableListByProjectId(Long projectId) {
        TableQuery query = new TableQuery();
        query.setProjectId(projectId);
        return tableMapper.entityList(query);
    }

    private TableEntity selectById(Long id) {
        TableEntity entity = tableMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "表", id);
        }
        return entity;
    }

    private void importTable(ProjectEntity project, DataSourceBO dataSource, String tableName) {
        Long projectId = project.getId();

        //查询表是否存在
        TableEntity table = this.getByTableName(projectId, tableName);
        //表存在
        if (table != null) {
            throw new BusinessException(tableName + "已存在");
        }

        //从数据库获取表信息
        table = DbUtil.getTable(dataSource, tableName);

        //保存表信息
        table.setVersion(project.getProjectVersion());
        table.setProjectId(projectId);
        table.setAuthor(project.getAuthor());
        table.setFormLayout(FormLayoutEnum.ONE.getCode());
        table.setClassName(NamingCase.toPascalCase(tableName));
        table.setModuleName(getModuleName(tableName));
        table.setFunctionName(getFunctionName(table.getModuleName(), tableName));
        table.setPermissionFlag(table.getModuleName() + ":" + NamingCase.toKebabCase(table.getFunctionName()));
        table.setGeneratorFunction(List.of(0, 1, 2, 3, 4, 5));
        table.setTemplateGroupPropertyData(Map.of());
        this.save(table);

        // 获取原生字段数据
        List<TableFieldEntity> tableFieldList = DbUtil.getTableFieldList(dataSource, table.getId(), table.getTableName());
        // 初始化字段数据
        tableFieldService.initFieldList(tableFieldList);

        // 保存列数据
        tableFieldService.saveBatch(tableFieldList);
    }

    private String getModuleName(String tableName) {
        String[] split = tableName.split("_");
        if (split.length > 1) {
            return split[0];
        }
        return tableName;
    }

    private String getFunctionName(String moduleName, String tableName) {
        // 如果tableName包含moduleName，例如sys_user，则取user
        if (tableName.contains(moduleName + "_")) {
            return StrUtil.toCamelCase(tableName.substring(moduleName.length() + 1));
        }
        return tableName;
    }

    private TableEntity getByTableName(Long projectId, String tableName) {
        return tableMapper.selectOne(Wrappers.lambdaQuery(TableEntity.class)
                .eq(TableEntity::getProjectId, projectId)
                .eq(TableEntity::getTableName, tableName));
    }

}