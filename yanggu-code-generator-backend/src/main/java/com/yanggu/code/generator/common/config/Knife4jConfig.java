package com.yanggu.code.generator.common.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j配置
 */
@Configuration
@EnableKnife4j
@ConditionalOnProperty(name = "web.knife4j.enable", havingValue = "true", matchIfMissing = true)
public class Knife4jConfig {

    /**
     * 应用名称
     */
    @Value("${spring.application.name:}")
    private String applicationName;

    /**
     * 应用版本
     */
    @Value("${spring.application.version:}")
    private String version;

    /**
     * 应用描述
     */
    @Value("${spring.application.description:}")
    private String description;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName)
                        .version(version)
                        .description(description)
                );
    }

}