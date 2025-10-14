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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.mybatis.util.MybatisUtil;
import com.yanggu.code.generator.domain.bo.TemplateGroupPropertyBO;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyDTO;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateGroupPropertyVO;
import com.yanggu.code.generator.mapper.TemplateGroupPropertyMapper;
import com.yanggu.code.generator.mapstruct.TemplateGroupPropertyMapstruct;
import com.yanggu.code.generator.service.TemplateGroupPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static cn.hutool.v7.core.date.DateFormatPool.PURE_DATETIME_PATTERN;
import static com.yanggu.code.generator.common.response.ResultEnum.DATA_NOT_EXIST;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

/**
 * 模板组属性Service实现类
 */
@Service
public class TemplateGroupPropertyServiceImpl extends ServiceImpl<TemplateGroupPropertyMapper, TemplateGroupPropertyEntity> implements TemplateGroupPropertyService {

    @Autowired
    private TemplateGroupPropertyMapper templateGroupPropertyMapper;

    @Autowired
    private TemplateGroupPropertyMapstruct templateGroupPropertyMapstruct;

    /**
     * 新增
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public TemplateGroupPropertyEntity add(TemplateGroupPropertyDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        TemplateGroupPropertyEntity entity = templateGroupPropertyMapstruct.dtoToEntity(dto);
        templateGroupPropertyMapper.insert(entity);
        return entity;
    }

    /**
     * 修改
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void update(TemplateGroupPropertyDTO dto) {
        //唯一性校验等
        checkUnique(dto);
        TemplateGroupPropertyEntity formEntity = templateGroupPropertyMapstruct.dtoToEntity(dto);
        TemplateGroupPropertyEntity dbEntity = selectById(dto.getId());
        templateGroupPropertyMapper.updateById(formEntity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void delete(Long id) {
        deleteList(List.of(id));
    }

    /**
     * 批量删除
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteList(List<Long> idList) {
        List<TemplateGroupPropertyEntity> dbEntityList = templateGroupPropertyMapper.selectByIds(idList);
        //删除校验
        //checkReference(dbEntityList);
        //关联删除
        templateGroupPropertyMapper.deleteByIds(idList);
    }

    /**
     * 详情
     */
    @Override
    public TemplateGroupPropertyVO detail(Long id) {
        TemplateGroupPropertyEntity dbEntity = selectById(id);
        return templateGroupPropertyMapstruct.entityToVO(dbEntity);
    }

    /**
     * 详情列表
     */
    @Override
    public List<TemplateGroupPropertyVO> detailList(List<Long> idList) {
        List<TemplateGroupPropertyEntity> entityList = templateGroupPropertyMapper.selectByIds(idList);
        return templateGroupPropertyMapstruct.entityToVO(entityList);
    }

    /**
     * 简单分页
     */
    @Override
    public PageVO<TemplateGroupPropertyVO> entityPage(TemplateGroupPropertyEntityQuery query) {
        //简单sql使用QueryWrapper
        templateGroupPropertyMapper.selectPage(query, buildQueryWrapper(query));
        //较复杂sql，使用xml
        //templateGroupPropertyMapper.entityPage(query);
        return templateGroupPropertyMapstruct.entityToPageVO(query);
    }

    /**
     * 简单列表
     */
    @Override
    public List<TemplateGroupPropertyVO> entityList(TemplateGroupPropertyEntityQuery query) {
        query.setPageSize(-1L);
        //简单sql使用QueryWrapper
        List<TemplateGroupPropertyEntity> entityList = templateGroupPropertyMapper.selectList(buildQueryWrapper(query));
        //复杂sql，使用xml
        //List<TemplateGroupPropertyEntity> entityList = templateGroupPropertyMapper.entityList(query);
        return templateGroupPropertyMapstruct.entityToVO(entityList);
    }

    /**
     * 复杂分页
     */
    @Override
    public PageVO<TemplateGroupPropertyVO> voPage(TemplateGroupPropertyVOQuery query) {
        templateGroupPropertyMapper.voPage(query);
        return templateGroupPropertyMapstruct.voToPageVO(query);
    }

    /**
     * 复杂列表
     */
    @Override
    public List<TemplateGroupPropertyVO> voList(TemplateGroupPropertyVOQuery query) {
        //查询全部数据
        query.setPageSize(-1L);
        return templateGroupPropertyMapper.voList(query);
    }

    @Override
    public ResponseEntity<byte[]> export(List<Long> idList) {
        List<TemplateGroupPropertyEntity> list = idList.stream()
                .map(this::selectById)
                .toList();

        //转换成导出对象
        List<TemplateGroupPropertyBO> boList = templateGroupPropertyMapstruct.entityToBO(list);

        String jsonStr = JSONUtil.toJsonStr(boList);

        String fileName = "template_group_property_" + DateUtil.format(new Date(), PURE_DATETIME_PATTERN) + ".json";
        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, HttpHeaderUtil.createAttachmentDisposition(fileName, CharsetUtil.UTF_8))
                .contentType(APPLICATION_OCTET_STREAM)
                .body(jsonStr.getBytes());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void importTemplateGroupProperty(MultipartFile file, Long templateGroupId) throws IOException {
        String data = IoUtil.read(file.getInputStream(), CharsetUtil.UTF_8);
        if (StrUtil.isBlank(data)) {
            throw new BusinessException("导入数据不能为空");
        }
        List<TemplateGroupPropertyBO> list = JSONUtil.toList(data, TemplateGroupPropertyBO.class);
        if (CollUtil.isEmpty(list)) {
            throw new BusinessException("导入数据不能为空");
        }

        list.forEach(bo -> {
            TemplateGroupPropertyDTO dto = templateGroupPropertyMapstruct.boToDTO(bo);
            dto.setTemplateGroupId(templateGroupId);
            add(dto);
        });
    }


    /**
     * ID查询
     */
    private TemplateGroupPropertyEntity selectById(Long id) {
        TemplateGroupPropertyEntity entity = templateGroupPropertyMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(DATA_NOT_EXIST, "模板组属性", id);
        }
        return entity;
    }

    /**
     * 唯一性校验
     */
    private void checkUnique(TemplateGroupPropertyDTO dto) {
        LambdaQueryWrapper<TemplateGroupPropertyEntity> wrapper = Wrappers.lambdaQuery(TemplateGroupPropertyEntity.class);

        wrapper.ne(Objects.nonNull(dto.getId()), TemplateGroupPropertyEntity::getId, dto.getId());
        wrapper.eq(TemplateGroupPropertyEntity::getTemplateGroupId, dto.getTemplateGroupId());
        wrapper.and(temp -> temp
                .eq(TemplateGroupPropertyEntity::getPropTitle, dto.getPropTitle())
                .or()
                .eq(TemplateGroupPropertyEntity::getPropKey, dto.getPropKey())
        );

        boolean exists = templateGroupPropertyMapper.exists(wrapper);
        if (exists) {
            throw new BusinessException("模板组属性标题或者属性键已存在");
        }
    }

    /**
     * 校验能否被删除
     */
    private void checkReference(List<TemplateGroupPropertyEntity> dbEntityList) {
        //TODO 校验是否被引用
        for (TemplateGroupPropertyEntity dbEntity : dbEntityList) {

        }
    }

    private LambdaQueryWrapper<TemplateGroupPropertyEntity> buildQueryWrapper(TemplateGroupPropertyEntityQuery query) {
        LambdaQueryWrapper<TemplateGroupPropertyEntity> wrapper = Wrappers.lambdaQuery(TemplateGroupPropertyEntity.class);

        //排序字段
        MybatisUtil.orderBy(wrapper, query.getOrderItemList());
        return wrapper;
    }

}
