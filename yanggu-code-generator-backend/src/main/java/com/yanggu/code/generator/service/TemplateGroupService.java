package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.dto.TemplateGroupDTO;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.query.TemplateGroupEntityQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupVOQuery;
import com.yanggu.code.generator.domain.vo.TemplateGroupVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 模板组服务层
 */
public interface TemplateGroupService extends IService<TemplateGroupEntity> {

    /**
     * 新增
     */
    TemplateGroupEntity add(TemplateGroupDTO dto);

    /**
     * 修改
     */
    void update(TemplateGroupDTO dto);

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
    TemplateGroupVO detail(Long id);

    /**
     * 批量查询
     */
    List<TemplateGroupVO> detailList(List<Long> idList);

    /**
     * 简单分页
     */
    PageVO<TemplateGroupVO> entityPage(TemplateGroupEntityQuery query);

    /**
     * 简单列表
     */
    List<TemplateGroupVO> entityList(TemplateGroupEntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<TemplateGroupVO> voPage(TemplateGroupVOQuery query);

    /**
     * 复杂列表
     */
    List<TemplateGroupVO> voList(TemplateGroupVOQuery query);

    /**
     * 复制模板组
     */
    void copy(TemplateGroupDTO dto);

    /**
     * 导出模板组
     */
    ResponseEntity<byte[]> export(List<Long> idList);

    /**
     * 导入模板组
     */
    void importTemplateGroup(MultipartFile file) throws IOException;

    /**
     * 根据ID查询（包含所有模板）
     */
    TemplateGroupEntity getById(Long id);

}