package ${projectPackage}.${projectNameDot}.common.config;

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
     * 跨域过期时间。单位：秒
     */
    @Value("<#noparse>${web.cors.max-age:3600}</#noparse>")
    private Long maxAge;

    /**
     * 跨域是否传递cookie
     */
    @Value("<#noparse>${web.cors.allow-credentials:true}</#noparse>")
    private Boolean allowCredentials;

    /**
     * 允许跨域的请求头
     */
    @Value("<#noparse>#{'${web.cors.allowed-headers:*}'.split(',')}</#noparse>")
    private List<String> allowedHeaders;

    /**
     * 允许跨域的方法
     */
    @Value("<#noparse>#{'${web.cors.allowed-methods:*}'.split(',')}</#noparse>")
    private List<String> allowedMethods;

    /**
     * 允许跨域的源
     */
    @Value("<#noparse>#{'${web.cors.allowed-origins:*}'.split(',')}</#noparse>")
    private List<String> allowedOrigins;

    /**
     * 允许跨域的响应头
     */
    @Value("<#noparse>#{'${web.cors.expose-headers:Content-Disposition,Authorization,token,Cookie}'.split(',')}</#noparse>")
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
