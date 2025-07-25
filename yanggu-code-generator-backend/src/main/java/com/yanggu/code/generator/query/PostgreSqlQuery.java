package com.yanggu.code.generator.query;

import com.yanggu.code.generator.enums.DbType;
import org.dromara.hutool.core.text.StrUtil;

/**
 * PostgreSql查询
 */
public class PostgreSqlQuery extends AbstractQuery {

    @Override
    public DbType dbType() {
        return DbType.PostgreSQL;
    }

    @Override
    public String tableSql(String tableName, Boolean isLike) {
        StringBuilder sql = new StringBuilder();
        sql.append("select t1.tablename, obj_description(relfilenode, 'pg_class') as comments from pg_tables t1, pg_class t2 ");
        sql.append("where t1.tablename not like 'pg%' and t1.tablename not like 'sql_%' and t1.tablename = t2.relname ");
        // 表名查询
        if (StrUtil.isNotBlank(tableName)) {
            if (isLike) {
                sql.append("and t1.tablename like '%").append(tableName).append("%' ")
                        .append(" order by t1.tablename acs");
            } else {
                sql.append("and t1.tablename = '").append(tableName).append("' ");
            }
        }

        return sql.toString();
    }

    @Override
    public String tableFieldsSql() {
        return "select t2.attname as columnName, pg_type.typname as dataType, col_description(t2.attrelid,t2.attnum) as columnComment,"
                + "(CASE t3.contype WHEN 'p' THEN 'PRI' ELSE '' END) as columnKey "
                + "from pg_class as t1, pg_attribute as t2 inner join pg_type on pg_type.oid = t2.atttypid "
                + "left join pg_constraint t3 on t2.attnum = t3.conkey[1] and t2.attrelid = t3.conrelid "
                + "where t1.relname = '%s' and t2.attrelid = t1.oid and t2.attnum>0";
    }

    @Override
    public String databaseName() {
        return "select current_database()";
    }

    @Override
    public String tableName() {
        return "tablename";
    }

    @Override
    public String tableComment() {
        return "comments";
    }

    @Override
    public String fieldName() {
        return "columnName";
    }

    @Override
    public String fieldType() {
        return "dataType";
    }

    @Override
    public String fieldComment() {
        return "columnComment";
    }

    @Override
    public String fieldKey() {
        return "columnKey";
    }

}
