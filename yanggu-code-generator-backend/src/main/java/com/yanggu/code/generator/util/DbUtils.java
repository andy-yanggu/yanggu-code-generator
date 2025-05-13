package com.yanggu.code.generator.util;

import com.yanggu.code.generator.domain.bo.DataSourceBO;
import com.yanggu.code.generator.enums.DbType;
import oracle.jdbc.OracleConnection;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * DB工具类
 */
public class DbUtils {

    private static final int CONNECTION_TIMEOUTS_SECONDS = 6;

    /**
     * 获得数据库连接
     */
    public static Connection getConnection(DataSourceBO dataSource) throws Exception {
        DriverManager.setLoginTimeout(CONNECTION_TIMEOUTS_SECONDS);
        Class.forName(dataSource.getDbType().getDriverClass());

        Connection connection = DriverManager.getConnection(dataSource.getConnUrl(), dataSource.getUsername(), dataSource.getPassword());
        if (dataSource.getDbType() == DbType.Oracle) {
            ((OracleConnection) connection).setRemarksReporting(true);
        }

        return connection;
    }


}