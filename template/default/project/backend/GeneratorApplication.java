package ${projectPackage}.${projectNameUnderline};

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ${projectNamePascal}主启动类
 */
@MapperScan("${projectPackage}.${projectNameUnderline}.mapper")
@SpringBootApplication(scanBasePackages = "com.yanggu.*")
public class ${projectNamePascal}Application {

    public static void main(String[] args) {
        SpringApplication.run(${projectNamePascal}Application.class, args);
    }

}