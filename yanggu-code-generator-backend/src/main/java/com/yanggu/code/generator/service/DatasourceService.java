package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.bo.DataSourceBO;
import com.yanggu.code.generator.domain.dto.DatasourceDTO;
import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import com.yanggu.code.generator.domain.query.DatasourceEntityQuery;
import com.yanggu.code.generator.domain.query.DatasourceVOQuery;
import com.yanggu.code.generator.domain.vo.DatasourceTestVO;
import com.yanggu.code.generator.domain.vo.DatasourceVO;

import java.util.List;

/**
 * 数据源服务层
 */
public interface DatasourceService extends IService<DatasourceEntity> {

    /**
     * 新增
     */
    void add(DatasourceDTO dto);

    /**
     * 修改
     */
    void update(DatasourceDTO dto);

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
    DatasourceVO detail(Long id);

    /**
     * 批量查询
     */
    List<DatasourceVO> detailList(List<Long> idList);

    /**
     * 简单分页
     */
    PageVO<DatasourceVO> entityPage(DatasourceEntityQuery query);

    /**
     * 简单列表
     */
    List<DatasourceVO> entityList(DatasourceEntityQuery query);

    /**
     * 复杂分页
     */
    PageVO<DatasourceVO> voPage(DatasourceVOQuery query);

    /**
     * 复杂列表
     */
    List<DatasourceVO> voList(DatasourceVOQuery query);

    /**
     * 根据数据源ID，获取数据源
     *
     * @param datasourceId 数据源ID
     */
    DataSourceBO get(Long datasourceId) throws Exception;

    /**
     * 测试数据源
     */
    DatasourceTestVO test(Long id) throws Exception;

    /**
     * 获取数据库产品名，如：MySQL
     *
     * @param datasourceId 数据源ID
     * @return 返回产品名
     */
    String getDatabaseProductName(Long datasourceId);

}