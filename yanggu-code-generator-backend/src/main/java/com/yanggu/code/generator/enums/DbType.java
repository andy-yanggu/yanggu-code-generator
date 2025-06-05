package com.yanggu.code.generator.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据库类型枚举
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

    KingBase("KingBase", "com.kingbase8.Driver"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String driverClass;

}