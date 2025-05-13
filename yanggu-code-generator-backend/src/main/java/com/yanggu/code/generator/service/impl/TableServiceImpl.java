package com.yanggu.code.generator.service.impl;

import cn.hutool.core.text.NamingCase;
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
import com.yanggu.code.generator.domain.vo.TableVO;
import com.yanggu.code.generator.enums.FormLayoutEnum;
import com.yanggu.code.generator.mapper.TableMapper;
import com.yanggu.code.generator.mapstruct.TableMapstruct;
import com.yanggu.code.generator.service.DatasourceService;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TableFieldService;
import com.yanggu.code.generator.service.TableService;
import com.yanggu.code.generator.util.GenUtils;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
        //唯一性校验等
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
        //唯一性校验等
        tableMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        TableEntity dbEntity = selectById(id);
        //删除校验和关联删除
        tableMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        //删除校验和关联删除
        tableMapper.deleteByIds(idList);
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

        Long datasourceId = project.getDatasourceId();
        DataSourceBO dataSource = datasourceService.get(datasourceId);

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
        List<Long> tableIdList = tableList.stream().map(TableEntity::getId).toList();
        tableMapper.delete(queryWrapper);

        //删除对应的字段
        tableFieldService.deleteByTableIdList(tableIdList);
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
        Long templateGroupId = project.getTableTemplateGroupId();
        if (templateGroupId == null) {
            throw new BusinessException("模板组不存在");
        }
        return templateGroupId;
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
        table = GenUtils.getTable(dataSource, tableName);

        //保存表信息
        table.setVersion(project.getProjectVersion());
        table.setProjectId(projectId);
        table.setAuthor(project.getAuthor());
        table.setFormLayout(FormLayoutEnum.ONE.getValue());
        table.setClassName(NamingCase.toPascalCase(tableName));
        table.setFunctionName(GenUtils.getFunctionName(tableName));
        table.setCreateTime(new Date());
        this.save(table);

        // 获取原生字段数据
        List<TableFieldEntity> tableFieldList = GenUtils.getTableFieldList(dataSource, table.getId(), table.getTableName());
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

    /**
     * 批量查询
     */
    @Override
    public List<TableVO> detailList(List<Long> idList) {
        List<TableEntity> entityList = tableMapper.selectByIds(idList);
        return tableMapstruct.entityToVO(entityList);
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

        //过滤字段
        wrapper.like(StrUtil.isNotBlank(query.getTableName()), TableEntity::getTableName, query.getTableName());
        wrapper.eq(Objects.nonNull(query.getProjectId()), TableEntity::getProjectId, query.getProjectId());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}