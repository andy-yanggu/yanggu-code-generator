package com.yanggu.code.generator.common.mybatis.typehandler.map;


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
import java.util.Map;
import java.util.Objects;

/**
 * 自定义Mybatis类型处理器。为Map类型
 */
@NoArgsConstructor
@MappedTypes(Map.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public abstract class AbstractMapBaseTypeHandler<K, V> extends BaseTypeHandler<Map<K, V>> {

    //使用jackson序列化和反序列化
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 获取类型
     */
    protected abstract TypeReference<Map<K, V>> getTypeReference();

    @Override
    @SneakyThrows
    public void setNonNullParameter(PreparedStatement ps, int i, Map<K, V> t, JdbcType jdbcType) {
        ps.setString(i, OBJECT_MAPPER.writeValueAsString(t));
    }

    @Override
    public Map<K, V> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return toObject(resultSet.getObject(s));
    }

    @Override
    public Map<K, V> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return toObject(resultSet.getObject(i));
    }

    @Override
    public Map<K, V> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return toObject(callableStatement.getObject(i));
    }

    @SneakyThrows
    private Map<K, V> toObject(Object o) {
        if (Objects.isNull(o)) {
            return Map.of();
        }
        return OBJECT_MAPPER.readValue(o.toString(), getTypeReference());
    }

}
