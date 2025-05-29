package com.yanggu.code.generator.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 代码生成器配置
 */
@Data
@Component
public class GeneratorConfig {

    /**
     * 逻辑删除字段列表
     */
    @Value("#{'${generator.logic-delete-column:is_deleted,del_flag,is_del,del_status,is_del,del_status,is_delete}'.split(',')}")
    private List<String> logicDeleteColumnList;

    /**
     * 逻辑删除字段值
     */
    @Value("${generator.logic-delete-value:1}")
    private String logicDeleteValue;

    /**
     * 逻辑未删除字段值
     */
    @Value("${generator.logic-not-delete-value:0}")
    private String logicNotDeleteValue;

}
