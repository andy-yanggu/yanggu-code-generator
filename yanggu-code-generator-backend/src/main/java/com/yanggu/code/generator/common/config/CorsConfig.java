package com.yanggu.code.generator.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 跨域配置
 */
@Configuration
@ConditionalOnProperty(name = "web.cors.enable", havingValue = "true", matchIfMissing = true)
public class CorsConfig {

    /**
     * 跨域过期时间
     */
    @Value("${web.cors.max-age:3600}")
    private Long maxAge;

    /**
     * 跨域传递cookie
     */
    @Value("${web.cors.allow-credentials:true}")
    private Boolean allowCredentials;

    /**
     * 允许跨域的请求头
     */
    @Value("#{'${web.cors.allowed-headers:*}'.split(',')}")
    private List<String> allowedHeaders;

    /**
     * 允许跨域的方法
     */
    @Value("#{'${web.cors.allowed-methods:*}'.split(',')}")
    private List<String> allowedMethods;

    /**
     * 允许跨域的源
     */
    @Value("#{'${web.cors.allowed-origins:*}'.split(',')}")
    private List<String> allowedOrigins;

    /**
     * 允许跨域的响应头
     */
    @Value("#{'${web.cors.expose-headers:Content-Disposition,Authorization,token,Cookie}'.split(',')}")
    private List<String> exposedHeaders;

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setMaxAge(maxAge);
        corsConfiguration.setAllowCredentials(allowCredentials);
        corsConfiguration.setAllowedHeaders(allowedHeaders);
        corsConfiguration.setAllowedMethods(allowedMethods);
        corsConfiguration.setAllowedOriginPatterns(allowedOrigins);
        corsConfiguration.setExposedHeaders(exposedHeaders);
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }

}