package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.bo.GenDataSourceBO;
import com.yanggu.code.generator.domain.dto.DatasourceDTO;
import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.query.DatasourceEntityQuery;
import com.yanggu.code.generator.domain.query.DatasourceVOQuery;
import com.yanggu.code.generator.domain.vo.DatasourceVO;
import com.yanggu.code.generator.mapper.DatasourceMapper;
import com.yanggu.code.generator.mapstruct.DatasourceMapstruct;
import com.yanggu.code.generator.service.DatasourceService;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.util.DbUtils;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 数据源Service实现类
 */
@Service
public class DatasourceServiceImpl extends ServiceImpl<DatasourceMapper, DatasourceEntity> implements DatasourceService {

    @Autowired
    private DatasourceMapper datasourceMapper;

    @Autowired
    private DatasourceMapstruct datasourceMapstruct;

    @Autowired
    private ProjectService projectService;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(DatasourceDTO dto) {
        DatasourceEntity entity = datasourceMapstruct.dtoToEntity(dto);
        //唯一性校验等
        datasourceMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(DatasourceDTO dto) {
        DatasourceEntity formEntity = datasourceMapstruct.dtoToEntity(dto);
        DatasourceEntity dbEntity = selectById(dto.getId());
        //唯一性校验等
        datasourceMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        DatasourceEntity dbEntity = selectById(id);
        check(List.of(id));
        //删除校验和关联删除
        datasourceMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        check(idList);
        //删除校验和关联删除
        datasourceMapper.deleteByIds(idList);
    }

    private void check(List<Long> idList) {
        LambdaQueryWrapper<ProjectEntity> queryWrapper = Wrappers.lambdaQuery(ProjectEntity.class)
                .in(ProjectEntity::getDatasourceId, idList);
        boolean exists = projectService.exists(queryWrapper);
        if (exists) {
            throw new BusinessException("数据源已被项目引用，不能删除");
        }
    }

    /**
     * 详情
     */
    @Override
    public DatasourceVO detail(Long id) {
        DatasourceEntity dbEntity = selectById(id);
        return datasourceMapstruct.entityToVO(dbEntity);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<DatasourceVO> entityPage(DatasourceEntityQuery query) {
        //简单sql使用QueryWrapper
        datasourceMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //datasourceMapper.entityPage(query);
        return datasourceMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<DatasourceVO> entityList(DatasourceEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<DatasourceEntity> entityList = datasourceMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<DatasourceEntity> entityList = datasourceMapper.entityList(query);
        return datasourceMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<DatasourceVO> voPage(DatasourceVOQuery query) {
        datasourceMapper.voPage(query);
        return datasourceMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<DatasourceVO> voList(DatasourceVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return datasourceMapper.voList(query);
    }

    @Override
    public GenDataSourceBO get(Long datasourceId) throws Exception {
        // 初始化配置信息
        return new GenDataSourceBO(this.getById(datasourceId));
    }

    @Override
    public void test(Long id) throws Exception {
        DatasourceEntity entity = getById(id);
        DbUtils.getConnection(new GenDataSourceBO(entity));
    }

    /**
     * 批量查询
     */
    @Override
    public List<DatasourceVO> detailList(List<Long> idList) {
        List<DatasourceEntity> entityList = datasourceMapper.selectByIds(idList);
        return datasourceMapstruct.entityToVO(entityList);
    }

    private DatasourceEntity selectById(Long id) {
        DatasourceEntity entity = datasourceMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "数据源", id);
        }
        return entity;
    }

    private LambdaQueryWrapper<DatasourceEntity> buildQueryWrapper(DatasourceEntityQuery query) {
        LambdaQueryWrapper<DatasourceEntity> wrapper = Wrappers.lambdaQuery(DatasourceEntity.class);

        //过滤字段
        wrapper.eq(StrUtil.isNotBlank(query.getDbType()), DatasourceEntity::getDbType, query.getDbType());
        wrapper.like(StrUtil.isNotBlank(query.getConnName()), DatasourceEntity::getConnName, query.getConnName());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}