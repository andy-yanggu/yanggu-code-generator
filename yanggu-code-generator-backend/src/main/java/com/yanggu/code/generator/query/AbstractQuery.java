package com.yanggu.code.generator.query;


import com.yanggu.code.generator.enums.DbType;
import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.reflect.ClassUtil;
import org.dromara.hutool.core.reflect.ConstructorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 数据库Query抽象类
 */
public abstract class AbstractQuery {

    private static final Map<DbType, AbstractQuery> MAP = new HashMap<>();

    static {
        String classPackage = ClassUtil.getPackage(AbstractQuery.class);
        Set<Class<?>> classes = ClassUtil.scanPackageBySuper(classPackage, AbstractQuery.class);
        if (CollUtil.isEmpty(classes)) {
            throw new RuntimeException("未找到AbstractQuery实现类");
        }
        for (Class<?> clazz : classes) {
            AbstractQuery query = (AbstractQuery) ConstructorUtil.newInstance(clazz);
            AbstractQuery put = MAP.put(query.dbType(), query);
            if (put != null) {
                throw new RuntimeException("AbstractQuery实现类重复: " + query.dbType());
            }
        }
    }

    public static AbstractQuery getQuery(DbType dbType) {
        AbstractQuery abstractQuery = MAP.get(dbType);
        if (abstractQuery == null) {
            throw new RuntimeException("未找到AbstractQuery实现类");
        }
        return abstractQuery;
    }

    /**
     * 数据库类型
     */
    public abstract DbType dbType();

    /**
     * 表信息查询 SQL
     */
    public abstract String tableSql(String tableName, Boolean isLike);

    /**
     * 当前数据库名称
     */
    public abstract String databaseName();

    /**
     * 表名称
     */
    public abstract String tableName();

    /**
     * 表注释
     */
    public abstract String tableComment();

    /**
     * 表字段信息查询 SQL
     */
    public abstract String tableFieldsSql();

    /**
     * 字段名称
     */
    public abstract String fieldName();

    /**
     * 字段类型
     */
    public abstract String fieldType();

    /**
     * 字段注释
     */
    public abstract String fieldComment();

    /**
     * 主键字段
     */
    public abstract String fieldKey();

}
