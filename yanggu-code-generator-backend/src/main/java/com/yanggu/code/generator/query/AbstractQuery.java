package com.yanggu.code.generator.query;


import cn.hutool.v7.core.collection.CollUtil;
import cn.hutool.v7.core.reflect.ClassUtil;
import cn.hutool.v7.core.reflect.ConstructorUtil;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.common.response.ResultEnum;
import com.yanggu.code.generator.enums.DbType;

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
            //构建/部署缺陷，不是客户端错误：保留 500，但带出扫描包路径供定位
            throw new BusinessException("扫描未找到 AbstractQuery 实现类, 扫描包: {}", classPackage);
        }
        for (Class<?> clazz : classes) {
            AbstractQuery query = (AbstractQuery) ConstructorUtil.newInstance(clazz);
            AbstractQuery put = MAP.put(query.dbType(), query);
            if (put != null) {
                //同上：方言重复注册属构建缺陷，保留 500
                throw new BusinessException("AbstractQuery实现类重复: {}", query.dbType());
            }
        }
    }

    public static AbstractQuery getQuery(DbType dbType) {
        AbstractQuery abstractQuery = MAP.get(dbType);
        if (abstractQuery == null) {
            //数据源记录的 db_type 解析不出枚举时为 null，属可修正的配置/入参错误，要带出具体类型
            throw new BusinessException(ResultEnum.PARAM_ERR.getCode(), "未找到数据库类型 {} 对应的查询实现",
                    dbType == null ? "null" : dbType.getCode());
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
