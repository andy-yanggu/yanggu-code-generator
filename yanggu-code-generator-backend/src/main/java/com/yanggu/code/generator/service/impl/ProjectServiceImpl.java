package com.yanggu.code.generator.service.impl;

import cn.hutool.v7.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.bo.DataSourceBO;
import com.yanggu.code.generator.domain.dto.ProjectDTO;
import com.yanggu.code.generator.domain.dto.TableImportDTO;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.query.ProjectQuery;
import com.yanggu.code.generator.domain.query.ProjectTableQuery;
import com.yanggu.code.generator.domain.vo.ProjectVO;
import com.yanggu.code.generator.domain.vo.TableImportVO;
import com.yanggu.code.generator.mapper.ProjectMapper;
import com.yanggu.code.generator.mapstruct.ProjectMapstruct;
import com.yanggu.code.generator.service.*;
import com.yanggu.code.generator.util.DbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_ALREADY_EXIST;
import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 项目Service实现类
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, ProjectEntity> implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectMapstruct projectMapstruct;

    @Autowired
    private DatasourceService datasourceService;

    @Autowired
    private TableService tableService;

    @Autowired
    private EnumService enumService;

    @Autowired
    private TemplateGroupPropertyDataService templateGroupPropertyDataService;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ProjectDTO dto) throws Exception {
        //唯一性校验等
        checkUnique(dto);
        ProjectEntity entity = projectMapstruct.dtoToEntity(dto);
        projectMapper.insert(entity);

        //默认导入项目对应数据库下的所有表,try-with-resources确保连接在任何路径下都被释放
        Long datasourceId = entity.getDatasourceId();
        if (datasourceId != null) {
            List<String> tableNameList;
            try (DataSourceBO dataSourceBO = datasourceService.get(datasourceId)) {
                tableNameList = DbUtil.getTableNameList(dataSourceBO);
            }

            if (CollUtil.isEmpty(tableNameList)) {
                return;
            }

            TableImportDTO tableImportDTO = new TableImportDTO();
            tableImportDTO.setProjectId(entity.getId());
            tableImportDTO.setTableNameList(tableNameList);

            tableService.importTable(tableImportDTO);
        }
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProjectDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        ProjectEntity formEntity = projectMapstruct.dtoToEntity(dto);
        Long projectId = dto.getId();
        ProjectEntity dbEntity = selectById(projectId);
        projectMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ProjectEntity dbEntity = selectById(id);
        deleteList(List.of(id));
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteList(List<Long> idList) {
        //删除校验和关联删除
        projectMapper.deleteByIds(idList);
        //删除项目对应的表
        tableService.deleteByProjectId(idList);
        //删除项目对应的枚举
        enumService.deleteByProjectId(idList);
    }

    /**
     * 详情
     */
    @Override
    public ProjectVO detail(Long id) {
        ProjectEntity dbEntity = selectById(id);
        ProjectVO projectVO = projectMapstruct.entityToVO(dbEntity);
        projectVO.setTemplateGroupPropertyData(templateGroupPropertyDataService.getData(projectVO.getProjectTemplateGroupId(), projectVO.getTemplateGroupPropertyData()));
        return projectVO;
    }

    /**
     * 批量查询
     */
    @Override
    public List<ProjectVO> detailList(List<Long> idList) {
        List<ProjectEntity> entityList = projectMapper.selectByIds(idList);
        return projectMapstruct.entityToVO(entityList);
    }

    /**
     * 分页（完整VO，含关联字段）
     */
    @Override
    public PageVO<ProjectVO> page(ProjectQuery query) {
        //需要JOIN关联字段，使用xml
        Page<ProjectVO> page = query.toPage();
        projectMapper.voPage(page, query);
        return projectMapstruct.voToPageVO(page);
    }

    /**
     * 列表（完整VO，含关联字段）
     */
    @Override
    public List<ProjectVO> list(ProjectQuery query) {
        //需要JOIN关联字段，使用xml
        return projectMapper.voList(query);
    }

    @Override
    public List<TableImportVO> tableList(ProjectTableQuery query) throws Exception {
        Long projectId = query.getProjectId();
        ProjectEntity project = selectById(projectId);
        Long datasourceId = project.getDatasourceId();
        if (datasourceId == null) {
            throw new BusinessException("该项目没有配置数据源，请先配置数据源");
        }
        // 获取数据源,try-with-resources确保连接在任何路径下都被释放
        List<TableImportVO> tableList;
        try (DataSourceBO datasource = datasourceService.get(datasourceId)) {
            // 根据数据源，获取全部数据表
            tableList = DbUtil.getTableList(datasource, query.getTableName());
        }
        tableList.forEach(table -> {
            LambdaQueryWrapper<TableEntity> queryWrapper = Wrappers.lambdaQuery(TableEntity.class);
            queryWrapper.eq(TableEntity::getProjectId, projectId);
            queryWrapper.eq(TableEntity::getTableName, table.getTableName());
            boolean exists = tableService.exists(queryWrapper);
            table.setExist(exists);
        });

        return tableList;
    }

    private ProjectEntity selectById(Long id) {
        ProjectEntity entity = projectMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "项目", id);
        }
        return entity;
    }

    private void checkUnique(ProjectDTO dto) {
        //唯一性校验
        LambdaQueryWrapper<ProjectEntity> queryWrapper = Wrappers.lambdaQuery(ProjectEntity.class);
        queryWrapper.ne(Objects.nonNull(dto.getId()), ProjectEntity::getId, dto.getId());
        queryWrapper.eq(ProjectEntity::getProjectName, dto.getProjectName());
        boolean exists = exists(queryWrapper);
        if (exists) {
            throw new BusinessException(DATA_ALREADY_EXIST, "项目", dto.getProjectName());
        }
    }

}