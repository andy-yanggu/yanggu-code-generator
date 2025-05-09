package com.yanggu.code.generator.common.mapstruct;

import com.yanggu.code.generator.common.domain.query.PageQuery;
import com.yanggu.code.generator.common.domain.vo.PageVO;

/**
 * 将VO分页数据转换为VO分页数据的通用接口
 *
 * @param <V> vo实体类泛型（controller返回参数）
 */
public interface PageVOMapstruct<V> extends PageVOOtherMapstruct {

    /**
     * 将VO分页数据转换成PageVO分页数据
     */
    default PageVO<V> voToPageVO(PageQuery<V> voPageQuery) {
        return toPageVO(voPageQuery);
    }

}