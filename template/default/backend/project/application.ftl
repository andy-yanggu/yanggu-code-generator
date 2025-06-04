server:
  port: 8080
  servlet:
    context-path: /${projectName}

spring:
  # 项目名配置
  application:
    name: ${projectName}
    version: '${projectVersion}'
    description: '${projectDesc}'

  main:
    # 允许循环依赖
    allow-circular-references: true

  # 配置策略
  mvc:
    pathmatch:
      matching-strategy: ANT_PATH_MATCHER

  # jackson配置
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

  # 数据源配置
  datasource:
    driver-class-name: ${databaseDriverClassName}
    url: ${databaseUrl}
    username: ${databaseUsername}
    password: ${databasePassword}

# mybatis-plus配置
mybatis-plus:
  # mapper配置文件
  mapper-locations: classpath*:mapper/**/*.xml
  configuration:
    # 日志配置
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

knife4j:
  enable: true