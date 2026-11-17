package com.yanggu.code.generator.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.yanggu.code.generator.common.domain.vo.PageVO;
import com.yanggu.code.generator.domain.bo.DataSourceBO;
import com.yanggu.code.generator.domain.dto.DatasourceDTO;
import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import com.yanggu.code.generator.domain.query.DatasourceQuery;
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
     * 分页（完整VO）
     */
    PageVO<DatasourceVO> page(DatasourceQuery query);

    /**
     * 列表（完整VO）
     */
    List<DatasourceVO> list(DatasourceQuery query);

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