package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.bo.DataSourceBO;
import com.yanggu.code.generator.domain.dto.TableDTO;
import com.yanggu.code.generator.domain.dto.TableImportDTO;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.query.TableEntityQuery;
import com.yanggu.code.generator.domain.query.TableVOQuery;
import com.yanggu.code.generator.domain.vo.TableGenerateCheckVO;
import com.yanggu.code.generator.domain.vo.TableVO;
import com.yanggu.code.generator.enums.FormLayoutEnum;
import com.yanggu.code.generator.mapper.TableMapper;
import com.yanggu.code.generator.mapstruct.TableMapstruct;
import com.yanggu.code.generator.service.DatasourceService;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TableFieldService;
import com.yanggu.code.generator.service.TableService;
import com.yanggu.code.generator.util.DbUtil;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.text.NamingCase;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.rmi.ServerException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(TableDTO dto) {
        TableEntity entity = tableMapstruct.dtoToEntity(dto);
        tableMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(TableDTO dto) {
        TableEntity formEntity = tableMapstruct.dtoToEntity(dto);
        TableEntity dbEntity = selectById(dto.getId());
        tableMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        TableEntity dbEntity = selectById(id);
        deleteList(List.of(id));
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
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
        return tableMapstruct.entityToVO(dbEntity);
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
     * 简单分页
     */
    @Override
    public PageVO<TableVO> entityPage(TableEntityQuery query) {
        //简单sql使用QueryWrapper
        tableMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //tableMapper.entityPage(query);
        return tableMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<TableVO> entityList(TableEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<TableEntity> entityList = tableMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<TableEntity> entityList = tableMapper.entityList(query);
        return tableMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<TableVO> voPage(TableVOQuery query) {
        tableMapper.voPage(query);
        return tableMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<TableVO> voList(TableVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return tableMapper.voList(query);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void importTable(TableImportDTO importDTO) throws Exception {
        Long projectId = importDTO.getProjectId();
        List<String> tableNameList = importDTO.getTableNameList();

        //初始化配置信息
        ProjectEntity project = projectService.getById(projectId);

        DataSourceBO dataSource = datasourceService.get(project.getDatasourceId());

        //批量导入
        for (String tempTableName : tableNameList) {
            importTable(project, dataSource, tempTableName);
        }
        try {
            //释放数据源
            dataSource.getConnection().close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
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
    @Transactional(rollbackFor = RuntimeException.class)
    public void syncTableField(Long id) throws Exception {
        TableEntity table = this.getById(id);
        Long projectId = table.getProjectId();
        ProjectEntity project = projectService.getById(projectId);

        // 初始化配置信息
        DataSourceBO datasource = datasourceService.get(project.getDatasourceId());

        // 从数据库获取表字段列表
        List<TableFieldEntity> dbTableFieldList = DbUtil.getTableFieldList(datasource, table.getId(), table.getTableName());
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
        TableEntityQuery query = new TableEntityQuery();
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

    private LambdaQueryWrapper<TableEntity> buildQueryWrapper(TableEntityQuery query) {
        LambdaQueryWrapper<TableEntity> wrapper = Wrappers.lambdaQuery(TableEntity.class);

        wrapper.eq(Objects.nonNull(query.getProjectId()), TableEntity::getProjectId, query.getProjectId());
        wrapper.like(MybatisUtil.isNotEmpty(query.getTableName()), TableEntity::getTableName, query.getTableName());
        wrapper.like(MybatisUtil.isNotEmpty(query.getClassName()), TableEntity::getClassName, query.getClassName());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrderItemList());
        return wrapper;
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
        table.setFunctionName(StrUtil.toCamelCase(tableName));
        this.save(table);

        // 获取原生字段数据
        List<TableFieldEntity> tableFieldList = DbUtil.getTableFieldList(dataSource, table.getId(), table.getTableName());
        // 初始化字段数据
        tableFieldService.initFieldList(tableFieldList);

        // 保存列数据
        tableFieldService.saveBatch(tableFieldList);
    }

    private TableEntity getByTableName(Long projectId, String tableName) {
        return tableMapper.selectOne(Wrappers.lambdaQuery(TableEntity.class)
                .eq(TableEntity::getProjectId, projectId)
                .eq(TableEntity::getTableName, tableName));
    }

}