package com.yanggu.code.generator.util;

import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.config.GeneratorConfig;
import com.yanggu.code.generator.domain.bo.DataSourceBO;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.vo.TableImportVO;
import com.yanggu.code.generator.enums.DbType;
import com.yanggu.code.generator.query.AbstractQuery;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleConnection;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.util.BooleanUtil;
import org.dromara.hutool.extra.spring.SpringUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DB工具类
 */
@Slf4j
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

    /**
     * 根据数据源，获取全部数据表
     */
    public static List<TableImportVO> getTableList(DataSourceBO datasource, String tableName) {
        List<TableImportVO> tableList = new ArrayList<>();
        try {
            AbstractQuery query = datasource.getDbQuery();

            //查询数据
            PreparedStatement preparedStatement = datasource.getConnection().prepareStatement(query.tableSql(tableName, true));
            String databaseName = getDatabaseName(datasource);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                TableImportVO table = new TableImportVO();
                table.setDatabaseName(databaseName);
                table.setTableName(rs.getString(query.tableName()));
                table.setTableComment(rs.getString(query.tableComment()));
                tableList.add(table);
            }

            datasource.getConnection().close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return tableList;
    }

    /**
     * 获取数据表列表
     */
    public static List<String> getTableNameList(DataSourceBO datasource) {
        return getTableList(datasource, null).stream()
                .map(TableImportVO::getTableName)
                .toList();
    }

    /**
     * 根据数据源，获取指定数据表
     *
     * @param datasource 数据源
     * @param tableName  表名
     */
    public static TableEntity getTable(DataSourceBO datasource, String tableName) {
        AbstractQuery query = datasource.getDbQuery();
        try (PreparedStatement preparedStatement = datasource.getConnection().prepareStatement(query.tableSql(tableName, false))) {

            //查询表元数据
            ResultSet rs;
            rs = preparedStatement.executeQuery();
            rs.next();
            TableEntity table = new TableEntity();
            //设置表名
            table.setTableName(rs.getString(query.tableName()));
            //设置表注释
            table.setTableComment(rs.getString(query.tableComment()));

            //设置数据库名称
            table.setDatabaseName(DbUtil.getDatabaseName(datasource));
            return table;
        } catch (Exception e) {
            throw new BusinessException("数据源连接失败：" + e.getMessage());
        }
    }


    /**
     * 获取表字段列表
     *
     * @param datasource 数据源
     * @param tableId    表ID
     * @param tableName  表名
     */
    public static List<TableFieldEntity> getTableFieldList(DataSourceBO datasource, Long tableId, String tableName) {
        List<TableFieldEntity> tableFieldList = new ArrayList<>();

        try {
            AbstractQuery query = datasource.getDbQuery();
            String tableFieldsSql = query.tableFieldsSql();
            if (datasource.getDbType() == DbType.Oracle) {
                DatabaseMetaData md = datasource.getConnection().getMetaData();
                tableFieldsSql = String.format(tableFieldsSql.replace("#schema", md.getUserName()), tableName);
            } else {
                tableFieldsSql = String.format(tableFieldsSql, tableName);
            }
            PreparedStatement preparedStatement = datasource.getConnection().prepareStatement(tableFieldsSql);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                TableFieldEntity field = new TableFieldEntity();
                tableFieldList.add(field);

                //设置表ID
                field.setTableId(tableId);

                //设置字段名称
                field.setFieldName(rs.getString(query.fieldName()));

                //设置字段类型
                setFieldType(rs, query, field);

                //设置字段注释
                field.setFieldComment(rs.getString(query.fieldComment()));

                //设置主键字段
                setPrimaryKey(rs, query, field);

                //设置逻辑删除字段
                setLogicDeleteInfo(field);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return tableFieldList;
    }

    private static void setFieldType(ResultSet rs, AbstractQuery query, TableFieldEntity field) throws SQLException {
        String fieldType = rs.getString(query.fieldType());
        if (fieldType.contains(" ")) {
            fieldType = fieldType.substring(0, fieldType.indexOf(" "));
        }
        field.setFieldType(fieldType);
    }

    /**
     * 设置主键字段
     */
    private static void setPrimaryKey(ResultSet rs, AbstractQuery query, TableFieldEntity field) throws SQLException {
        String key = rs.getString(query.fieldKey());
        boolean primaryPk = StrUtil.isNotBlank(key) && "PRI".equalsIgnoreCase(key);
        field.setPrimaryPk(BooleanUtil.toInteger(primaryPk));
    }

    /**
     * 设置逻辑删除相关字段
     */
    private static void setLogicDeleteInfo(TableFieldEntity field) {
        GeneratorConfig generatorConfig = SpringUtil.getBean(GeneratorConfig.class);
        boolean contains = generatorConfig.getLogicDeleteColumnList().contains(field.getFieldName());
        field.setLogicDelete(BooleanUtil.toInteger(contains));
        if (contains) {
            field.setLogicDeleteValue(generatorConfig.getLogicDeleteValue());
            field.setLogicNotDeleteValue(generatorConfig.getLogicNotDeleteValue());
        }
    }

}