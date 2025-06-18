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
import org.dromara.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
        if (CollUtil.isNotEmpty(templateList)) {
            templateList.forEach(template -> {
                TemplateDTO templateDTO = templateMapstruct.entityToDTO(template);
                templateDTO.setId(null);
                templateDTO.setTemplateGroupId(newTemplateGroupId);

                //新增模板
                templateService.add(templateDTO);
            });
        }
    }

    @Override
    public ResponseEntity<byte[]> export(List<Long> idList) {
        List<TemplateGroupEntity> list = idList.stream()
                .map(this::getById)
                .toList();

        //转换成导出对象
        List<TemplateGroupBO> boList = templateGroupMapstruct.entityToBO(list);
        String jsonStr = JSONUtil.toJsonStr(boList);

        String fileName = "TemplateGroup_" + DateUtil.format(new Date(), PURE_DATETIME_PATTERN) + ".json";
        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, "attachment; filename=" + fileName)
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
                templateList.forEach(template -> {
                    TemplateDTO templateDTO = templateMapstruct.boToDTO(template);
                    templateDTO.setTemplateGroupId(groupEntity.getId());
                    templateService.add(templateDTO);
                });
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