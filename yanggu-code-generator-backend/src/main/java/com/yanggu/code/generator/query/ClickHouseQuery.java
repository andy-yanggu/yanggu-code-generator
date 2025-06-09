
package com.yanggu.code.generator.query;

import com.yanggu.code.generator.enums.DbType;
import org.dromara.hutool.core.text.StrUtil;

/**
 * ClickHouse查询
 */
public class ClickHouseQuery extends AbstractQuery {

    @Override
    public DbType dbType() {
        return DbType.Clickhouse;
    }

    @Override
    public String tableFieldsSql() {
        return "select * from system.columns where table='%s'";
    }

    @Override
    public String tableSql(String tableName, Boolean isLike) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM system.tables WHERE 1=1 ");

        // 表名查询
        if (StrUtil.isNotBlank(tableName)) {
            if (isLike) {
                sql.append("and name like '").append(tableName).append("%' ")
                        .append(" order by name asc");
            } else {
                sql.append("and name = '").append(tableName).append("' ");
            }
        }
        return sql.toString();
    }

    @Override
    public String tableName() {
        return "name";
    }

    @Override
    public String tableComment() {
        return "comment";
    }

    @Override
    public String fieldName() {
        return "name";
    }


    @Override
    public String fieldType() {
        return "type";
    }


    @Override
    public String fieldComment() {
        return "comment";
    }


    @Override
    public String fieldKey() {
        return "is_in_primary_key";
    }

    @Override
    public String databaseName() {
        return "select currentDatabase()";
    }

}
