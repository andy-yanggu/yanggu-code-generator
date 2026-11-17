package com.yanggu.code.generator.common.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson配置
 * <p>LocalDateTime统一序列化/反序列化为yyyy-MM-dd HH:mm:ss，与原Date的全局格式保持一致</p>
 * <p>Jackson 3迁移：Jackson2ObjectMapperBuilderCustomizer与serializerByType便捷方法已移除，
 * 改为JsonMapperBuilderCustomizer，经SimpleModule注册序列化器</p>
 */
@Configuration
public class JacksonConfig {

    /**
     * 日期时间格式
     */
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public JsonMapperBuilderCustomizer localDateTimeCustomizer() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        SimpleModule module = new SimpleModule("LocalDateTimeFormatModule")
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        return builder -> builder.addModule(module);
    }

}
