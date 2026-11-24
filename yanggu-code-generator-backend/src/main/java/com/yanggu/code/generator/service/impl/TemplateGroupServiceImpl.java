package com.yanggu.code.generator.service.impl;


import cn.hutool.v7.core.collection.CollUtil;
import cn.hutool.v7.core.date.DateUtil;
import cn.hutool.v7.core.io.IoUtil;
import cn.hutool.v7.core.text.StrUtil;
import cn.hutool.v7.core.util.CharsetUtil;
import cn.hutool.v7.http.meta.HttpHeaderUtil;
import cn.hutool.v7.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.bo.TemplateBO;
import com.yanggu.code.generator.domain.bo.TemplateGroupBO;
import com.yanggu.code.generator.domain.bo.TemplateGroupPropertyBO;
import com.yanggu.code.generator.domain.dto.TemplateDTO;
import com.yanggu.code.generator.domain.dto.TemplateGroupCopyDTO;
import com.yanggu.code.generator.domain.dto.TemplateGroupDTO;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyDTO;
import com.yanggu.code.generator.domain.entity.*;
import com.yanggu.code.generator.domain.query.TemplateGroupDetailQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupQuery;
import com.yanggu.code.generator.domain.vo.CascaderDataVO;
import com.yanggu.code.generator.domain.vo.TemplateGroupPropertyVO;
import com.yanggu.code.generator.domain.vo.TemplateGroupVO;
import com.yanggu.code.generator.mapper.TemplateGroupMapper;
import com.yanggu.code.generator.mapstruct.TemplateGroupMapstruct;
import com.yanggu.code.generator.mapstruct.TemplateGroupPropertyMapstruct;
import com.yanggu.code.generator.mapstruct.TemplateMapstruct;
import com.yanggu.code.generator.service.*;
import com.yanggu.code.generator.service.support.ProductGenerators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.hutool.v7.core.date.DateFormatPool.PURE_DATETIME_PATTERN;
import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;
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

    @Autowired
    private TemplateGroupPropertyService templateGroupPropertyService;

    @Autowired
    private TemplateGroupPropertyMapstruct templateGroupPropertyMapstruct;

    @Autowired
    private ProductGenerators productGenerators;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TemplateGroupEntity dbEntity = selectById(id);
        deleteList(List.of(id));
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteList(List<Long> idList) {
        //删除校验
        checkReference(idList);
        //关联删除
        templateGroupMapper.deleteByIds(idList);
        templateService.deleteByGroupId(idList);
        templateGroupPropertyService.deleteByGroupId(idList);
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
     * 分页（完整VO，含关联字段）
     */
    @Override
    public PageVO<TemplateGroupVO> page(TemplateGroupQuery query) {
        //需要JOIN关联字段，使用xml
        Page<TemplateGroupVO> page = query.toPage();
        templateGroupMapper.voPage(page, query);
        return templateGroupMapstruct.voToPageVO(page);
    }

    /**
     * 列表（完整VO，含关联字段及属性清单）
     */
    @Override
    public List<TemplateGroupVO> list(TemplateGroupQuery query) {
        //需要JOIN关联字段，使用xml
        List<TemplateGroupVO> voList = templateGroupMapper.voList(query);
        fillPropertyList(voList);
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copy(TemplateGroupCopyDTO dto) {
        // 源模板组ID由前端单独传入：id 字段不用来区分新旧，新增的那条记录靠 id 置空走 insert
        Long oldGroupId = dto.getOldGroupId();
        dto.setId(null);

        //新增模板组
        TemplateGroupEntity add = add(dto);
        Long newTemplateGroupId = add.getId();

        // 复制需要连带模板树与属性清单，两个开关均开
        TemplateGroupEntity oldTemplateGroup = getDetailById(TemplateGroupDetailQuery.of(oldGroupId, true, true));

        // 先保存所有顶级模板（parentId为null的模板）
        List<TemplateEntity> templateList = oldTemplateGroup.getTemplateList();
        List<TemplateEntity> rootTemplates = templateList.stream()
                .filter(template -> template.getParentId().equals(0L))
                .toList();

        // 递归复制模板树
        rootTemplates.forEach(rootTemplate -> copyTemplateTree(rootTemplate, 0L, newTemplateGroupId, templateList));

        // 复制模板组属性
        List<TemplateGroupPropertyEntity> propertyList = oldTemplateGroup.getPropertyList();
        if (CollUtil.isNotEmpty(propertyList)) {
            propertyList.forEach(temp -> {
                temp.setTemplateGroupId(newTemplateGroupId);
                temp.setId(null);
                templateGroupPropertyService.save(temp);
            });
        }
    }

    @Override
    public ResponseEntity<byte[]> export(List<Long> idList) {
        List<TemplateGroupEntity> list = idList.stream()
                .map(id -> getDetailById(TemplateGroupDetailQuery.of(id, true, true)))
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

        String fileName = "template_group_" + DateUtil.format(LocalDateTime.now(), PURE_DATETIME_PATTERN) + ".json";
        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, HttpHeaderUtil.createAttachmentDisposition(fileName, CharsetUtil.UTF_8))
                .contentType(APPLICATION_OCTET_STREAM)
                .body(jsonStr.getBytes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
                templateList.forEach(template -> importTemplateTree(template, 0L, groupEntity.getId()));
            }

            List<TemplateGroupPropertyBO> propertyList = templateGroup.getPropertyList();
            if (CollUtil.isNotEmpty(propertyList)) {
                propertyList.forEach(property -> {
                    TemplateGroupPropertyDTO propertyDTO = templateGroupPropertyMapstruct.boToDTO(property);
                    propertyDTO.setTemplateGroupId(groupEntity.getId());
                    templateGroupPropertyService.add(propertyDTO);
                });
            }
        });
    }

    @Override
    public TemplateGroupEntity getDetailById(TemplateGroupDetailQuery query) {
        Long id = query.getId();
        TemplateGroupEntity templateGroup = selectById(id);
        // 开关未显式赋值时按不装配处理，避免 Boolean 拆箱 NPE
        if (Boolean.TRUE.equals(query.getIsIncludeTemplateList())) {
            List<TemplateEntity> templateList = templateService.selectByGroupId(id);
            templateGroup.setTemplateList(templateList);
        }
        if (Boolean.TRUE.equals(query.getIsIncludePropertyList())) {
            List<TemplateGroupPropertyEntity> propertyList = templateGroupPropertyService.listByGroupId(List.of(id));
            templateGroup.setPropertyList(propertyList);
        }
        return templateGroup;
    }

    /**
     * 级联数据分发到对应产物策略，类型差异全部收口在 ProductGenerator 实现内
     */
    @Override
    public List<CascaderDataVO> cascaderData(Integer templateGroupType, Long templateGroupId) {
        return productGenerators.getByCode(templateGroupType).cascaderData(templateGroupId);
    }

    /**
     * 填充模板组属性清单
     */
    private void fillPropertyList(List<TemplateGroupVO> voList) {
        if (CollUtil.isEmpty(voList)) {
            return;
        }
        List<Long> groupIdList = voList.stream()
                .map(TemplateGroupVO::getId)
                .toList();
        Map<Long, List<TemplateGroupPropertyEntity>> collectMap = templateGroupPropertyService.listByGroupId(groupIdList).stream()
                .collect(Collectors.groupingBy(TemplateGroupPropertyEntity::getTemplateGroupId));

        voList.forEach(vo -> {
            List<TemplateGroupPropertyEntity> propertyList = collectMap.get(vo.getId());
            List<TemplateGroupPropertyVO> propertyVOList = templateGroupPropertyMapstruct.entityToVO(propertyList);
            vo.setPropertyList(propertyVOList);
        });
    }

    /**
     * 递归复制模板树
     *
     * @param sourceTemplate     源模板
     * @param newParentId        新的父模板ID
     * @param newTemplateGroupId 新模板组ID
     * @param templateList       原模板映射
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
     *
     * @param templateBO      模板BO
     * @param parentId        父模板ID
     * @param templateGroupId 模板组ID
     */
    private void importTemplateTree(TemplateBO templateBO, Long parentId, Long templateGroupId) {
        // 创建模板DTO
        TemplateDTO templateDTO = templateMapstruct.boToDTO(templateBO);
        Long oldId = templateDTO.getId();
        templateDTO.setId(null);
        templateDTO.setParentId(parentId);
        templateDTO.setTemplateGroupId(templateGroupId);

        // 添加模板
        TemplateEntity newTemplate = templateService.add(templateDTO);


        // 递归处理子模板
        if (CollUtil.isNotEmpty(templateBO.getChildren())) {
            templateBO.getChildren().forEach(child ->
                    importTemplateTree(child, newTemplate.getId(), templateGroupId));
        }
    }

    /**
     * 构建模板树结构
     *
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

}