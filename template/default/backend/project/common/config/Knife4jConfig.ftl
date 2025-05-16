package ${projectPackage}.${projectNameDot}.common.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 * 创建Knife4j配置
 */
@Configuration
@EnableKnife4j
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