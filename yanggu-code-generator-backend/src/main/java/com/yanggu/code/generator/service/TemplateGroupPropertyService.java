package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyDTO;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyUpdateOrderDTO;
import com.yanggu.code.generator.domain.dto.TemplateGroupPropertyUpdateRequiredDTO;
import com.yanggu.code.generator.domain.entity.TemplateGroupPropertyEntity;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupPropertyVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateGroupPropertyVO;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 模板组属性服务层
 */
public interface TemplateGroupPropertyService extends IService<TemplateGroupPropertyEntity> {

    /**
     * 新增
     */
    TemplateGroupPropertyEntity add(TemplateGroupPropertyDTO dto);

    /**
     * 修改
     */
    void update(TemplateGroupPropertyDTO dto);

    /**
     * 删除
     */
    void delete(Long id);

    /**
     * 批量删除
     */
    void deleteList(List<Long> idList);

    /**
     * 详情
     */
    TemplateGroupPropertyVO detail(Long id);

    /**
     * 详情列表
     */
    List<TemplateGroupPropertyVO> detailList(List<Long> idList);

    /**
     * 简单分页
     */
    PageVO<TemplateGroupPropertyVO> entityPage(TemplateGroupPropertyEntityQuery query);

    /**
     * 简单列表
     */
    List<TemplateGroupPropertyVO> entityList(TemplateGroupPropertyEntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<TemplateGroupPropertyVO> voPage(TemplateGroupPropertyVOQuery query);

    /**
     * 复杂列表
     */
    List<TemplateGroupPropertyVO> voList(TemplateGroupPropertyVOQuery query);

    /**
     * 导出
     */
    ResponseEntity<byte[]> export(List<Long> idList);

    /**
     * 导入
     */
    void importTemplateGroupProperty(MultipartFile file, Long templateGroupId) throws IOException;

    /**
     * 根据模板组ID删除
     */
    default void deleteByGroupId(List<Long> idList) {
        LambdaQueryWrapper<TemplateGroupPropertyEntity> queryWrapper = Wrappers.lambdaQuery(TemplateGroupPropertyEntity.class)
                .in(TemplateGroupPropertyEntity::getTemplateGroupId, idList);
        this.remove(queryWrapper);
    }

    /**
     * 根据模板组ID查询
     */
    default List<TemplateGroupPropertyEntity> listByGroupId(List<Long> groupIdList) {
        LambdaQueryWrapper<TemplateGroupPropertyEntity> wrapper = Wrappers.lambdaQuery(TemplateGroupPropertyEntity.class)
                .in(TemplateGroupPropertyEntity::getTemplateGroupId, groupIdList)
                .orderByAsc(TemplateGroupPropertyEntity::getPropOrder);
        return this.list(wrapper);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    default void changeRequired(TemplateGroupPropertyUpdateRequiredDTO dto) {
        TemplateGroupPropertyEntity updateEntity = new TemplateGroupPropertyEntity();
        updateEntity.setId(dto.getId());
        updateEntity.setRequired(dto.getRequired());
        this.updateById(updateEntity);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    default void changeOrder(TemplateGroupPropertyUpdateOrderDTO dto) {
        TemplateGroupPropertyEntity updateEntity = new TemplateGroupPropertyEntity();
        updateEntity.setId(dto.getId());
        updateEntity.setPropOrder(dto.getPropOrder());
        this.updateById(updateEntity);
    }

}