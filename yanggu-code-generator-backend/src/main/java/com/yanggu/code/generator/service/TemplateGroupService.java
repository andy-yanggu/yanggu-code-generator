package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.dto.TemplateGroupCopyDTO;
import com.yanggu.code.generator.domain.dto.TemplateGroupDTO;
import com.yanggu.code.generator.domain.entity.TemplateGroupEntity;
import com.yanggu.code.generator.domain.query.TemplateGroupDetailQuery;
import com.yanggu.code.generator.domain.query.TemplateGroupQuery;
import com.yanggu.code.generator.domain.vo.CascaderDataVO;
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
     * 分页（完整VO，含关联字段）
     */
    PageVO<TemplateGroupVO> page(TemplateGroupQuery query);

    /**
     * 列表（完整VO，含关联字段及属性清单）
     */
    List<TemplateGroupVO> list(TemplateGroupQuery query);

    /**
     * 复制模板组
     */
    void copy(TemplateGroupCopyDTO dto);

    /**
     * 导出模板组
     */
    ResponseEntity<byte[]> export(List<Long> idList);

    /**
     * 导入模板组
     */
    void importTemplateGroup(MultipartFile file) throws IOException;

    /**
     * 根据ID查询详情（模板列表、属性列表按查询参数开关按需装配）
     */
    TemplateGroupEntity getDetailById(TemplateGroupDetailQuery query);

    /**
     * 获取级联数据：按模板组类型分发到对应产物策略，列出该模板组可关联的候选产物
     */
    List<CascaderDataVO> cascaderData(Integer templateGroupType, Long templateGroupId);

}