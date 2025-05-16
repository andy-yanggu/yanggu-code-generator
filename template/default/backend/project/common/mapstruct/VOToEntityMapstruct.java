package ${projectPackage}.${projectNameDot}.common.mapstruct;

import org.dromara.hutool.core.collection.CollUtil;

import java.util.Collections;
import java.util.List;

/**
 * 将VO转换成Entity的通用接口
 *
 * @param <V> vo实体类泛型（controller返回参数）
 * @param <E> entity实体类泛型（数据库实体类）
 */
public interface VOToEntityMapstruct<V, E> {

    /**
     * 将VO转换成Entity
     */
    E voToEntity(V vo);

    /**
     * 将Entity列表转换成VO列表
     */
    default List<E> voToEntity(List<V> voList) {
        if (CollUtil.isEmpty(voList)) {
            return Collections.emptyList();
        }
        return voList.stream().map(this::voToEntity).toList();
    }

}