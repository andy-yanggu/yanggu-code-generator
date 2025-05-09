package com.yanggu.code.generator.common.mybatis.typehandler.list;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 自定义Mybatis类型处理器。为JSON数组类型
 */
@NoArgsConstructor
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public abstract class AbstractListTypeHandler<T> extends BaseTypeHandler<List<T>> {

    //使用jackson序列化和反序列化
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 获取类型
     */
    protected abstract TypeReference<List<T>> getTypeReference();

    @Override
    @SneakyThrows
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> t, JdbcType jdbcType) {
        ps.setString(i, OBJECT_MAPPER.writeValueAsString(t));
    }

    @Override
    public List<T> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return toObject(resultSet.getObject(s));
    }

    @Override
    public List<T> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return toObject(resultSet.getObject(i));
    }

    @Override
    public List<T> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return toObject(callableStatement.getObject(i));
    }

    @SneakyThrows
    private List<T> toObject(Object o) {
        if (o == null) {
            return Collections.emptyList();
        }
        return OBJECT_MAPPER.readValue(o.toString(), getTypeReference());
    }

}