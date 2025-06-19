package com.yanggu.code.generator.service.impl;

import ch.qos.logback.classic.LoggerContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.dto.EnumDTO;
import com.yanggu.code.generator.domain.entity.EnumEntity;
import com.yanggu.code.generator.domain.entity.EnumItemEntity;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.query.EnumEntityQuery;
import com.yanggu.code.generator.domain.query.EnumVOQuery;
import com.yanggu.code.generator.domain.vo.EnumGenerateCheckVO;
import com.yanggu.code.generator.domain.vo.EnumVO;
import com.yanggu.code.generator.mapper.EnumMapper;
import com.yanggu.code.generator.mapstruct.EnumMapstruct;
import com.yanggu.code.generator.service.EnumItemService;
import com.yanggu.code.generator.service.EnumService;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TableFieldService;
import org.dromara.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;

/**
 * 枚举Service实现类
 */
@Service
public class EnumServiceImpl extends ServiceImpl<EnumMapper, EnumEntity> implements EnumService {

    @Autowired
    private EnumMapper enumMapper;

    @Autowired
    private EnumMapstruct enumMapstruct;

    @Autowired
    private EnumItemService enumItemService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TableFieldService tableFieldService;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(EnumDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        EnumEntity entity = enumMapstruct.dtoToEntity(dto);
        enumMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(EnumDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        EnumEntity formEntity = enumMapstruct.dtoToEntity(dto);
        EnumEntity dbEntity = selectById(dto.getId());
        enumMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        EnumEntity dbEntity = selectById(id);
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
        enumMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public EnumVO detail(Long id) {
        EnumEntity dbEntity = selectById(id);
        return enumMapstruct.entityToVO(dbEntity);
    }

    /**
     * 批量查询
     */
    @Override
    public List<EnumVO> detailList(List<Long> idList) {
        List<EnumEntity> entityList = enumMapper.selectByIds(idList);
        return enumMapstruct.entityToVO(entityList);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<EnumVO> entityPage(EnumEntityQuery query) {
        //简单sql使用QueryWrapper
        enumMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //enumMapper.entityPage(query);
        return enumMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<EnumVO> entityList(EnumEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<EnumEntity> entityList = enumMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<EnumEntity> entityList = enumMapper.entityList(query);
        return enumMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<EnumVO> voPage(EnumVOQuery query) {
        enumMapper.voPage(query);
        return enumMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<EnumVO> voList(EnumVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return enumMapper.voList(query);
    }

    @Override
    public List<EnumEntity> enumList(Long projectId) {
        return enumMapper.selectEnumList(projectId);
    }

    @Override
    public EnumEntity getById(Long id) {
        EnumEntity enumEntity = selectById(id);
        List<EnumItemEntity> enumItemList = enumItemService.selectByEnumId(id);
        enumEntity.setEnumItemList(enumItemList);
        return enumEntity;
    }

    @Override
    public EnumGenerateCheckVO generateCheck(List<Long> idList) {
        EnumGenerateCheckVO checkVO = new EnumGenerateCheckVO();
        List<Long> projectIdList = enumMapper.distinctProjectIdList(idList);
        if (CollUtil.size(projectIdList) == 1) {
            checkVO.setCheckResult(true);
            ProjectEntity project = projectService.getById(projectIdList.getFirst());
            checkVO.setEnumTemplateGroupId(project.getEnumTemplateGroupId());
            checkVO.setGeneratorType(project.getGeneratorType());
        } else {
            checkVO.setCheckResult(false);
        }
        return checkVO;
    }

    private EnumEntity selectById(Long id) {
        EnumEntity entity = enumMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "枚举", id);
        }
        return entity;
    }

    private void checkUnique(EnumDTO dto) {
        LambdaQueryWrapper<EnumEntity> queryWrapper = Wrappers.lambdaQuery(EnumEntity.class)
                .ne(Objects.nonNull(dto.getId()), EnumEntity::getId, dto.getId())
                .eq(EnumEntity::getProjectId, dto.getProjectId())
                .and(wrapper -> wrapper
                        .eq(EnumEntity::getEnumName, dto.getEnumName())
                        .or()
                        .eq(EnumEntity::getEnumDesc, dto.getEnumDesc())
                );

        boolean exists = enumMapper.exists(queryWrapper);
        if (exists) {
            throw new BusinessException("枚举名称或者描述在项目下已存在");
        }
    }

    private void checkReference(List<Long> idList) {
        LambdaQueryWrapper<TableFieldEntity> queryWrapper = Wrappers.lambdaQuery(TableFieldEntity.class)
                .in(TableFieldEntity::getEnumId, idList);

        boolean exists = tableFieldService.exists(queryWrapper);
        if (exists) {
            throw new BusinessException("枚举已被表字段引用，不能删除");
        }
    }

    private LambdaQueryWrapper<EnumEntity> buildQueryWrapper(EnumEntityQuery query) {
        LambdaQueryWrapper<EnumEntity> wrapper = Wrappers.lambdaQuery(EnumEntity.class);

        //过滤字段
        wrapper.eq(MybatisUtil.isNotEmpty(query.getProjectId()), EnumEntity::getProjectId, query.getProjectId());
        wrapper.eq(MybatisUtil.isNotEmpty(query.getEnumName()), EnumEntity::getEnumName, query.getEnumName());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrderItemList());
        return wrapper;
    }

}