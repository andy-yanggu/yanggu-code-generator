package com.yanggu.code.generator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 代码生成器主启动类
 */
@SpringBootApplication
@MapperScan("com.yanggu.code.generator.mapper")
public class CodeGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeGeneratorApplication.class, args);
    }

}