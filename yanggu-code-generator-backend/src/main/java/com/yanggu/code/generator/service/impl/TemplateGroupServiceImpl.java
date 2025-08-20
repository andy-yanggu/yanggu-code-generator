package com.yanggu.code.generator.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.bo.TemplateBO;
import com.yanggu.code.generator.domain.bo.TemplateGroupBO;
import com.yanggu.code.generator.domain.dto.TemplateDTO;
import com.yanggu.code.generator.domain.dto.TemplateGroupDTO;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TemplateEntity;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.query.TemplateGroupEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateGroupVO;
import com.yanggu.code.generator.mapper.TemplateGroupMapper;
import com.yanggu.code.generator.mapstruct.TemplateGroupMapstruct;
import com.yanggu.code.generator.mapstruct.TemplateMapstruct;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TemplateGroupService;
import com.yanggu.code.generator.service.TemplateService;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.date.DateUtil;
import org.dromara.hutool.core.io.IoUtil;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.util.CharsetUtil;
import org.dromara.hutool.http.meta.HttpHeaderUtil;
import org.dromara.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;
import static org.dromara.hutool.core.date.DateFormatPool.PURE_DATETIME_PATTERN;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

/**
 * 模板组Service实现类
 */
@Service
public class TemplateGroupServiceImpl extends ServiceImpl<TemplateGroupMapper, TemplateGroupEntity> implements TemplateGroupService {

    @Autowired
    private TemplateGroupMapper templateGroupMapper;

    @Autowired
    private TemplateGroupMapstruct templateGroupMapstruct;

    @Autowired
    private TemplateMapstruct templateMapstruct;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ProjectService projectService;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public TemplateGroupEntity add(TemplateGroupDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        TemplateGroupEntity entity = templateGroupMapstruct.dtoToEntity(dto);
        templateGroupMapper.insert(entity);
        return entity;
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(TemplateGroupDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        TemplateGroupEntity formEntity = templateGroupMapstruct.dtoToEntity(dto);
        TemplateGroupEntity dbEntity = selectById(dto.getId());
        if (!dbEntity.getType().equals(formEntity.getType())) {
            throw new BusinessException("模板组类型不能修改");
        }

        templateGroupMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        TemplateGroupEntity dbEntity = selectById(id);
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
        templateGroupMapper.deleteByIds(idList);
        templateService.deleteByGroupId(idList);
    }

    /**
     * 详情
     */
    @Override
    public TemplateGroupVO detail(Long id) {
        TemplateGroupEntity dbEntity = selectById(id);
        return templateGroupMapstruct.entityToVO(dbEntity);
    }

    /**
     * 批量查询
     */
    @Override
    public List<TemplateGroupVO> detailList(List<Long> idList) {
        List<TemplateGroupEntity> entityList = templateGroupMapper.selectByIds(idList);
        return templateGroupMapstruct.entityToVO(entityList);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<TemplateGroupVO> entityPage(TemplateGroupEntityQuery query) {
        //简单sql使用QueryWrapper
        templateGroupMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //templateGroupMapper.entityPage(query);
        return templateGroupMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<TemplateGroupVO> entityList(TemplateGroupEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<TemplateGroupEntity> entityList = templateGroupMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<TemplateGroupBO> entityList = templateGroupMapper.entityList(query);
        return templateGroupMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<TemplateGroupVO> voPage(TemplateGroupVOQuery query) {
        templateGroupMapper.voPage(query);
        return templateGroupMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<TemplateGroupVO> voList(TemplateGroupVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return templateGroupMapper.voList(query);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void copy(TemplateGroupDTO dto) {
        Long oldGroupId = dto.getId();
        dto.setId(null);

        //新增模板组
        TemplateGroupEntity add = add(dto);
        Long newTemplateGroupId = add.getId();

        //查询原有模板组下的所有模板
        List<TemplateEntity> templateList = templateService.selectByGroupId(oldGroupId);
        if (CollUtil.isEmpty(templateList)) {
            return;
        }

        // 先保存所有顶级模板（parentId为null的模板）
        List<TemplateEntity> rootTemplates = templateList.stream()
                .filter(template -> template.getParentId().equals(0L))
                .toList();

        // 递归复制模板树
        rootTemplates.forEach(rootTemplate -> copyTemplateTree(rootTemplate, 0L, newTemplateGroupId, templateList));
    }

    @Override
    public ResponseEntity<byte[]> export(List<Long> idList) {
        List<TemplateGroupEntity> list = idList.stream()
                .map(this::getById)
                .toList();

        //转换成导出对象
        List<TemplateGroupBO> boList = templateGroupMapstruct.entityToBO(list);

        // 构建模板树结构
        boList.forEach(templateGroupBO -> {
            List<TemplateBO> templateList = templateGroupBO.getTemplateList();
            if (CollUtil.isNotEmpty(templateList)) {
                // 构建模板树
                buildTemplateTree(templateList);
            }
        });

        String jsonStr = JSONUtil.toJsonStr(boList);

        String fileName = "template_group_" + DateUtil.format(new Date(), PURE_DATETIME_PATTERN) + ".json";
        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, HttpHeaderUtil.createAttachmentDisposition(fileName, CharsetUtil.UTF_8))
                .contentType(APPLICATION_OCTET_STREAM)
                .body(jsonStr.getBytes());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void importTemplateGroup(MultipartFile file) throws IOException {
        String data = IoUtil.read(file.getInputStream(), CharsetUtil.UTF_8);
        if (StrUtil.isBlank(data)) {
            throw new BusinessException("导入数据不能为空");
        }
        List<TemplateGroupBO> list = JSONUtil.toList(data, TemplateGroupBO.class);
        if (CollUtil.isEmpty(list)) {
            throw new BusinessException("导入数据不能为空");
        }

        list.forEach(templateGroup -> {
            TemplateGroupDTO groupDTO = templateGroupMapstruct.boToDTO(templateGroup);
            TemplateGroupEntity groupEntity = add(groupDTO);

            List<TemplateBO> templateList = templateGroup.getTemplateList();
            if (CollUtil.isNotEmpty(templateList)) {
                // 直接使用树形结构递归导入
                templateList.forEach(template -> importTemplateTree(template, 0L, groupEntity.getId(), new HashMap<>()));
            }
        });
    }

    @Override
    public TemplateGroupEntity getById(Long id) {
        TemplateGroupEntity templateGroup = selectById(id);
        List<TemplateEntity> templateList = templateService.selectByGroupId(id);
        templateGroup.setTemplateList(templateList);
        return templateGroup;
    }

    /**
     * 递归复制模板树
     *
     * @param sourceTemplate     源模板
     * @param newParentId        新的父模板ID
     * @param newTemplateGroupId 新模板组ID
     * @param templateList     原模板映射
     */
    private void copyTemplateTree(TemplateEntity sourceTemplate, Long newParentId,
                                  Long newTemplateGroupId, List<TemplateEntity> templateList) {
        // 创建新模板DTO
        TemplateDTO templateDTO = templateMapstruct.entityToDTO(sourceTemplate);
        templateDTO.setId(null);
        templateDTO.setParentId(newParentId);
        templateDTO.setTemplateGroupId(newTemplateGroupId);

        // 新增模板
        TemplateEntity newTemplate = templateService.add(templateDTO);

        // 查找所有子模板
        List<TemplateEntity> children = templateList.stream()
                .filter(template -> sourceTemplate.getId().equals(template.getParentId()))
                .toList();

        if (CollUtil.isEmpty(children)) {
            return;
        }

        // 递归处理子模板
        children.forEach(child -> copyTemplateTree(child, newTemplate.getId(), newTemplateGroupId, templateList));
    }

    /**
     * 递归导入模板树
     * @param templateBO 模板BO
     * @param parentId 父模板ID
     * @param templateGroupId 模板组ID
     * @param oldToNewIdMap 新旧ID映射
     */
    private void importTemplateTree(TemplateBO templateBO, Long parentId,
                                    Long templateGroupId, Map<Long, Long> oldToNewIdMap) {
        // 创建模板DTO
        TemplateDTO templateDTO = templateMapstruct.boToDTO(templateBO);
        Long oldId = templateDTO.getId();
        templateDTO.setId(null);
        templateDTO.setParentId(parentId);
        templateDTO.setTemplateGroupId(templateGroupId);

        // 添加模板
        TemplateEntity newTemplate = templateService.add(templateDTO);

        // 记录ID映射关系
        oldToNewIdMap.put(oldId, newTemplate.getId());

        // 递归处理子模板
        if (CollUtil.isNotEmpty(templateBO.getChildren())) {
            templateBO.getChildren().forEach(child ->
                    importTemplateTree(child, newTemplate.getId(), templateGroupId, oldToNewIdMap));
        }
    }

    /**
     * 构建模板树结构
     * @param templateList 模板列表
     */
    private void buildTemplateTree(List<TemplateBO> templateList) {
        Map<Long, TemplateBO> templateMap = templateList.stream()
                .collect(Collectors.toMap(TemplateBO::getId, Function.identity()));

        // 清空原有的子模板列表
        templateList.forEach(template -> template.setChildren(new ArrayList<>()));

        Iterator<TemplateBO> iterator = templateList.iterator();
        while (iterator.hasNext()) {
            TemplateBO template = iterator.next();
            // 如果有父节点，则将当前节点添加到父节点的子节点列表中
            if (template.getParentId() != null && !template.getParentId().equals(0L)) {
                TemplateBO parent = templateMap.get(template.getParentId());
                if (parent != null) {
                    parent.getChildren().add(template);
                    // 从顶层列表中移除子节点，只保留根节点
                    iterator.remove();
                }
            }
        }
    }

    private TemplateGroupEntity selectById(Long id) {
        TemplateGroupEntity entity = templateGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "模板组", id);
        }
        return entity;
    }

    private void checkUnique(TemplateGroupDTO dto) {
        LambdaQueryWrapper<TemplateGroupEntity> queryWrapper = Wrappers.lambdaQuery(TemplateGroupEntity.class)
                .ne(Objects.nonNull(dto.getId()), TemplateGroupEntity::getId, dto.getId())
                .eq(TemplateGroupEntity::getGroupName, dto.getGroupName());

        boolean exists = templateGroupMapper.exists(queryWrapper);
        if (exists) {
            throw new BusinessException("模板组: {}, 已存在", dto.getGroupName());
        }
    }

    private void checkReference(List<Long> idList) {
        LambdaQueryWrapper<ProjectEntity> queryWrapper = Wrappers.lambdaQuery(ProjectEntity.class)
                .and(wrapper -> wrapper
                        .in(ProjectEntity::getProjectTemplateGroupId, idList)
                        .or()
                        .in(ProjectEntity::getTableTemplateGroupId, idList)
                        .or()
                        .in(ProjectEntity::getEnumTemplateGroupId, idList)
                );
        boolean exists = projectService.exists(queryWrapper);
        if (exists) {
            throw new BusinessException("模板组已被项目引用，不能删除");
        }
    }

    private LambdaQueryWrapper<TemplateGroupEntity> buildQueryWrapper(TemplateGroupEntityQuery query) {
        LambdaQueryWrapper<TemplateGroupEntity> wrapper = Wrappers.lambdaQuery(TemplateGroupEntity.class);

        //过滤字段
        wrapper.like(MybatisUtil.isNotEmpty(query.getGroupName()), TemplateGroupEntity::getGroupName, query.getGroupName());
        wrapper.eq(MybatisUtil.isNotEmpty(query.getType()), TemplateGroupEntity::getType, query.getType());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrderItemList());
        return wrapper;
    }

}