package ${projectPackage}.${projectNameDot};

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ${projectNamePascal}主启动类
 */
@MapperScan("${projectPackage}.${projectNameDot}.mapper")
@SpringBootApplication(scanBasePackages = "${projectPackage}.${projectNameDot}.*")
public class ${projectNamePascal}Application {

    public static void main(String[] args) {
        SpringApplication.run(${projectNamePascal}Application.class, args);
    }

}