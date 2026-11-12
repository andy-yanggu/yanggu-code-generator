package com.yanggu.code.generator.common.mapstruct;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
    default PageVO<V> entityToPageVO(IPage<E> page) {
        Long pageNum = page.getCurrent();
        Long pageSize = page.getSize();
        long total = page.getTotal();
        List<V> voList = this.entityToVO(page.getRecords());
        return new PageVO<>(pageNum, pageSize, total, voList);
    }

}