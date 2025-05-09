package com.yanggu.code.generator.common.mapstruct;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.common.domain.vo.PageVO;

import java.util.List;

/**
 * 将Entity分页数据转换为VO分页数据的通用接口
 *
 * @param <E> entity实体类泛型（数据库实体类）
 * @param <V> vo实体类泛型（controller返回参数）
 */
public interface PageEntityMapstruct<E, V> extends EntityToVOMapstruct<E, V> {

    /**
     * 将Entity分页数据转换为VO分页数据
     */
    default PageVO<V> entityToPageVO(PageQuery<E> entityPageQuery) {
        Long pageNum = entityPageQuery.getPageNum();
        Long pageSize = entityPageQuery.getPageSize();
        long total = entityPageQuery.getTotal();
        List<V> voList = this.entityToVO(entityPageQuery.getRecords());
        return new PageVO<>(pageNum, pageSize, total, voList);
    }

}