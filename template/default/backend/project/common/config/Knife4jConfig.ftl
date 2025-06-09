package ${projectPackage}.${projectNameDot}.common.config;

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

    @Value("<#noparse>${spring.application.name:}</#noparse>")
    private String applicationName;

    @Value("<#noparse>${spring.application.version:}</#noparse>")
    private String version;

    @Value("<#noparse>${spring.application.description:}</#noparse>")
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