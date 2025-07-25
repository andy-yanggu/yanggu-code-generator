<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- 服务名称 -->
    <springProperty name="APP_NAME" source="spring.application.name" defaultValue="app" />
    <!-- 日志格式 -->
    <springProperty name="LOG_PATTERN" source="logging.pattern.console"
                    defaultValue="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36}:%line - %msg%n"/>
    <!-- 指定日志输出路径 -->
    <springProperty name="LOG_PATH" source="logging.file.path" defaultValue="./logs" />
    <!-- 日志文件最大大小 -->
    <springProperty name="MAX_FILE_SIZE" source="logging.max-file-size" defaultValue="200MB" />
    <!-- 日志保留天数 -->
    <springProperty name="MAX_HISTORY" source="logging.max-history" defaultValue="30" />

    <!-- 输出到控制台 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <Pattern><#noparse>${LOG_PATTERN}</#noparse></Pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- 所有日志级别 -->
    <appender name="ALL_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 正在记录的日志文档的路径及文档名 -->
        <file><#noparse>${LOG_PATH}/${APP_NAME}-all.log</#noparse></file>
        <!--日志文档输出格式-->
        <encoder>
            <pattern><#noparse>${LOG_PATTERN}</#noparse></pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern><#noparse>${LOG_PATH}/${APP_NAME}-all-%d{yyyy-MM-dd}-%i.log.gz</#noparse></fileNamePattern>
            <maxFileSize><#noparse>${MAX_FILE_SIZE}</#noparse></maxFileSize>
            <maxHistory><#noparse>${MAX_HISTORY}</#noparse></maxHistory>
        </rollingPolicy>
    </appender>

    <!-- DEBUG日志级别 -->
    <appender name="DEBUG_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 正在记录的日志文档的路径及文档名 -->
        <file><#noparse>${LOG_PATH}/${APP_NAME}-debug.log</#noparse></file>
        <!--日志文档输出格式-->
        <encoder>
            <pattern><#noparse>${LOG_PATTERN}</#noparse></pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern><#noparse>${LOG_PATH}/${APP_NAME}-debug-%d{yyyy-MM-dd}-%i.log.gz</#noparse></fileNamePattern>
            <maxFileSize><#noparse>${MAX_FILE_SIZE}</#noparse></maxFileSize>
            <maxHistory><#noparse>${MAX_HISTORY}</#noparse></maxHistory>
        </rollingPolicy>
        <!-- 此日志文档只记录debug级别的 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>DEBUG</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- INFO日志级别 -->
    <appender name="INFO_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file><#noparse>${LOG_PATH}/${APP_NAME}-info.log</#noparse></file>
        <encoder>
            <pattern><#noparse>${LOG_PATTERN}</#noparse></pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern><#noparse>${LOG_PATH}/${APP_NAME}-info-%d{yyyy-MM-dd}-%i.log.gz</#noparse></fileNamePattern>
            <maxFileSize><#noparse>${MAX_FILE_SIZE}</#noparse></maxFileSize>
            <maxHistory><#noparse>${MAX_HISTORY}</#noparse></maxHistory>
        </rollingPolicy>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>INFO</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- WARN日志级别 -->
    <appender name="WARN_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file><#noparse>${LOG_PATH}/${APP_NAME}-warn.log</#noparse></file>
        <encoder>
            <pattern><#noparse>${LOG_PATTERN}</#noparse></pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern><#noparse>${LOG_PATH}/${APP_NAME}-warn-%d{yyyy-MM-dd}-%i.log.gz</#noparse></fileNamePattern>
            <maxFileSize><#noparse>${MAX_FILE_SIZE}</#noparse></maxFileSize>
            <maxHistory><#noparse>${MAX_HISTORY}</#noparse></maxHistory>
        </rollingPolicy>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>WARN</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- ERROR日志级别 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file><#noparse>${LOG_PATH}/${APP_NAME}-error.log</#noparse></file>
        <encoder>
            <pattern><#noparse>${LOG_PATTERN}</#noparse></pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern><#noparse>${LOG_PATH}/${APP_NAME}-error-%d{yyyy-MM-dd}-%i.log.gz</#noparse></fileNamePattern>
            <maxFileSize><#noparse>${MAX_FILE_SIZE}</#noparse></maxFileSize>
            <maxHistory><#noparse>${MAX_HISTORY}</#noparse></maxHistory>
        </rollingPolicy>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- mapper接口日志配置 -->
    <logger name="${projectPackage}.${projectNameDot}.mapper" level="DEBUG" />

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="DEBUG_FILE" />
        <appender-ref ref="ERROR_FILE" />
        <appender-ref ref="WARN_FILE" />
        <appender-ref ref="INFO_FILE" />
        <appender-ref ref="ALL_FILE" />
    </root>

</configuration>