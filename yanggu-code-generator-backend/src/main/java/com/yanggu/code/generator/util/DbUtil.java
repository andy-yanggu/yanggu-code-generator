package com.yanggu.code.generator.util;

import com.yanggu.code.generator.domain.bo.DataSourceBO;
import com.yanggu.code.generator.enums.DbType;
import com.yanggu.code.generator.query.AbstractQuery;
import lombok.experimental.UtilityClass;
import oracle.jdbc.OracleConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * DB工具类
 */
@UtilityClass
public class DbUtil {

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

    /**
     * 获取数据库名称
     */
    public static String getDatabaseName(DataSourceBO dataSource) throws Exception {
        Connection connection = dataSource.getConnection();
        AbstractQuery dbQuery = dataSource.getDbQuery();
        PreparedStatement preparedStatement = connection.prepareStatement(dbQuery.databaseName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString(1);
    }

}