package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.dto.BaseClassDTO;
import com.yanggu.code.generator.domain.entity.BaseClassEntity;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.query.BaseClassQuery;
import com.yanggu.code.generator.domain.vo.BaseClassVO;
import com.yanggu.code.generator.mapper.BaseClassMapper;
import com.yanggu.code.generator.mapstruct.BaseClassMapstruct;
import com.yanggu.code.generator.service.BaseClassService;
import com.yanggu.code.generator.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
    private TableService tableService;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(BaseClassDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        BaseClassEntity entity = baseClassMapstruct.dtoToEntity(dto);
        baseClassMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(BaseClassDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        BaseClassEntity formEntity = baseClassMapstruct.dtoToEntity(dto);
        BaseClassEntity dbEntity = selectById(dto.getId());
        baseClassMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        BaseClassEntity dbEntity = selectById(id);
        deleteList(List.of(id));
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        //删除校验
        checkReference(idList);
        //关联删除
        baseClassMapper.deleteByIds(idList);
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
     * 批量查询
     */
    @Override
    public List<BaseClassVO> detailList(List<Long> idList) {
        List<BaseClassEntity> entityList = baseClassMapper.selectByIds(idList);
        return baseClassMapstruct.entityToVO(entityList);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<BaseClassVO> entityPage(BaseClassQuery query) {
        Page<BaseClassEntity> page = query.toPage();
        //简单sql使用QueryWrapper
        baseClassMapper.selectPage(page, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //baseClassMapper.entityPage(page, query);
        return baseClassMapstruct.entityToPageVO(page);
    }

    /**
     * 简单列表
     */
    @Override
    public List<BaseClassVO> entityList(BaseClassQuery query) {
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
    public PageVO<BaseClassVO> voPage(BaseClassQuery query) {
        Page<BaseClassVO> page = query.toPage();
        baseClassMapper.voPage(page, query);
        return baseClassMapstruct.voToPageVO(page);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<BaseClassVO> voList(BaseClassQuery query) {
        //查询全部数据：不传page即不触发分页插件
        return baseClassMapper.voList(query);
    }

    private BaseClassEntity selectById(Long id) {
        BaseClassEntity entity = baseClassMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "基类", id);
        }
        return entity;
    }

    /**
     * 唯一性校验
     */
    private void checkUnique(BaseClassDTO dto) {
        LambdaQueryWrapper<BaseClassEntity> wrapper = Wrappers.lambdaQuery(BaseClassEntity.class);
        wrapper.ne(Objects.nonNull(dto.getId()), BaseClassEntity::getId, dto.getId());
        wrapper.and(temp -> temp
                .and(inner -> inner
                        .apply("CONCAT(package_name, '.', class_name) = CONCAT({0}, '.', {1})",
                                dto.getPackageName(), dto.getClassName())
                .or()
                .eq(BaseClassEntity::getBaseClassName, dto.getBaseClassName())
        ));

        boolean exists = baseClassMapper.exists(wrapper);
        if (exists) {
            throw new BusinessException("基类名称或者（包名+类名）已存在");
        }
    }

    private void checkReference(List<Long> idList) {
        LambdaQueryWrapper<TableEntity> queryWrapper = Wrappers.lambdaQuery(TableEntity.class)
                .and(wrapper -> wrapper
                        .in(TableEntity::getEntityBaseClassId, idList)
                        .or()
                        .in(TableEntity::getVoBaseClassId, idList)
                );
        boolean exists = tableService.exists(queryWrapper);
        if (exists) {
            throw new BusinessException("基类已被表引用，不能删除");
        }
    }

    private LambdaQueryWrapper<BaseClassEntity> buildQueryWrapper(BaseClassQuery query) {
        LambdaQueryWrapper<BaseClassEntity> wrapper = Wrappers.lambdaQuery(BaseClassEntity.class);

        //过滤字段
        wrapper.like(MybatisUtil.isNotEmpty(query.getBaseClassName()), BaseClassEntity::getBaseClassName, query.getBaseClassName());
        wrapper.like(MybatisUtil.isNotEmpty(query.getPackageName()), BaseClassEntity::getPackageName, query.getPackageName());
        wrapper.like(MybatisUtil.isNotEmpty(query.getClassName()), BaseClassEntity::getClassName, query.getClassName());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrderItemList());
        return wrapper;
    }

}