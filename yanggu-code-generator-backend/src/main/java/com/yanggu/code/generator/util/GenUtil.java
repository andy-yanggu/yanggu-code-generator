package com.yanggu.code.generator.util;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
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
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.util.BooleanUtil;
import org.dromara.hutool.extra.spring.SpringUtil;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成器工具类
 */
@Slf4j
@UtilityClass
public class GenUtil {

    /**
     * 根据数据源，获取全部数据表
     *
     * @param datasource 数据源
     */
    public static List<TableImportVO> getTableList(DataSourceBO datasource, String tableName) {
        List<TableImportVO> tableList = new ArrayList<>();
        try {
            AbstractQuery query = datasource.getDbQuery();

            //查询数据
            PreparedStatement preparedStatement = datasource.getConnection().prepareStatement(query.tableSql(tableName));
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                TableImportVO table = new TableImportVO();
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
        try {
            AbstractQuery query = datasource.getDbQuery();

            //查询表元数据
            PreparedStatement preparedStatement = datasource.getConnection().prepareStatement(query.tableSql(tableName));
            ResultSet rs = preparedStatement.executeQuery();
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
            log.error(e.getMessage(), e);
        }

        throw new BusinessException("数据表不存在：" + tableName);
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
                field.setTableId(tableId);
                field.setFieldName(rs.getString(query.fieldName()));
                String fieldType = rs.getString(query.fieldType());
                if (fieldType.contains(" ")) {
                    fieldType = fieldType.substring(0, fieldType.indexOf(" "));
                }
                field.setFieldType(fieldType);
                field.setFieldComment(rs.getString(query.fieldComment()));
                String key = rs.getString(query.fieldKey());
                boolean primaryPk = StringUtils.isNotBlank(key) && "PRI".equalsIgnoreCase(key);
                field.setPrimaryPk(BooleanUtil.toInteger(primaryPk));
                //设置逻辑删除字段
                setLogicDeleteInfo(field);
                tableFieldList.add(field);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return tableFieldList;
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

    /**
     * 获取功能名，默认使用表名作为功能名
     *
     * @param tableName 表名
     * @return 功能名
     */
    public static String getFunctionName(String tableName) {
        return StrUtil.toCamelCase(tableName);
    }

}
