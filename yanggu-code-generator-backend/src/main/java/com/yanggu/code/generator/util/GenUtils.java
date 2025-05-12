package com.yanggu.code.generator.util;

import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yanggu.code.generator.common.exception.BusinessException;
import com.yanggu.code.generator.domain.bo.GenDataSourceBO;
import com.yanggu.code.generator.domain.entity.TableEntity;
import com.yanggu.code.generator.domain.entity.TableFieldEntity;
import com.yanggu.code.generator.domain.vo.TableImportVO;
import com.yanggu.code.generator.enums.DbType;
import com.yanggu.code.generator.query.AbstractQuery;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.util.BooleanUtil;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 代码生成器 工具类
 *
 * @author 阿沐 babamu@126.com
 * <a href="https://maku.net">MAKU</a>
 */
@Slf4j
public class GenUtils {

    /**
     * 根据数据源，获取全部数据表
     *
     * @param datasource 数据源
     */
    public static List<TableImportVO> getTableList(GenDataSourceBO datasource, String tableName) {
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
    public static List<String> getTableNameList(GenDataSourceBO datasource) {
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
    public static TableEntity getTable(GenDataSourceBO datasource, String tableName) {
        try {
            AbstractQuery query = datasource.getDbQuery();

            // 查询数据
            PreparedStatement preparedStatement = datasource.getConnection().prepareStatement(query.tableSql(tableName));
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                TableEntity table = new TableEntity();
                table.setTableName(rs.getString(query.tableName()));
                table.setTableComment(rs.getString(query.tableComment()));

                //设置数据库名称
                table.setDatabaseName(datasource.getDatabaseName());
                return table;
            }
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
    public static List<TableFieldEntity> getTableFieldList(GenDataSourceBO datasource, Long tableId, String tableName) {
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
                field.setPrimaryPk(BooleanUtil.toInt(primaryPk));
                //设置逻辑删除字段
                boolean logicDeleteResult = setLogicDelete(rs, query);
                field.setLogicDelete(BooleanUtil.toInt(logicDeleteResult));
                if (logicDeleteResult) {
                    field.setLogicDeleteValue(SpringUtil.getProperty("generator.logic-delete-value", "1"));
                    field.setLogicNotDeleteValue(SpringUtil.getProperty("generator.logic-not-delete-value", "0"));
                }
                tableFieldList.add(field);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return tableFieldList;
    }

    /**
     * 判断字段是否为逻辑删除字段
     */
    private static boolean setLogicDelete(ResultSet rs, AbstractQuery query) throws SQLException {
        String columnName = rs.getString(query.fieldName());
        String property = SpringUtil.getProperty("generator.logic-delete-column");
        List<String> list = List.of("is_deleted", "del_flag", "is_del", "del_status", "is_del", "del_status", "is_delete");
        if (StrUtil.isNotBlank(property)) {
            list = Arrays.stream(property.split(",")).toList();
        }
        return list.contains(columnName);
    }

    /**
     * 获取模块名
     *
     * @param packageName 包名
     * @return 模块名
     */
    public static String getModuleName(String packageName) {
        return StrUtil.subAfter(packageName, ".", true);
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

    /**
     * 表名转驼峰并移除前后缀
     *
     * @param upperFirst   首字母大写
     * @param tableName    表名
     * @param removePrefix 删除前缀
     * @param removeSuffix 删除后缀
     * @return java.lang.String
     */
    public static String camelCase(boolean upperFirst, String tableName, String removePrefix, String removeSuffix) {
        String className = tableName;
        // 移除前缀
        if (StrUtil.isNotBlank(removePrefix)) {
            className = StrUtil.removePrefix(tableName, removePrefix);
        }
        // 移除后缀
        if (StrUtil.isNotBlank(removeSuffix)) {
            className = StrUtil.removeSuffix(className, removeSuffix);
        }
        // 是否首字母大写
        if (upperFirst) {
            return NamingCase.toPascalCase(className);
        } else {
            return NamingCase.toCamelCase(className);
        }
    }
}
