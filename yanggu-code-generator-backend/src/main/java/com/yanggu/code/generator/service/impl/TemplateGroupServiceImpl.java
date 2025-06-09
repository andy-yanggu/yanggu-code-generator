package com.yanggu.code.generator.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.dto.TemplateGroupDTO;
import com.yanggu.code.generator.domain.entity.ProjectEntity;
import com.yanggu.code.generator.domain.entity.TemplateEntity;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.query.TemplateGroupEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateGroupVO;
import com.yanggu.code.generator.mapper.TemplateGroupMapper;
import com.yanggu.code.generator.mapstruct.TemplateGroupMapstruct;
import com.yanggu.code.generator.service.ProjectService;
import com.yanggu.code.generator.service.TemplateGroupService;
import com.yanggu.code.generator.service.TemplateService;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.date.DateUtil;
import org.dromara.hutool.core.io.IoUtil;
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
    private TemplateService templateService;

    @Autowired
    private ProjectService projectService;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void add(TemplateGroupDTO dto) {
        TemplateGroupEntity entity = templateGroupMapstruct.dtoToEntity(dto);
        //唯一性校验等
        templateGroupMapper.insert(entity);
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(TemplateGroupDTO dto) {
        TemplateGroupEntity formEntity = templateGroupMapstruct.dtoToEntity(dto);
        TemplateGroupEntity dbEntity = selectById(dto.getId());
        //唯一性校验等
        templateGroupMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        TemplateGroupEntity dbEntity = selectById(id);
        checkReference(List.of(id));
        //删除校验和关联删除
        templateGroupMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        checkReference(idList);
        //删除校验和关联删除
        templateGroupMapper.deleteByIds(idList);
    }

    private void checkReference(List<Long> idList) {
        LambdaQueryWrapper<ProjectEntity> queryWrapper = Wrappers.lambdaQuery(ProjectEntity.class)
                .in(ProjectEntity::getProjectTemplateGroupId, idList)
                .or()
                .in(ProjectEntity::getTableTemplateGroupId, idList);
        boolean exists = projectService.exists(queryWrapper);
        if (exists) {
            throw new BusinessException("模板组已被项目引用，不能删除");
        }
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
        //List<TemplateGroupEntity> entityList = templateGroupMapper.entityList(query);
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
        TemplateGroupEntity newGroup = templateGroupMapstruct.dtoToEntity(dto);
        newGroup.setId(null);
        newGroup.setCreateTime(new Date());
        templateGroupMapper.insert(newGroup);

        Long newTemplateGroupId = newGroup.getId();
        List<TemplateEntity> templateList = templateService.selectByGroupId(oldGroupId);
        if (CollUtil.isNotEmpty(templateList)) {
            templateList.forEach(template -> {
                template.setId(null);
                template.setTemplateGroupId(newTemplateGroupId);
                templateService.save(template);
            });
        }
    }

    @Override
    public ResponseEntity<byte[]> export(List<Long> idList) {
        List<TemplateGroupEntity> list = new ArrayList<>();
        for (Long id : idList) {
            TemplateGroupEntity templateGroup = this.getById(id);
            templateGroup.setId(null);
            templateGroup.setCreateTime(null);
            templateGroup.setUpdateTime(null);
            templateGroup.setIsDelete(null);
            List<TemplateEntity> templateList = templateService.selectByGroupId(id);
            if (CollUtil.isNotEmpty(templateList)) {
                for (TemplateEntity template : templateList) {
                    template.setId(null);
                    template.setTemplateGroupId(null);
                    template.setCreateTime(null);
                    template.setUpdateTime(null);
                    template.setIsDelete(null);
                }
                templateGroup.setTemplateList(templateList);
            }
            list.add(templateGroup);
        }
        String jsonStr = JSONUtil.toJsonStr(list);

        String fileName = "templateGroup" + DateUtil.format(new Date(), PURE_DATETIME_PATTERN) + ".json";
        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(APPLICATION_OCTET_STREAM)
                .body(jsonStr.getBytes());
    }

    @Override
    public TemplateGroupEntity getById(Long id) {
        TemplateGroupEntity templateGroup = templateGroupMapper.selectById(id);
        List<TemplateEntity> templateList = templateService.selectByGroupId(id);
        templateGroup.setTemplateList(templateList);
        return templateGroup;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void importTemplateGroup(MultipartFile file) throws IOException {
        String data = IoUtil.read(file.getInputStream(), CharsetUtil.UTF_8);
        List<TemplateGroupEntity> list = JSONUtil.toList(data, TemplateGroupEntity.class);
        list.forEach(templateGroup -> {
            templateGroup.setId(null);
            templateGroup.setCreateTime(new Date());
            templateGroupMapper.insert(templateGroup);

            List<TemplateEntity> templateList = templateGroup.getTemplateList();
            if (CollUtil.isNotEmpty(templateList)) {
                templateList.forEach(template -> {
                    template.setId(null);
                    template.setTemplateGroupId(templateGroup.getId());
                    template.setCreateTime(new Date());
                    templateService.save(template);
                });
            }
        });
    }

    /**
     * 批量查询
     */
    @Override
    public List<TemplateGroupVO> detailList(List<Long> idList) {
        List<TemplateGroupEntity> entityList = templateGroupMapper.selectByIds(idList);
        return templateGroupMapstruct.entityToVO(entityList);
    }

    private TemplateGroupEntity selectById(Long id) {
        TemplateGroupEntity entity = templateGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "模板组", id);
        }
        return entity;
    }

    private LambdaQueryWrapper<TemplateGroupEntity> buildQueryWrapper(TemplateGroupEntityQuery query) {
        LambdaQueryWrapper<TemplateGroupEntity> wrapper = Wrappers.lambdaQuery(TemplateGroupEntity.class);

        //过滤字段
        wrapper.like(MybatisUtil.isNotEmpty(query.getGroupName()), TemplateGroupEntity::getGroupName, query.getGroupName());
        wrapper.eq(MybatisUtil.isNotEmpty(query.getType()), TemplateGroupEntity::getType, query.getType());

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrders());
        return wrapper;
    }

}