package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.mapstruct.BaseClassMapstruct;
import com.yanggu.code.generator.domain.entity.BaseClassEntity;
import com.yanggu.code.generator.domain.query.BaseClassVOQuery;
import com.yanggu.code.generator.domain.query.BaseClassEntityQuery;
import com.yanggu.code.generator.domain.dto.BaseClassDTO;
import com.yanggu.code.generator.domain.vo.BaseClassVO;
import com.yanggu.code.generator.mapper.BaseClassMapper;
import com.yanggu.code.generator.service.BaseClassService;
import com.yanggu.code.generator.service.ProjectService;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 基类Service实现类
 */
@Service
public class BaseClassServiceImpl extends ServiceImpl<BaseClassMapper, BaseClassEntity> implements BaseClassService {

    @Autowired
    private BaseClassMapper baseClassMapper;

    @Autowired
    private BaseClassMapstruct baseClassMapstruct;

    @Autowired
    private ProjectService projectService;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(BaseClassDTO dto) {
        BaseClassEntity entity = baseClassMapstruct.dtoToEntity(dto);
        //唯一性校验等
        baseClassMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(BaseClassDTO dto) {
        BaseClassEntity formEntity = baseClassMapstruct.dtoToEntity(dto);
        BaseClassEntity dbEntity = selectById(dto.getId());
        //唯一性校验等
        baseClassMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        BaseClassEntity dbEntity = selectById(id);
        checkReference(List.of(id));
        //删除校验和关联删除
        baseClassMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        checkReference(idList);
        //删除校验和关联删除
        baseClassMapper.deleteByIds(idList);
    }

    private void checkReference(List<Long> idList) {
        LambdaQueryWrapper<ProjectEntity> queryWrapper = Wrappers.lambdaQuery(ProjectEntity.class)
                .in(ProjectEntity::getBaseClassId, idList);
        boolean exists = projectService.exists(queryWrapper);
        if (exists) {
            throw new BusinessException("基类已被项目引用，不能删除");
        }
    }

    /**
     * 详情
     */
    @Override
    public BaseClassVO detail(Long id) {
        BaseClassEntity dbEntity = selectById(id);
        return baseClassMapstruct.entityToVO(dbEntity);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<BaseClassVO> entityPage(BaseClassEntityQuery query) {
        //简单sql使用QueryWrapper
        baseClassMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //baseClassMapper.entityPage(query);
        return baseClassMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<BaseClassVO> entityList(BaseClassEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<BaseClassEntity> entityList = baseClassMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<BaseClassEntity> entityList = baseClassMapper.entityList(query);
        return baseClassMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<BaseClassVO> voPage(BaseClassVOQuery query) {
        baseClassMapper.voPage(query);
        return baseClassMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<BaseClassVO> voList(BaseClassVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return baseClassMapper.voList(query);
    }

    /**
     * 批量查询
     */
    @Override
    public List<BaseClassVO> detailList(List<Long> idList) {
        List<BaseClassEntity> entityList = baseClassMapper.selectByIds(idList);
        return baseClassMapstruct.entityToVO(entityList);
    }

    private BaseClassEntity selectById(Long id) {
        BaseClassEntity entity = baseClassMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "基类", id);
        }
        return entity;
    }

    private LambdaQueryWrapper<BaseClassEntity> buildQueryWrapper(BaseClassEntityQuery query) {
        LambdaQueryWrapper<BaseClassEntity> wrapper = Wrappers.lambdaQuery(BaseClassEntity.class);

        //过滤字段
        wrapper.like(StrUtil.isNotBlank(query.getCode()), BaseClassEntity::getCode, query.getCode());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}