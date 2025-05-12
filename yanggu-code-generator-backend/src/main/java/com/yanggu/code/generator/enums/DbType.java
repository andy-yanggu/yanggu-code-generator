package com.yanggu.code.generator.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据库类型 枚举
 */
@Getter
@AllArgsConstructor
public enum DbType {

    MySQL("com.mysql.cj.jdbc.Driver"),

    Oracle("oracle.jdbc.driver.OracleDriver"),

    PostgreSQL("org.postgresql.Driver"),

    SQLServer("com.microsoft.sqlserver.jdbc.SQLServerDriver"),

    DM("dm.jdbc.driver.DmDriver"),

    Clickhouse("com.clickhouse.jdbc.ClickHouseDriver"),

    KingBase("com.kingbase8.Driver");

    private final String driverClass;

    public static DbType getValue(String dbType) {
        if (StrUtil.equalsAny(dbType, "MySQL")) {
            return MySQL;
        }

        if (StrUtil.equalsAny(dbType, "Oracle")) {
            return Oracle;
        }

        if (StrUtil.equalsAny(dbType, "PostgreSQL")) {
            return PostgreSQL;
        }

        if (StrUtil.equalsAny(dbType, "SQLServer", "Microsoft SQL Server")) {
            return SQLServer;
        }

        if (StrUtil.equalsAny(dbType, "DM", "DM DBMS")) {
            return DM;
        }

        if (StrUtil.equalsAny(dbType, "Clickhouse")) {
            return Clickhouse;
        }

        if (StrUtil.equalsAny(dbType, "KingBase")) {
            return KingBase;
        }
        return null;
    }

}