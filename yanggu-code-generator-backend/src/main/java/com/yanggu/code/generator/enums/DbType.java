package com.yanggu.code.generator.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dromara.hutool.core.text.StrUtil;

/**
 * 数据库类型 枚举
 */
@Getter
@AllArgsConstructor
public enum DbType {

    MySQL("MySQL", "com.mysql.cj.jdbc.Driver"),

    Oracle("Oracle", "oracle.jdbc.driver.OracleDriver"),

    PostgreSQL("PostgreSQL", "org.postgresql.Driver"),

    SQLServer("SQLServer", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),

    DM("DM", "dm.jdbc.driver.DmDriver"),

    Clickhouse("Clickhouse", "com.clickhouse.jdbc.ClickHouseDriver"),

    KingBase("KingBase", "com.kingbase8.Driver");

    @EnumValue
    @JsonValue
    private final String code;

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