package com.yanggu.code.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.dto.TemplateContentDTO;
import com.yanggu.code.generator.domain.dto.TemplateDTO;
import com.yanggu.code.generator.domain.dto.TemplateDragDTO;
import com.yanggu.code.generator.domain.entity.TemplateEntity;
import com.yanggu.code.generator.domain.query.TemplateEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateVO;
import com.yanggu.code.generator.enums.TemplateTypeEnum;
import com.yanggu.code.generator.mapper.TemplateMapper;
import com.yanggu.code.generator.mapstruct.TemplateMapstruct;
import com.yanggu.code.generator.service.TemplateService;
import org.dromara.hutool.core.text.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;
import static com.yanggu.code.generator.domain.vo.TemplateVO.TREE_COMPARATOR;

/**
 * 模板Service实现类
 */
@Service
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, TemplateEntity> implements TemplateService {

    @Autowired
    private TemplateMapper templateMapper;

    @Autowired
    private TemplateMapstruct templateMapstruct;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public TemplateEntity add(TemplateDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        TemplateEntity entity = templateMapstruct.dtoToEntity(dto);
        templateMapper.insert(entity);
        return entity;
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(TemplateDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        TemplateEntity formEntity = templateMapstruct.dtoToEntity(dto);
        TemplateEntity dbEntity = selectById(dto.getId());
        templateMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        TemplateEntity dbEntity = selectById(id);
        deleteList(List.of(id));
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        //删除校验和关联删除
        templateMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public TemplateVO detail(Long id) {
        TemplateEntity dbEntity = selectById(id);
        return templateMapstruct.entityToVO(dbEntity);
    }

    /**
     * 批量查询
     */
    @Override
    public List<TemplateVO> detailList(List<Long> idList) {
        List<TemplateEntity> entityList = templateMapper.selectByIds(idList);
        return templateMapstruct.entityToVO(entityList);
    }


    /**
     * 简单分页
     */
    @Override
    public PageVO<TemplateVO> entityPage(TemplateEntityQuery query) {
        //简单sql使用QueryWrapper
        templateMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //templateMapper.entityPage(query);
        return templateMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<TemplateVO> entityList(TemplateEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<TemplateEntity> entityList = templateMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<TemplateBO> entityList = templateMapper.entityList(query);
        return templateMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<TemplateVO> voPage(TemplateVOQuery query) {
        templateMapper.voPage(query);
        return templateMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<TemplateVO> voList(TemplateVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return templateMapper.voList(query);
    }

    @Override
    public List<TemplateEntity> selectByGroupId(Long groupId) {
        LambdaQueryWrapper<TemplateEntity> queryWrapper = Wrappers.lambdaQuery(TemplateEntity.class)
                .eq(TemplateEntity::getTemplateGroupId, groupId);
        return templateMapper.selectList(queryWrapper);
    }

    @Override
    public List<TemplateVO> tree(Long templateGroupId) {
        TemplateEntityQuery query = new TemplateEntityQuery();
        query.setTemplateGroupId(templateGroupId);
        List<TemplateVO> allList = this.entityList(query);
        return allList.stream()
                .filter(templateVO -> templateVO.getParentId() == 0)
                .peek(templateVO -> templateVO.setChildren(getChildren(allList, templateVO.getId())))
                .sorted(TREE_COMPARATOR)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void updateContent(TemplateContentDTO contentDTO) {
        TemplateEntity entity = new TemplateEntity();
        entity.setId(contentDTO.getId());
        entity.setTemplateContent(contentDTO.getTemplateContent());
        templateMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void updateParent(TemplateDragDTO dragDTO) {
        Long dragNodeId = dragDTO.getDragNodeId();
        Long newParentId = dragDTO.getNewParentId();
        // 1. 校验拖拽节点是否存在
        TemplateEntity dragNode = templateMapper.selectById(dragNodeId);
        if (dragNode == null) {
            throw new BusinessException("拖拽的节点不存在");
        }

        // 2. 校验新父节点
        TemplateEntity parentNode = templateMapper.selectById(newParentId);
        if (parentNode == null) {
            throw new BusinessException("目标父节点不存在");
        }
        // 文件不能接子节点
        if (parentNode.getTemplateType() != 0) {
            throw new BusinessException("文件节点不能作为父级");
        }

        // 3. 防止循环引用：不能把节点拖到自己或自己的子孙下面
        if (Objects.equals(dragNodeId, newParentId)) {
            throw new BusinessException("不能将节点拖到自己下面");
        }
        if (newParentId != null && newParentId > 0 && isDescendant(newParentId, dragNodeId)) {
            throw new BusinessException("不能将节点拖到自己的子节点下面");
        }

        // 4. 更新 parentId
        TemplateEntity updateEntity = new TemplateEntity();
        updateEntity.setId(dragNodeId);
        updateEntity.setParentId(newParentId);
        templateMapper.updateById(updateEntity);
    }

    /**
     * 判断 nodeId 是否是 targetId 的子孙节点
     */
    private boolean isDescendant(Long nodeId, Long targetId) {
        TemplateEntity node = templateMapper.selectById(nodeId);
        while (node != null && node.getParentId() != null && node.getParentId() > 0) {
            if (Objects.equals(node.getParentId(), targetId)) {
                return true;
            }
            node = templateMapper.selectById(node.getParentId());
        }
        return false;
    }

    private List<TemplateVO> getChildren(List<TemplateVO> allList, Long id) {
        return allList.stream()
                .filter(templateVO -> templateVO.getParentId().equals(id))
                .peek(templateVO -> templateVO.setChildren(getChildren(allList, templateVO.getId())))
                .sorted(TREE_COMPARATOR)
                .toList();
    }

    private TemplateEntity selectById(Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "模板", id);
        }
        return entity;
    }

    private void checkUnique(TemplateDTO dto) {
        LambdaQueryWrapper<TemplateEntity> queryWrapper = Wrappers.lambdaQuery(TemplateEntity.class)
                .ne(Objects.nonNull(dto.getId()), TemplateEntity::getId, dto.getId())
                .eq(TemplateEntity::getTemplateGroupId, dto.getTemplateGroupId())
                .eq(TemplateEntity::getParentId, dto.getParentId());

        Supplier<String> supplier;
        if (Objects.equals(dto.getTemplateType(), TemplateTypeEnum.DIRECTORY.getCode())) {
            queryWrapper.eq(TemplateEntity::getFileName, dto.getFileName());
            supplier = () -> StrUtil.format("目录名称: {}已存在", dto.getFileName());
        } else {
            queryWrapper.and(wrapper -> wrapper
                    .eq(TemplateEntity::getTemplateName, dto.getTemplateName())
                    .or()
                    .eq(TemplateEntity::getFileName, dto.getFileName())
            );
            supplier = () -> "模板名称或者文件/目录名称已存在";
        }

        boolean exists = templateMapper.exists(queryWrapper);
        if (exists) {
            throw new BusinessException(supplier.get());
        }
    }

    private LambdaQueryWrapper<TemplateEntity> buildQueryWrapper(TemplateEntityQuery query) {
        LambdaQueryWrapper<TemplateEntity> wrapper = Wrappers.lambdaQuery(TemplateEntity.class);

        //过滤字段
        wrapper.eq(MybatisUtil.isNotEmpty(query.getTemplateGroupId()), TemplateEntity::getTemplateGroupId, query.getTemplateGroupId());
        wrapper.like(MybatisUtil.isNotEmpty(query.getTemplateName()), TemplateEntity::getTemplateName, query.getTemplateName());
        wrapper.eq(MybatisUtil.isNotEmpty(query.getTemplateType()), TemplateEntity::getTemplateType, query.getTemplateType());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrderItemList());
        return wrapper;
    }

}