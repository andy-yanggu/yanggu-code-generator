package com.yanggu.code.generator.domain;

import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import com.yanggu.code.generator.enums.DbType;
import com.yanggu.code.generator.query.*;
import com.yanggu.code.generator.util.DbUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据源业务实体类
 */
@Data
@Slf4j
public class GenDataSourceBO {

    /**
     * 数据源ID
     */
    private Long id;

    /**
     * 数据库类型
     */
    private DbType dbType;

    /**
     * 数据库URL
     */
    private String connUrl;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    private AbstractQuery dbQuery;

    private Connection connection;

    public GenDataSourceBO(DatasourceEntity entity) throws Exception {
        this.id = entity.getId();
        this.dbType = DbType.getValue(entity.getDbType());
        this.connUrl = entity.getConnUrl();
        this.username = entity.getUsername();
        this.password = entity.getPassword();

        if (dbType == DbType.MySQL) {
            this.dbQuery = new MySqlQuery();
        } else if (dbType == DbType.Oracle) {
            this.dbQuery = new OracleQuery();
        } else if (dbType == DbType.PostgreSQL) {
            this.dbQuery = new PostgreSqlQuery();
        } else if (dbType == DbType.SQLServer) {
            this.dbQuery = new SQLServerQuery();
        } else if (dbType == DbType.DM) {
            this.dbQuery = new DmQuery();
        } else if (dbType == DbType.Clickhouse) {
            this.dbQuery = new ClickHouseQuery();
        } else if (dbType == DbType.KingBase) {
            this.dbQuery = new KingBaseSqlQuery();
        }

        try {
            this.connection = DbUtils.getConnection(this);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public GenDataSourceBO(Connection connection) throws SQLException {
        this.id = 0L;
        this.dbType = DbType.getValue(connection.getMetaData().getDatabaseProductName());

        if (dbType == DbType.MySQL) {
            this.dbQuery = new MySqlQuery();
        } else if (dbType == DbType.Oracle) {
            this.dbQuery = new OracleQuery();
        } else if (dbType == DbType.PostgreSQL) {
            this.dbQuery = new PostgreSqlQuery();
        } else if (dbType == DbType.SQLServer) {
            this.dbQuery = new SQLServerQuery();
        } else if (dbType == DbType.DM) {
            this.dbQuery = new DmQuery();
        } else if (dbType == DbType.Clickhouse) {
            this.dbQuery = new ClickHouseQuery();
        } else if (dbType == DbType.KingBase) {
            this.dbQuery = new KingBaseSqlQuery();
        }

        this.connection = connection;
    }

    public String getDatabaseName() throws Exception {
        //设置数据库名称
        PreparedStatement preparedStatement = connection.prepareStatement(dbQuery.databaseName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString(1);
    }

}
