package com.yanggu.code.generator.query;

import com.yanggu.code.generator.enums.DbType;
import org.dromara.hutool.core.text.StrUtil;

/**
 * MySQL查询
 */
public class MySqlQuery extends AbstractQuery {

    @Override
    public DbType dbType() {
        return DbType.MySQL;
    }

    @Override
    public String tableSql(String tableName, Boolean isLike) {
        StringBuilder sql = new StringBuilder();
        sql.append("select table_name, table_comment from information_schema.tables ");
        sql.append("where table_schema = (select database()) ");
        // 表名查询
        if (StrUtil.isNotBlank(tableName)) {
            if (isLike) {
                sql.append("and table_name like '%").append(tableName).append("%' ")
                        .append(" order by table_name asc");
            } else {
                sql.append("and table_name = '").append(tableName).append("' ");
            }
        }

        return sql.toString();
    }

    @Override
    public String databaseName() {
        return "select database()";
    }

    @Override
    public String tableName() {
        return "table_name";
    }

    @Override
    public String tableComment() {
        return "table_comment";
    }

    @Override
    public String tableFieldsSql() {
        return "select column_name, data_type, column_comment, column_key from information_schema.columns "
                + "where table_name = '%s' and table_schema = (select database()) order by ordinal_position";
    }

    @Override
    public String fieldName() {
        return "column_name";
    }

    @Override
    public String fieldType() {
        return "data_type";
    }

    @Override
    public String fieldComment() {
        return "column_comment";
    }

    @Override
    public String fieldKey() {
        return "column_key";
    }

}
