package com.yanggu.code.generator.domain.bo;

import com.yanggu.code.generator.domain.entity.DatasourceEntity;
import com.yanggu.code.generator.enums.DbType;
import com.yanggu.code.generator.query.AbstractQuery;
import com.yanggu.code.generator.util.DbUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.util.EnumUtil;

import java.sql.Connection;

/**
 * 数据源业务实体类
 */
@Data
@Slf4j
public class DataSourceBO implements AutoCloseable {

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

    public DataSourceBO(DatasourceEntity entity) throws Exception {
        this.id = entity.getId();
        this.dbType = EnumUtil.getBy(DbType::getCode, entity.getDbType());
        this.connUrl = entity.getConnUrl();
        this.username = entity.getUsername();
        this.password = entity.getPassword();
        this.dbQuery = AbstractQuery.getQuery(this.dbType);
        this.connection = DbUtil.getConnection(this);
    }

    @Override
    public void close() throws Exception {
        if (this.connection != null) {
            connection.close();
        }
    }

}
