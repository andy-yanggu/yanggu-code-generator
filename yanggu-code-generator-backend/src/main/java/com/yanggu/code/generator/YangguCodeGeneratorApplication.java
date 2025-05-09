package com.yanggu.code.generator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 代码生成器主启动类
 */
@MapperScan("com.yanggu.code.generator.mapper")
@SpringBootApplication
public class YangguCodeGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(YangguCodeGeneratorApplication.class, args);
    }

}