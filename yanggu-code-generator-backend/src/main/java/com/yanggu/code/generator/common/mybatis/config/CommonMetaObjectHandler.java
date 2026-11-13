package com.yanggu.code.generator.common.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 填充公共字段
 */
public class CommonMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        //插入的时候设置添加和修改时间的值
        this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("isDelete", 0, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        //更新的时候设置修改时间的值
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }

}
